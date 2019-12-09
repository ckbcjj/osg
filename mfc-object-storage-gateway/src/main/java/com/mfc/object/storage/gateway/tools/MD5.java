package com.mfc.object.storage.gateway.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    private static final Logger logger = LoggerFactory.getLogger(MD5.class);
    MessageDigest digest = null;

    public MD5() {
        try {
            this.digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException var2) {
            logger.error("MessageDigest", var2);
        }

    }

    public void update(String str) throws UnsupportedEncodingException {
        if (this.digest != null) {
            this.digest.update(str.getBytes("UTF-8"), 0, str.getBytes("UTF-8").length);
        }

    }

    public void update(byte[] bytes) {
        if (this.digest != null) {
            this.digest.update(bytes, 0, bytes.length);
        }

    }

    public String checksum() {
        return ByteHexUtils.byte2hex(this.digest.digest(), false);
    }

    public String base64Checksum() {
        return Base64Utils.encodeURLSafe(this.digest.digest());
    }

    public static final String MD5(String s) {
        try {
            return MD5(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalArgumentException(var2.getMessage());
        }
    }

    public static final String MD5AsBase64(String s) {
        try {
            return MD5AsBase64(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException var2) {
            throw new IllegalArgumentException(var2.getMessage());
        }
    }

    public static final String MD5(byte[] bytes) {
        MD5 md5 = new MD5();
        md5.update(bytes);
        return md5.checksum();
    }

    public static final String MD5AsBase64(byte[] bytes) {
        MD5 md5 = new MD5();
        md5.update(bytes);
        return md5.base64Checksum();
    }
}
