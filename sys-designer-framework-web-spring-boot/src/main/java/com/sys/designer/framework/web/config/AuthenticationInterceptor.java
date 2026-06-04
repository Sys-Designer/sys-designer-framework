/**
 * Copyright (C) Oceancode Cloud. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.web.config;

import com.sys.designer.framework.api.session.SessionService;
import com.sys.designer.framework.common.config.CommonConfig;
import com.sys.designer.framework.common.constant.CommonConst;
import com.sys.designer.framework.common.util.SessionUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import com.sys.designer.framework.web.util.ApiUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

public class AuthenticationInterceptor implements HandlerInterceptor {
    private CommonConfig commonConfig;

    public AuthenticationInterceptor(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUrl = request.getRequestURI();
        boolean ret = true;
        if (requestUrl.startsWith(CommonConst.API_PREFIX)) {
            MDC.put(CommonConst.TRACE_ID, UUID.randomUUID().toString());
            MDC.put(CommonConst.SERVICE_NAME, commonConfig.getServiceName());
            MDC.put(CommonConst.INSTANCE_NAME, commonConfig.getInstanceName());
            ret = doApiHandler(request);
        }
        if (ValueUtil.isEmpty(SessionUtil.requestId())) {
            SessionUtil.setRequestId(UUID.randomUUID().toString());
        }
        return ret;
    }

    private boolean doApiHandler(HttpServletRequest request) {
        ApiUtil.processCommonArguments(request);
        if (commonConfig.isMicroService()) {
            String userId = request.getHeader(CommonConst.X_USER_ID);
            if (ValueUtil.isEmpty(userId)) {
                userId = request.getParameter(CommonConst.X_USER_ID);
            }
            if (ValueUtil.isNotEmpty(userId)) {
                SessionUtil.setUserId(Long.parseLong(userId));
                MDC.put(CommonConst.USER_ID, userId);
            }

            String requestId = request.getHeader(CommonConst.X_REQUEST_ID);
            if (ValueUtil.isEmpty(requestId)) {
                requestId = request.getParameter(CommonConst.X_REQUEST_ID);
            }
            if (ValueUtil.isNotEmpty(requestId)) {
                MDC.put(CommonConst.REQUEST_ID, requestId);
            }
        } else {
            String requestId = request.getHeader(CommonConst.X_REQUEST_ID);
            if (ValueUtil.isNotEmpty(requestId) && requestId.length() <= 32) {
                MDC.put(CommonConst.REQUEST_ID, requestId);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        SessionUtil.remove();
        MDC.clear();
    }
}
