package com.sys.designer.framework.web.handler;

import com.sys.designer.framework.api.ApplicationLifeCycleService;
import com.sys.designer.framework.api.interceptor.FunctionInterceptor;
import com.sys.designer.framework.api.permission.ResourcePermissionService;
import com.sys.designer.framework.api.permission.Permission;
import com.sys.designer.framework.api.permission.PermissionConst;
import com.sys.designer.framework.api.security.AesCryptoService;
import com.sys.designer.framework.api.security.Rsa2CryptoService;
import com.sys.designer.framework.api.session.RoleType;
import com.sys.designer.framework.api.session.SessionService;
import com.sys.designer.framework.api.session.UserBaseInfo;
import com.sys.designer.framework.api.session.UserType;
import com.sys.designer.framework.common.config.CommonConfig;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.exception.ErrorCodeRuntimeException;
import com.sys.designer.framework.common.util.ComponentUtil;
import com.sys.designer.framework.common.util.PermissionUtil;
import com.sys.designer.framework.common.util.SessionUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import com.sys.designer.framework.web.util.ApiUtil;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
public class PermissionHandler implements ApplicationLifeCycleService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionHandler.class);

    private static ResourcePermissionService resourcePermissionService;
    private static SessionService sessionService;
    @Autowired(required = false)
    FunctionInterceptor functionInterceptor;

    @Resource
    private AesCryptoService aesCryptoService;

    @Resource
    private CommonConfig commonConfig;

    @Resource
    private Rsa2CryptoService rsa2CryptoService;


    @Override
    public void onReady() {
        try {
            resourcePermissionService = ComponentUtil.getBean(ResourcePermissionService.class);
        } catch (Exception e) {
            // ignore
        }

        try {
            sessionService = ComponentUtil.getBean(SessionService.class);
        } catch (Exception e) {
            // ignore
        }
    }

    @Pointcut("@annotation(com.sys.designer.framework.api.permission.Permission)")
    public void permissionPointCut() {

    }

    @Around("permissionPointCut()")
    public Object permissionCheck(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        Permission permission = method.getAnnotation(Permission.class);

        boolean isSse = PermissionConst.RESOURCE_SSE == permission.resourceType();
        if (isSse) {
            try {
                return processDoPermissionCheck(permission, proceedingJoinPoint);
            } catch (Exception e) {
                LOGGER.error("error", e);
                return ResponseEntity.badRequest().build();
            }
        }
        return processDoPermissionCheck(permission, proceedingJoinPoint);
    }

    private Object processDoPermissionCheck(Permission permission, ProceedingJoinPoint proceedingJoinPoint) {
        String token = ApiUtil.getToken();
        if (sessionService.isLogin(token)) {
            if (Objects.isNull(SessionUtil.getUserInfo())) {
                SessionUtil.setUserinfo(sessionService.getUserInfo(token));
            }
        }
        if (Objects.nonNull(functionInterceptor)) {
            functionInterceptor.before(permission.resourceId(), permission.resourceType());
        }
        boolean ret = doCheckPermission(permission) && PermissionUtil.checkCustomPermission(permission);
        if (!ret) {
            ret = PermissionUtil.checkPrivateToken(permission);
        }
        if (ret) {
            Object proceed = null;
            try {
                proceed = proceedingJoinPoint.proceed();
            } catch (Throwable e) {
                if (e instanceof ErrorCodeRuntimeException exception) {
                    throw exception;
                }
                throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, e);
            } finally {
                if (Objects.nonNull(functionInterceptor)) {
                    functionInterceptor.after(permission.resourceId(), permission.resourceType());
                }
            }
            return proceed;
        } else {
            throw new BusinessRuntimeException(CommonErrorCode.PERMISSION_DENIED);
        }
    }

    private boolean doCheckPermission(Permission permission) {
        String[] authorities = permission.authorities();
        boolean checkedLoginAuth = false;


        String token = ApiUtil.getToken();
        if (permission.resourceType() != PermissionConst.RESOURCE_TYPE_QUERY && permission.resourceType() != PermissionConst.RESOURCE_TYPE_ANY) {
            if (ValueUtil.isEmpty(token)) {
                return false;
            }
            UserBaseInfo userBaseInfo = sessionService.getUserInfo(token);
            if (Objects.nonNull(userBaseInfo) && UserType.EXAMPLE.equals(userBaseInfo.getUserType())) {
                return false;
            }
        }

        boolean isLogin = sessionService.isLogin(token);

        int count = 0;
        RoleType userRole = SessionUtil.getUserRole();
        for (String authority : authorities) {
            if (PermissionConst.AUTHORITY_LOGIN.equals(authority)) {
                if (ValueUtil.isEmpty(token)) {
                    return false;
                }
                if (!isLogin) {
                    throw new BusinessRuntimeException(CommonErrorCode.NOT_LOGIN);
                }
                checkedLoginAuth = true;
                count++;
            } else if (PermissionConst.AUTHORITY_UN_LOGIN.equals(authority)) {
                if (SessionUtil.userId() != null) {
                    return false;
                }
                checkedLoginAuth = true;
                count++;
            } else if (PermissionConst.PRIVATE_TOKEN.equals(authority)) {
                String privateToken = ApiUtil.getPrivateToken();
                if (ValueUtil.isEmpty(privateToken)) {
                    return false;
                }
                return PermissionUtil.checkPermission(permission);
            }
        }

        if (!checkedLoginAuth) {
            if (!isLogin) {
                throw new BusinessRuntimeException(CommonErrorCode.NOT_LOGIN);
            }
        }

        if (permission.authorities().length > count) {
            return PermissionUtil.checkPermission(permission);
        }

        return true;
    }
}
