package com.sys.designer.framework.web.mcp;

import com.sys.designer.framework.api.permission.PermissionConst;
import com.sys.designer.framework.api.tool.ToolManager;
import com.sys.designer.framework.common.constant.CommonConst;
import com.sys.designer.framework.common.util.JsonUtil;
import com.sys.designer.framework.common.util.PermissionUtil;
import com.sys.designer.framework.common.util.SessionUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import com.sys.designer.framework.entity.Tuple2;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP HTTP 传输层：协议由 {@link McpProtocolService#handler(JsonRpcRequest)} 处理。
 * <ul>
 *   <li>Streamable HTTP: POST/GET/DELETE {@code /mcp}</li>
 *   <li>Legacy: GET {@code /mcp/sse} + POST {@code /mcp/messages}</li>
 * </ul>
 */
@RestController
@RequestMapping(CommonConst.API_PREFIX)
@ConditionalOnBean(ToolManager.class)
public class McpController {

    private static final Logger LOGGER = LoggerFactory.getLogger(McpController.class);

    public static final String MCP_SESSION_ID_HEADER = "Mcp-Session-Id";

    private static final String METHOD_INITIALIZE = "initialize";
    private static final String INVALID_SESSION_MARKER = "\0INVALID_SESSION\0";

    private static final Map<String, Tuple2<SseEmitter, Long>> LEGACY_SESSION_MAP = new ConcurrentHashMap<>();
    public static final Map<String, Tuple2<SseEmitter, Long>> STREAMABLE_SESSION_MAP = new ConcurrentHashMap<>();

    private final McpProtocolService mcpProtocolService;
    private final ToolManager toolManager;

    @Value("${oc.mcp.timeout:0}")
    private Long sseTimeout;

    public McpController(McpProtocolService mcpProtocolService, ToolManager toolManager) {
        this.mcpProtocolService = mcpProtocolService;
        this.toolManager = toolManager;
    }

    // ======================== Streamable HTTP ========================

    @CrossOrigin
    @PostMapping(
            value = "/mcp",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> streamableHttp(
            HttpServletRequest servletRequest,
            @RequestBody Object body,
            @RequestHeader(value = MCP_SESSION_ID_HEADER, required = false) String mcpSessionId) {

        if (!checkMcpToken("mcp")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SessionUtil.setSource(CommonConst.MCP_SOURCE);
        List<JsonRpcRequest> messages = parseJsonRpcBody(body);
        if (messages.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<JsonRpcRequest> notifications = new ArrayList<>();
        List<JsonRpcRequest> requests = new ArrayList<>();
        for (JsonRpcRequest msg : messages) {
            if (isJsonRpcRequest(msg)) {
                requests.add(msg);
            } else {
                notifications.add(msg);
            }
        }

        for (JsonRpcRequest notification : notifications) {
            mcpProtocolService.handler(notification);
        }
        if (requests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }

        String sessionId = resolveStreamableSession(servletRequest, requests, mcpSessionId);
        if (ValueUtil.isEmpty(sessionId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("PRIVATE-TOKEN not found.");
        }
        if (INVALID_SESSION_MARKER.equals(sessionId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<Object> results = new ArrayList<>();
        for (JsonRpcRequest request : requests) {
            Object res = mcpProtocolService.handler(sessionId, request);
            if (res == null) {
                res = JsonRpcResponse.error(request.id(), -32603, "empty handler response");
            }
            results.add(res);
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(sessionId)) {
            builder.header(MCP_SESSION_ID_HEADER, sessionId);
        }
        if (results.size() == 1) {
            return builder.body(results.getFirst());
        }
        return builder.body(results);
    }

    @CrossOrigin
    @GetMapping(value = "/mcp", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Object streamableHttpGet(
            @RequestHeader(value = HttpHeaders.ACCEPT, required = false) String accept,
            @RequestHeader(value = MCP_SESSION_ID_HEADER, required = false) String mcpSessionId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {

        if (!StringUtils.hasText(accept) || !accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }
        if (!checkMcpToken("mcp")) {
            SseEmitter denied = new SseEmitter(sseTimeout);
            denied.complete();
            return denied;
        }

        SessionUtil.setSource(CommonConst.MCP_SOURCE);
        if (!StringUtils.hasText(mcpSessionId)) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
        }
        Long userId = SessionUtil.userId();
        if (!isValidStreamableSession(mcpSessionId, userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        SseEmitter emitter = new SseEmitter(sseTimeout);
        Tuple2<SseEmitter, Long> temp = STREAMABLE_SESSION_MAP.get(mcpSessionId);
        if (Objects.nonNull(temp) && Objects.nonNull(temp.getFirst())) {
            try {
                SseEmitter first = temp.getFirst();
                first.complete();
            } catch (Exception e) {
                // ignore
            }
        }
        STREAMABLE_SESSION_MAP.put(mcpSessionId, new Tuple2<>(emitter, userId));
        Runnable cleanup = () -> removeStreamableEmitter(mcpSessionId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());
        try {
            if (StringUtils.hasText(lastEventId)) {
                emitter.send(SseEmitter.event().id(lastEventId).comment("resume"));
            } else {
                emitter.send(SseEmitter.event().comment("open"));
            }
        } catch (IOException e) {
            cleanup.run();
        }
        return emitter;
    }

    @CrossOrigin
    @DeleteMapping("/mcp")
    public ResponseEntity<Void> streamableHttpDelete(
            @RequestHeader(value = MCP_SESSION_ID_HEADER, required = false) String mcpSessionId) {

        if (!StringUtils.hasText(mcpSessionId)) {
            return ResponseEntity.badRequest().build();
        }
        Tuple2<SseEmitter, Long> removed = STREAMABLE_SESSION_MAP.remove(mcpSessionId);
        if (removed != null && removed.getFirst() != null) {
            removed.getFirst().complete();
        }
        return ResponseEntity.noContent().build();
    }

    // ======================== Legacy HTTP+SSE ========================

    @CrossOrigin
    @GetMapping(value = "/mcp/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter legacySse(HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(sseTimeout);
        if (!checkMcpToken("mcp", "mcp_message")) {
            emitter.complete();
            return emitter;
        }

        SessionUtil.setSource(CommonConst.MCP_SOURCE);
        Long userId = SessionUtil.userId();
        if (userId == null) {
            LOGGER.error("legacy sse: not login");
            emitter.complete();
            return emitter;
        }

        String sessionId = request.getHeader(CommonConst.PRIVATE_TOKEN);
        Tuple2<SseEmitter, Long> item = LEGACY_SESSION_MAP.get(sessionId);
        if (Objects.nonNull(item) && Objects.nonNull(item.getFirst())) {
            try {
                item.getFirst().complete();
            } catch (Exception e) {
                LOGGER.error("close sse error", e);
            }
        }
        LEGACY_SESSION_MAP.put(sessionId, new Tuple2<>(emitter, userId));
        Runnable cleanup = () -> LEGACY_SESSION_MAP.remove(sessionId);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());
        try {
            emitter.send(SseEmitter.event().name("endpoint").data(legacyMessagesEndpoint(sessionId)));
        } catch (IOException e) {
            LEGACY_SESSION_MAP.remove(sessionId);
            emitter.complete();
        }
        return emitter;
    }

    @CrossOrigin
    @PostMapping(value = "/mcp/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> legacyMessage(
            HttpServletRequest httpServletRequest,
            @RequestBody JsonRpcRequest request) {

        String sessionId = httpServletRequest.getParameter("sessionId");
        if (!checkMcpToken("mcp", "mcp_message")) {
            return ResponseEntity.badRequest().build();
        }

        SessionUtil.setSource(CommonConst.MCP_SOURCE);
        Tuple2<SseEmitter, Long> item = LEGACY_SESSION_MAP.get(sessionId);
        Long currentUser = SessionUtil.userId();
        if (item == null || !sessionUserMatches(item.getSecond(), currentUser)) {
            return ResponseEntity.badRequest().build();
        }
        SseEmitter out = item.getFirst();
        if (out == null) {
            return ResponseEntity.badRequest().build();
        }

        LOGGER.info("legacy MCP method={}", request.method());
        if (!isJsonRpcRequest(request)) {
            mcpProtocolService.handler(request);
            return ResponseEntity.accepted().build();
        }

        Object res = mcpProtocolService.handler(request);
        if (res == null) {
            return ResponseEntity.accepted().build();
        }
        try {
            String line = JsonUtil.toJson(res);
            synchronized (out) {
                SseEmitter.SseEventBuilder builder = SseEmitter.event();
                String eventName = toolManager.getEventName(request.method());
                if (StringUtils.hasText(eventName)) {
                    builder.name(eventName);
                }
                out.send(builder.data(line, MediaType.APPLICATION_JSON));
            }
        } catch (Exception e) {
            LEGACY_SESSION_MAP.remove(sessionId);
            LOGGER.error("legacy SSE send error, method={}", request.method(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.accepted().build();
    }

    // ======================== Helpers ========================
    private String resolveStreamableSession(HttpServletRequest request, List<JsonRpcRequest> requests, String headerSessionId) {
        boolean initializing = requests.stream()
                .anyMatch(r -> METHOD_INITIALIZE.equalsIgnoreCase(r.method()));
        Long userId = SessionUtil.userId();

        if (initializing) {
            String sessionId = request.getHeader(CommonConst.PRIVATE_TOKEN);
            if (ValueUtil.isEmpty(sessionId)) {
                return sessionId;
            }
            Tuple2<SseEmitter, Long> target = STREAMABLE_SESSION_MAP.get(sessionId);
            if (Objects.nonNull(target) && Objects.nonNull(target.getFirst())) {
                target.getFirst().complete();
            }
            STREAMABLE_SESSION_MAP.put(sessionId, new Tuple2<>(null, userId));
            LOGGER.info("MCP session created: {}", sessionId);
            return sessionId;
        }
        if (!StringUtils.hasText(headerSessionId)) {
            return null;
        }
        if (!isValidStreamableSession(headerSessionId, userId)) {
            return INVALID_SESSION_MARKER;
        }
        return headerSessionId;
    }

    private boolean isValidStreamableSession(String sessionId, Long userId) {
        Tuple2<SseEmitter, Long> item = STREAMABLE_SESSION_MAP.get(sessionId);
        if (item == null) {
            return false;
        }
        Long bound = item.getSecond();
        return bound == null || userId == null || Objects.equals(bound, userId);
    }

    private void removeStreamableEmitter(String sessionId, SseEmitter emitter) {
        Tuple2<SseEmitter, Long> item = STREAMABLE_SESSION_MAP.get(sessionId);
        if (item != null && item.getFirst() == emitter) {
            STREAMABLE_SESSION_MAP.put(sessionId, new Tuple2<>(null, item.getSecond()));
        }
    }

    private boolean checkMcpToken(String... resources) {
        for (String resource : resources) {
            if (PermissionUtil.checkPrivateToken(resource, PermissionConst.RESOURCE_SSE)) {
                return true;
            }
        }
        LOGGER.error("permission denied for {}", String.join(",", resources));
        return false;
    }

    private boolean sessionUserMatches(Long sessionUser, Long currentUser) {
        return sessionUser == null || currentUser == null || Objects.equals(sessionUser, currentUser);
    }

    /**
     * JSON-RPC request：有 id，或 initialize（可无 id）
     */
    private boolean isJsonRpcRequest(JsonRpcRequest msg) {
        if (msg == null || !StringUtils.hasText(msg.method())) {
            return false;
        }
        if (METHOD_INITIALIZE.equalsIgnoreCase(msg.method())) {
            return true;
        }
        return msg.id() != null && !msg.id().isNull();
    }

    @SuppressWarnings("unchecked")
    private List<JsonRpcRequest> parseJsonRpcBody(Object body) {
        List<JsonRpcRequest> list = new ArrayList<>();
        if (body == null) {
            return list;
        }
        if (body instanceof List<?> rawList) {
            for (Object item : rawList) {
                JsonRpcRequest req = toJsonRpcRequest(item);
                if (req != null) {
                    list.add(req);
                }
            }
            return list;
        }
        JsonRpcRequest single = toJsonRpcRequest(body);
        if (single != null) {
            list.add(single);
        }
        return list;
    }

    private JsonRpcRequest toJsonRpcRequest(Object item) {
        if (item instanceof JsonRpcRequest req) {
            return req;
        }
        String json = JsonUtil.toJson(item);
        return JsonUtil.toBean(json, JsonRpcRequest.class);
    }

    private static String legacyMessagesEndpoint(String sessionId) {
        String prefix = CommonConst.API_PREFIX;
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix + "mcp/messages?sessionId=" + sessionId;
    }
}
