package com.mfc.object.storage.gateway.tools;

public class ByteHexUtils {
    public ByteHexUtils() {
    }

    public static String byte2hex(byte[] b, boolean uppercase) {
        String hs = "";
        String stmp = "";

        for(int n = 0; n < b.length; ++n) {
            stmp = Integer.toHexString(b[n] & 255);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }

            if (n < b.length - 1) {
                hs = hs + "";
            }
        }

        if (uppercase) {
            return hs.toUpperCase();
        } else {
            return hs.toLowerCase();
        }
    }

    public static byte[] hex2byte(String hex) throws IllegalArgumentException {
        if (hex != null && !hex.isEmpty()) {
            if (hex.length() % 2 != 0) {
                throw new IllegalArgumentException();
            } else {
                char[] arr = hex.toCharArray();
                byte[] b = new byte[hex.length() / 2];
                int i = 0;
                int j = 0;

                for(int l = hex.length(); i < l; ++j) {
                    String swap = "" + arr[i++] + arr[i];
                    int byteint = Integer.parseInt(swap, 16) & 255;
                    b[j] = (new Integer(byteint)).byteValue();
                    ++i;
                }

                return b;
            }
        } else {
            throw new IllegalArgumentException("content is empty");
        }
    }
}
