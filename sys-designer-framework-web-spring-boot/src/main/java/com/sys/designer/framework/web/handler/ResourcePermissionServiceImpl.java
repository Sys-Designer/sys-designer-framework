package com.sys.designer.framework.web.handler;

import com.sys.designer.framework.api.permission.PermissionResourceService;
import com.sys.designer.framework.api.session.RoleType;
import com.sys.designer.framework.common.util.SessionUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

public class ResourcePermissionServiceImpl implements PermissionResourceService {

    @Override
    public boolean checkPermission(String resourceId, int resourceType, String authority) {
        RoleType userRole = SessionUtil.getUserRole();
        if (Objects.isNull(userRole)) {
            return false;
        }
        if (RoleType.CREATOR.equals(userRole)) {
            return RoleType.CREATOR.getValue().equals(authority) || RoleType.ADMIN.getValue().equals(authority) || RoleType.SUPER_ADMIN.getValue().equals(authority);
        }
        return userRole.getValue().equals(authority);
    }

    @Override
    public Set<String> getPermissions(String resourceId, int resourceType) {
        RoleType userRole = SessionUtil.getUserRole();
        if (Objects.nonNull(userRole)) {
            return Set.of(userRole.getValue());
        }
        return Set.of();
    }
}
