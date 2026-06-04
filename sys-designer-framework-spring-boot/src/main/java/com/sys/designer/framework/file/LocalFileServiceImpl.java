package com.sys.designer.framework.file;

import com.sys.designer.framework.api.file.FileInfo;
import com.sys.designer.framework.api.file.FileService;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.util.SystemUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class LocalFileServiceImpl implements FileService {
    @Override
    public void save(FileInfo fileInfo) {
        if (ValueUtil.isEmpty(fileInfo.getId())) {
            fileInfo.setId(UUID.randomUUID().toString().replace("-", ""));
        }

        try {
            String suffix = ValueUtil.isNotEmpty(fileInfo.getSuffix()) ? fileInfo.getSuffix() : "";
            Path path = Path.of(SystemUtil.dataDir(), fileInfo.getGroup(), fileInfo.getId() + suffix);
            if (!path.getParent().toFile().exists()) {
                path.getParent().toFile().mkdirs();
            }
            FileCopyUtils.copy(fileInfo.getInputStream(), Files.newOutputStream(path));
            fileInfo.setUri("file://local/" + fileInfo.getId() + suffix);
        } catch (IOException e) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, e);
        }
    }
}
