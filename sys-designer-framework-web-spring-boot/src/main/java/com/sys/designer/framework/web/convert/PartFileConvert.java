package com.sys.designer.framework.web.convert;

import com.sys.designer.framework.common.entity.PartFile;
import com.sys.designer.framework.common.util.FileUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.web.multipart.MultipartFile;

public class PartFileConvert implements Converter<MultipartFile, PartFile> {
    @Override
    public PartFile convert(MultipartFile source) {
        return FileUtil.getPartFile(source);
    }
}
