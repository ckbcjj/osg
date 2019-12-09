package com.mfc.object.storage.gateway.tools;

import org.apache.commons.lang3.StringUtils;

public class StringSizeUtils {
    public static long parseSizeString(String sizeString) {
        if (StringUtils.isNumeric(sizeString)) {
            return Long.parseLong(sizeString);
        }
        if (StringUtils.endsWithIgnoreCase(sizeString, "k")) {
            String cutStr = StringUtils.substring(sizeString, 0, sizeString.length() - 1);
            return Long.parseLong(cutStr) * 1024;
        } else if (StringUtils.endsWithIgnoreCase(sizeString, "m")) {
            String cutStr = StringUtils.substring(sizeString, 0, sizeString.length() - 1);
            return Long.parseLong(cutStr) * 1024 * 1024;
        } else if (StringUtils.endsWithIgnoreCase(sizeString, "g")) {
            String cutStr = StringUtils.substring(sizeString, 0, sizeString.length() - 1);
            return Long.parseLong(cutStr) * 1024 * 1024 * 1024;
        } else if (StringUtils.endsWithIgnoreCase(sizeString, "t")) {
            String cutStr = StringUtils.substring(sizeString, 0, sizeString.length() - 1);
            return Long.parseLong(cutStr) * 1024 * 1024 * 1024 * 1024;
        } else {
            throw new IllegalArgumentException("Invalid size string " + sizeString);
        }
    }
}
