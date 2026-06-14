package com.sys.designer.framework.web.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.sys.designer.framework.agent.AgentManager;
import com.sys.designer.framework.api.ErrorCode;
import com.sys.designer.framework.api.tool.GlobalRuleLoader;
import com.sys.designer.framework.api.tool.ToolLoader;
import com.sys.designer.framework.api.tool.ToolManager;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.exception.ErrorCodeRuntimeException;
import com.sys.designer.framework.common.util.ComponentUtil;
import com.sys.designer.framework.common.util.JsonUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import com.sys.designer.framework.entity.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@ConditionalOnBean(ToolManager.class)
public class McpProtocolService {
    @Value("${oc.mcp.name:idesign}")
    private String name;
    @Value("${oc.mcp.version:2.0}")
    private String version;
    @Value("${oc.mcp.protocol.version:2025-06-18}")
    private String protocolVersion;
    private ToolManager toolManager;
    @Autowired(required = false)
    private AgentManager agentManager;
    @Autowired(required = false)
    private GlobalRuleLoader globalRuleLoader;
    private final static Logger LOGGER = LoggerFactory.getLogger(McpProtocolService.class);

    public McpProtocolService(ToolManager toolManager) {
        this.toolManager = toolManager;
        toolManager.setName(name);
    }

    public Object handler(JsonRpcRequest request) {
        return handler(null, request);
    }

    public Object handler(String sessionId, JsonRpcRequest request) {
        try {
            if (Objects.isNull(request) || ValueUtil.isEmpty(request.method())) {
                return JsonRpcResponse.error(null, -32600, "Invalid request");
            }
            return switch (request.method()) {
                case "initialize" -> handleInitialize(sessionId, request);
                case "tools/list" -> handleListTools(request);
                case "tools/call" -> handleCallTool(request);
                case "ping" -> JsonRpcResponse.success(request.id(), Map.of());
                case "notifications/initialized" -> null;
                default -> JsonRpcResponse.error(request.id(), -32600, "method(" + request.method() + ") not found");
            };
        } catch (Exception ex) {
            String message = ex.getMessage();
            if (ex instanceof BusinessRuntimeException runtimeException) {
                ErrorCode code = runtimeException.getCode();
                message = code.getMessage();
            } else if (ex instanceof ErrorCodeRuntimeException e) {
                message = e.getErrorCode() + " - " + e.getMessage();
            }
            LOGGER.error("err", ex);
            return JsonRpcResponse.error(request.id(), -32600, message);
        }
    }


    // 1. 握手协议
    private JsonRpcResponse handleInitialize(String sessionId, JsonRpcRequest request) {
        JsonNode jsonNode = request.params().get("clientInfo").get("name");
        String name = jsonNode.asText();

        if (Objects.nonNull(agentManager)) {
            agentManager.setAgentSender(name, (data) -> {
                Tuple2<SseEmitter, Long> item = McpController.STREAMABLE_SESSION_MAP.get(sessionId);
                if (Objects.nonNull(item) && Objects.nonNull(item.getFirst())) {
                    SseEmitter sseEmitter = item.getFirst();
                    try {
                        sseEmitter.send(SseEmitter.event().name("sampling/createMessage").data(data));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        Map<String, Object> result = new HashMap<>();
        result.put("protocolVersion", protocolVersion);
        result.put("capabilities", Map.of("tools", Map.of())); // 声明支持工具
        result.put("serverInfo", Map.of("name", name, "version", version));
        if (Objects.nonNull(globalRuleLoader)) {
            result.put("instructions", globalRuleLoader.load());
        }

        return JsonRpcResponse.success(request.id(), result);
    }

    // 2. 定义工具列表
    private JsonRpcResponse handleListTools(JsonRpcRequest request) {
        List<Map<String, Object>> list = toolManager.getTools();
        List<Map<String, Object>> resultList = new ArrayList<>();
        if (Objects.nonNull(list)) {
            resultList.addAll(list);
        }
        try {
            Map<String, ToolLoader> beans = ComponentUtil.getBeans(ToolLoader.class);
            Collection<ToolLoader> values = beans.values();
            for (ToolLoader value : values) {
                List<Map<String, Object>> tools = value.getTools();
                if (Objects.nonNull(tools)) {
                    resultList.addAll(tools);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        LOGGER.info("tools/list count={}", resultList.size());
        return JsonRpcResponse.success(request.id(), Map.of("tools", resultList));
    }

    private Object handleCallTool(JsonRpcRequest request) {
        String name = request.params().get("name").asText();
        Map<String, Object> arguments = JsonUtil.toBean(request.params().get("arguments").toString(), Map.class);

        if (toolManager.isClientMethod(name)) {
            Map<String, Object> func = new HashMap<>();
            name = name.substring(name.indexOf(".") + 1);
            func.put("name", name);
            func.put("arguments", arguments);

            Map<String, Object> map = new HashMap<>();
            map.put("type", "text");
            map.put("text", "前端执行");
            map.put("functionCall", func);

            Map<String, Object> content = Map.of(
                    "content", List.of(map)
            );

            return JsonRpcResponse.success(request.id(), content);
        }
        Object result = null;
        Map<String, Object> data = new HashMap<>();
        Integer errorCode = 200;
        String message = "";
        try {
            result = toolManager.execute(name, arguments);
            data.put("results", result);
        } catch (Exception e) {
            if (e instanceof BusinessRuntimeException runtimeException) {
                ErrorCode code = runtimeException.getCode();
                if (Objects.nonNull(code)) {
                    if (code.isServerError()) {
                        errorCode = -32603;
                    } else {
                        errorCode = -32600;
                        message = runtimeException.getMessage();
                    }
                    if (CommonErrorCode.PARAMETER_INVALID.equals(code) ||
                            CommonErrorCode.ALREADY_EXISTS.equals(code) ||
                            CommonErrorCode.PARAMETER_MISSING.equals(code)) {
                        errorCode = -32602;
                    } else if (CommonErrorCode.NOT_FOUND.equals(code)) {
                        errorCode = -32001;
                    }
                } else if (e instanceof ErrorCodeRuntimeException) {
                    errorCode = -32600;
                    message = runtimeException.getMessage();
                }
            }
        }
        data.put("code", errorCode);
        data.put("message", message);

        int total = 1;
        if (Objects.isNull(result)) {
            total = 0;
        } else if (result instanceof List list) {
            total = list.size();
        }
        data.put("total", total);
        Map<String, Object> content = new HashMap<>();


        String text = JsonUtil.toJson(result);

        List<Map<String, Object>> contentList = new ArrayList<>();
        if (Objects.nonNull(text)) {
            Map<String, Object> contentDataMap = new HashMap<>();
            contentList.add(contentDataMap);
            contentDataMap.put("type", "text");
            contentDataMap.put("text", text);
        }
        content.put("content", contentList);
        content.put("structuredContent", data);
        return JsonRpcResponse.success(request.id(),
                content);
    }
}
