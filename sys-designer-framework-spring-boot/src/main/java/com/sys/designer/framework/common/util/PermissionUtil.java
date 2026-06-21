package com.sys.designer.framework.common.util;

import com.sys.designer.framework.api.permission.Permission;
import com.sys.designer.framework.api.permission.PermissionConst;
import com.sys.designer.framework.api.permission.PermissionResourceService;
import com.sys.designer.framework.common.config.CommonConfig;
import com.sys.designer.framework.common.config.Config;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;

import java.util.Objects;

public final class PermissionUtil {
    private static CommonConfig commonConfig;
    private static PermissionResourceService permissionResourceService;

    private PermissionUtil() {

    }

    static {
        commonConfig = ComponentUtil.getBean(CommonConfig.class);
        try {
            permissionResourceService = ComponentUtil.getBean(PermissionResourceService.class);
        } catch (Exception e) {
            // ignore
        }
    }

    public static String[] getAuthorities(String resourceId) {
        String str = commonConfig.getValue(Config.Permission.PREFIX + resourceId + "." + Config.Permission.AUTHORITIES);
        if (str == null) {
            return null;
        }
        return str.split(",");
    }

    public static String getOperator(String resourceId) {
        return commonConfig.getValue(Config.Permission.PREFIX + resourceId + "." + Config.Permission.OPERATOR, PermissionConst.OPERATION_OR);
    }

    public static boolean checkPrivateToken(Permission permission) {
        if (Objects.isNull(permissionResourceService)) {
            return false;
        }
        return permissionResourceService.checkPermission(permission.resourceId(), permission.resourceType(), PermissionConst.PRIVATE_TOKEN);
    }

    public static boolean checkPrivateToken(String resourceId, int resourceType) {
        if (Objects.isNull(permissionResourceService)) {
            return false;
        }
        return permissionResourceService.checkPermission(resourceId, resourceType, PermissionConst.PRIVATE_TOKEN);
    }

    public static boolean checkCustomPermission(Permission permission) {
        if (Objects.isNull(permissionResourceService)) {
            return true;
        }
        return permissionResourceService.checkPermission(permission.resourceId(), permission.resourceType(), PermissionConst.CUSTOM_PERMISSION);
    }

    public static boolean checkPermission(Permission permission) {
        boolean isAnd = PermissionConst.OPERATION_OR.equals(getOperator(permission.resourceId())) || PermissionConst.OPERATION_OR.equals(permission.operation());
        String[] authorities = permission.authorities();
        if (authorities.length == 0) {
            authorities = getAuthorities(permission.resourceId());
        }

        int matchCount = 0;
        for (String authority : authorities) {
            if (PermissionConst.AUTHORITY_LOGIN.equals(authority) ||
                    PermissionConst.AUTHORITY_UN_LOGIN.equals(authority)) {
                matchCount++;
                continue;
            }
            if (Objects.isNull(permissionResourceService)) {
                return false;
            }
            if (permissionResourceService.checkPermission(permission.resourceId(), permission.resourceType(), authority)) {
                matchCount++;
                if (!isAnd) {
                    return true;
                }
            }
        }
        return matchCount == authorities.length;
    }

    public static PermissionResourceService getPermissionResourceService() {
        return permissionResourceService;
    }
}
