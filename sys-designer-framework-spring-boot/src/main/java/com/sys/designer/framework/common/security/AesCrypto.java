package com.sys.designer.framework.common.security;

import com.sys.designer.framework.api.security.AesCryptoService;
import com.sys.designer.framework.common.errorcode.CommonErrorCode;
import com.sys.designer.framework.common.exception.BusinessRuntimeException;
import com.sys.designer.framework.common.util.ValueUtil;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class AesCrypto implements AesCryptoService {
    @Override
    public String encrypt(String input, String key) {
        if (ValueUtil.isEmpty(input)) {
            return input;
        }
        if (ValueUtil.isEmpty(key)) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, "key must not be empty.");
        }
        try {
            String[] keys = key.split(":");
            IvParameterSpec iv = new IvParameterSpec(keys[0].getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(keys[1].getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, ex);
        }
    }

    @Override
    public String decrypt(String input, String key) {
        if (ValueUtil.isEmpty(input)) {
            return input;
        }
        if (ValueUtil.isEmpty(key)) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, "key must not be empty.");
        }
        try {
            String[] keys = key.split(":");
            IvParameterSpec iv = new IvParameterSpec(keys[0].getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(keys[1].getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(org.apache.commons.codec.binary.Base64.decodeBase64(input));

            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new BusinessRuntimeException(CommonErrorCode.SERVER_ERROR, ex);
        }
    }

    @Override
    public String generatorKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder key = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            key.append(chars.charAt(random.nextInt(chars.length())));
        }
        return generatorIv() + ":" + key;
    }

    public String generatorIv() {
        String chars = "0123456789abcdef";
        StringBuilder key = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 16; i++) {
            key.append(chars.charAt(random.nextInt(chars.length())));
        }
        return key.toString();
    }
}
