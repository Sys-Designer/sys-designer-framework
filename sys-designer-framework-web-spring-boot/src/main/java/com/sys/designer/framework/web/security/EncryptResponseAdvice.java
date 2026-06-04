package com.sys.designer.framework.web.security;

import com.sys.designer.framework.api.security.AesCryptoService;
import com.sys.designer.framework.api.session.SessionService;
import com.sys.designer.framework.api.session.UserBaseInfo;
import com.sys.designer.framework.common.entity.ResultData;
import com.sys.designer.framework.common.util.JsonUtil;
import com.sys.designer.framework.common.util.SessionUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import com.sys.designer.framework.web.util.ApiUtil;
import jakarta.annotation.Resource;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Objects;

@ControllerAdvice
public class EncryptResponseAdvice implements ResponseBodyAdvice<Object> {
    @Resource
    private AesCryptoService aesCryptoService;
    @Resource
    private SessionService sessionService;

    @Override
    public boolean supports(MethodParameter p, Class<? extends HttpMessageConverter<?>> c) {
        return p.hasMethodAnnotation(EncryptResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter p, MediaType m, Class<? extends HttpMessageConverter<?>> c, ServerHttpRequest req, ServerHttpResponse res) {
        UserBaseInfo userInfo = SessionUtil.getUserInfo();
        if (Objects.isNull(userInfo)) {
            userInfo = sessionService.getUserInfo(ApiUtil.getToken());
            if (Objects.isNull(userInfo)) {
                return o;
            }
        }
        if (ValueUtil.isEmpty(userInfo.getSecurityKey())) {
            return null;
        }
        try {
            String json = JsonUtil.toJson(o);
            ResultData<String> resultData = ResultData.isOk(aesCryptoService.encrypt(json, userInfo.getSecurityKey()));
            resultData.setEncrypt(true);
            return resultData;
        } catch (Exception e) {
            return o;
        }
    }
}
