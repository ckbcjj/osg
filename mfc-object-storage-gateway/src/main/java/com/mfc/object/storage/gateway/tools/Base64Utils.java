package com.mfc.object.storage.gateway.tools;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Utils {
    public Base64Utils() {
    }

    public static String encode(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    public static String encodeURLSafe(String data) {
        try {
            return Base64.encodeBase64URLSafeString(data.getBytes("utf-8"));
        } catch (UnsupportedEncodingException var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public static String encodeURLSafe(byte[] data) {
        return Base64.encodeBase64URLSafeString(data);
    }

    public static String decode(String data) {
        try {
            return new String(Base64.decodeBase64(data), "utf-8");
        } catch (UnsupportedEncodingException var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public static byte[] decodeAsByteArray(String data) {
        return Base64.decodeBase64(data);
    }
}
