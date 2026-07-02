package com.sys.designer.framework.web;

import com.sys.designer.framework.autoconfig.AutoConfigRequest;
import com.sys.designer.framework.autoconfig.AutoConfigResponse;
import com.sys.designer.framework.api.permission.Permission;
import com.sys.designer.framework.api.permission.PermissionConst;
import com.sys.designer.framework.autoconfig.AutoConfigService;
import com.sys.designer.framework.common.constant.CommonConst;
import com.sys.designer.framework.common.entity.ResultData;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.ErrorCodeRuntimeException;
import com.sys.designer.framework.web.security.EncryptResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(CommonConst.API_PREFIX)
@ConditionalOnBean(AutoConfigService.class)
public class AutoConfigController {
    private final static Logger LOGGER = LoggerFactory.getLogger(AutoConfigController.class);
    private AutoConfigService autoConfigService;

    public AutoConfigController(AutoConfigService autoConfigService) {
        this.autoConfigService = autoConfigService;
    }

    @Permission(resourceId = "autoConfig", authorities = {PermissionConst.AUTHORITY_LOGIN})
    @PostMapping("/autoConfig")
    @EncryptResponse
    public ResultData<?> autoConfig(@RequestBody AutoConfigRequest autoConfigRequest) {
        try {
            AutoConfigResponse result = autoConfigService.autoConfig(autoConfigRequest);
            return ResultData.isOk(result);
        } catch (Exception e) {
            if (e instanceof ErrorCodeRuntimeException ex) {
                ResultData<Object> resultData = ResultData.isFail();
                resultData.setCode(ex.getErrorCode());
                resultData.setMessage(ex.getMessage());
                if (!ex.isClientError()) {
                    LOGGER.error("error", e);
                    resultData.setCode(CommonErrorCode.SERVER_ERROR.getCode());
                    resultData.setMessage(CommonErrorCode.SERVER_ERROR.getMessage());
                }
                return resultData;
            }
        }
        return ResultData.isFail();
    }
}
