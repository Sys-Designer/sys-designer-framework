package com.sys.designer.framework.api.file;

public interface FileWriter {
    <T extends FileWriterContext> void write(T context, WriteCallback callback);
}
