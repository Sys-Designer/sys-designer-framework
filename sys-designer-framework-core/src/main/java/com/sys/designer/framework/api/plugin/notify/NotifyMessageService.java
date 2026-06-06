package com.sys.designer.framework.api.plugin.notify;

public interface NotifyMessageService {
    NotifyResult send(NotifyParam param);

    NotifyCode getCode(NotifyBaseParam param);

    void clearCode(NotifyBaseParam param);
}
