/**
 * Copyright (C) Oceancode Cloud. 2024-2024 .All Rights Reserved.
 */

package com.sys.designer.framework.common.util;

import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * <B>Md5Util</B>
 *
 * <p>
 * This class is a sha256 util.
 * </p>
 *
 * @author Dynamic Gen
 * @since 1.0
 */
public final class Md5Util {
    private static final String ALGORITHM = "SHA-256";

    private Md5Util() {
    }

    /**
     * get string sha256
     *
     * @param text raw string
     * @return sha256 string
     */
    public static String md5(String text) {
        if (ValueUtil.isEmpty(text)) {
            return null;
        }
        return digest(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * get stream sha256
     *
     * @param inputStream stream
     * @return sha256 string
     */
    public static String md5(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            return toHex(digest.digest());
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new BusinessRuntimeException(CommonErrorCode.ERROR, e);
        }
    }

    /**
     * get file sha256
     *
     * @param file file
     * @return sha256 string
     */
    public static String md5(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            return md5(in);
        } catch (Exception e) {
            throw new BusinessRuntimeException(CommonErrorCode.ERROR, e);
        }
    }

    private static String digest(byte[] data) {
        try {
            return toHex(MessageDigest.getInstance(ALGORITHM).digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessRuntimeException(CommonErrorCode.ERROR, e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}