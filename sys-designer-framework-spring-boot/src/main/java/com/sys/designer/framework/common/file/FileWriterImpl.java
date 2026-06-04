package com.sys.designer.framework.common.file;

import com.sys.designer.framework.api.file.Column;
import com.sys.designer.framework.api.file.DataRow;
import com.sys.designer.framework.api.file.FileWriter;
import com.sys.designer.framework.api.file.FileWriterContext;
import com.sys.designer.framework.api.file.WriteCallback;
import com.sys.designer.framework.common.util.FileUtil;
import com.sys.designer.framework.common.util.ValueUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class FileWriterImpl implements FileWriter {

    @Override
    public <T extends FileWriterContext> void write(T context, WriteCallback callback) {
        String filePath = context.getFilePath().trim();
        if (!filePath.toLowerCase().endsWith(".csv")) {
            return;
        }
        List<String> columns = new ArrayList<>();
        StringBuilder headerStr = new StringBuilder();
        if (ValueUtil.isNotEmpty(context.getColumns())) {
            for (Column column : context.getColumns()) {
                columns.add(column.getKey());
                headerStr.append(column.getTitle()).append("(").append(column.getKey()).append(")");
            }
        }
        File file = new File(filePath);
        if (ValueUtil.isNotEmpty(columns)) {
            FileUtil.writeStringToFile(file, headerStr.toString());
        }
        List<DataRow> rows = callback.getRows();

        while (ValueUtil.isNotEmpty(rows)) {
            for (DataRow row : rows) {
                String line = row.getText();
                if (Objects.nonNull(line)) {
                    FileUtil.writeStringToFile(file, line + "\n", true);
                    continue;
                }
                if (columns.isEmpty()) {
                    columns.addAll(row.getValues().get(0).keySet());
                }

                if (ValueUtil.isEmpty(row.getValues())) {
                    continue;
                }

                for (Map<String, Object> value : row.getValues()) {
                    line = processWriteFile(columns, value);
                    FileUtil.writeStringToFile(file, line, true);
                }
            }
        }
    }

    private String processWriteFile(List<String> columns, Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        for (String column : columns) {
            Object value = map.get(column);
            if (Objects.isNull(value)) {
                value = "";
            }
            sb.append(value).append(",");
        }
        sb.append("\n");
        return sb.toString();
    }
}
