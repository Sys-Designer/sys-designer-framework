package com.sys.designer.framework.web.agent;

import com.sys.designer.framework.agent.AgentManager;
import com.sys.designer.framework.api.agent.Agent;
import com.sys.designer.framework.api.agent.AgentInfo;
import com.sys.designer.framework.api.agent.AgentMessage;
import com.sys.designer.framework.common.constant.CommonConst;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CommonConst.API_PREFIX)
@ConditionalOnBean(Agent.class)
public class AgentController {
    @Resource
    private AgentManager agentManager;

//    @PostMapping("/agent")
//    public Mono<AgentInfo> createAgent(@RequestBody AgentMessage agentMessage) {
//        return agentManager.getAgent(agentMessage.getId()).create(agentMessage);
//    }
//
//    @Post
}
