/**
 * Copyright (C) Oceancode Cloud. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.common.util;

import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class SystemUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUtil.class);

    public static final String OUTPUT_DIR_CONFIG_KEY = "oc.system.output.dir";

    private static String getAppBinWorkDir() {
        return getAppBinWorkFile().getAbsolutePath() + File.separator;
    }

    public static String getAppHome() {
        return getAppBinWorkDir();
    }

    private static File getAppBinWorkFile() {
        String dir = System.getProperty("user.dir");
        File file = new File(dir, "bin/startup.sh");
        if (file.exists()) {
            file = file.getParentFile().getParentFile();
        }
        return file;
    }

    public static boolean isWindow() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static void init() {
        File dataDirFile = new File(dataDir());
        if (!dataDirFile.exists()) {
            dataDirFile.mkdirs();
        }

        File tempDirFile = new File(tempDir());
        if (!tempDirFile.exists()) {
            tempDirFile.mkdirs();
        }
    }

    public static String dataDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        String dataDir = environment.getProperty("oc.system.data.dir");
        if (ValueUtil.isEmpty(dataDir)) {
            dataDir = getAppBinWorkDir() + "data";
        }

        return dataDir;
    }

    public static String repositoryDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        String dataDir = environment.getProperty("oc.system.repository.dir");
        if (ValueUtil.isEmpty(dataDir)) {
            dataDir = dataDir();
        }

        return dataDir + "/repository";
    }

    public static String outputDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        String dataDir = environment.getProperty(OUTPUT_DIR_CONFIG_KEY);
        if (ValueUtil.isEmpty(dataDir)) {
            dataDir = getAppBinWorkDir() + "output";
        }

        return dataDir;
    }

    public static String tempDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        String tempDir = environment.getProperty("oc.tmp.dir");
        if (ValueUtil.isEmpty(tempDir)) {
            try {
                return Path.of(dataDir(), "./tmp").toFile().getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (tempDir.startsWith("../")) {
            try {
                return Path.of(dataDir(), tempDir).toFile().getCanonicalPath();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return tempDir;
    }

    public static boolean enableWeb() {
        String value = ComponentUtil.getBean(Environment.class).getProperty("oc.web.enabled", "false");
        return Boolean.parseBoolean(value);
    }

    public static String htmlDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        return parsePath(environment.getProperty("oc.web.html", "./data/web/html"));
    }

    public static String publicDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        return parsePath(environment.getProperty("oc.web.resource.public", "./data/web/public"));
    }

    public static String pluginDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        return parsePath(environment.getProperty("oc.plugin.dir", "../plugins"));
    }

    public static String privateResourceDir() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        return parsePath(environment.getProperty("oc.web.resource.private", "./data/web/private"));
    }

    public static String privateResourceUrlPrefix() {
        Environment environment = ComponentUtil.getBean(Environment.class);
        String url = environment.getProperty("oc.web.resource.private.url.prefix", "/s/");
        return ValueUtil.isNotEmpty(url) ? (url.endsWith("/") ? url : url + "/") : null;
    }

    public static String parsePath(String path) {
        if (path == null) {
            return null;
        }
        if (path.trim().startsWith(".")) {
            return getAppBinWorkDir() + path.trim();
        }
        return path.trim();
    }

    public static boolean killProcess(int processId) {
        String cmd = "kill -15 " + processId;
        if (isWindow()) {
            cmd = "taskkill /PID " + processId + " /F";
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(tokenize(cmd));
            builder.redirectErrorStream(true);
            Process killProcess = builder.start();
            return killProcess.waitFor() == 0;
        } catch (Exception e) {
            LOGGER.warn("killProcess failed, pid={}", processId, e);
            return false;
        }
    }

    /**
     * 执行系统命令。
     * 安全约束：禁止包含 shell 元字符（; | & $ ` > < 等），避免命令注入；
     * 不通过 shell 执行，命令按空白拆分为参数列表。
     */
    public static List<String> execCommand(String command, Function<String, Boolean> function) {
        List<String> list = new ArrayList<>();
        if (ValueUtil.isEmpty(command) || containsShellMeta(command)) {
            throw new BusinessRuntimeException(CommonErrorCode.PARAMETER_INVALID, "illegal command.");
        }
        try {
            ProcessBuilder builder = new ProcessBuilder(tokenize(command));
            // 合并标准错误到标准输出，避免子进程写 stderr 填满管道导致阻塞（死锁）
            builder.redirectErrorStream(true);
            Process child = builder.start();
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(child.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (Objects.nonNull(function) && !ValueUtil.isTrue(function.apply(line))) {
                        continue;
                    }
                    list.add(line);
                }
            }
            try {
                child.waitFor();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warn("execCommand interrupted: {}", command);
            }
        } catch (IOException e) {
            LOGGER.warn("execCommand failed: {}", command, e);
        }
        return list;
    }

    private static boolean containsShellMeta(String command) {
        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);
            if (c == ';' || c == '|' || c == '&' || c == '$' || c == '>'
                    || c == '<' || c == '`' || c == '\n' || c == '\r' || c == '\t') {
                return true;
            }
        }
        return false;
    }

    private static List<String> tokenize(String command) {
        List<String> tokens = new ArrayList<>();
        for (String token : command.trim().split("\\s+")) {
            if (ValueUtil.isNotEmpty(token)) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    public static String resolvePath(String path) {
        if (ValueUtil.isEmpty(path)) {
            return null;
        }
        String cur = path;
        if (cur.contains("..")) {
            cur = cur.replace("..", "");
        }
        while (Objects.nonNull(cur)) {
            boolean ret = cur.startsWith(".") || cur.startsWith("/")
                    || cur.startsWith("\\");
            if (ret) {
                cur = cur.substring(1);
                continue;
            }
            break;
        }
        return cur;
    }
}