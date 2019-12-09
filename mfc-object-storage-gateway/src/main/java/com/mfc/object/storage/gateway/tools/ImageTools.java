package com.mfc.object.storage.gateway.tools;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class ImageTools {

    public static String colorToHexStr(Color color) {
        return intToHexStr(color.getRed()) + intToHexStr(color.getGreen()) + intToHexStr(color.getBlue());
    }

    public static String colorToHexStrWithAlpha(Color color) {
        return intToHexStr(color.getAlpha()) + colorToHexStr(color);
    }

    public static String intToHexStr(int number) {
        String result = Integer.toHexString(number & 0xff);
        while (result.length() < 2) {
            result = "0" + result;
        }
        return result.toUpperCase();
    }

    public static Color colorFromHexStr(String colorStr) {
        if (StringUtils.isEmpty(colorStr))
            return Color.WHITE;
        colorStr = colorStr.toUpperCase();
        if (colorStr.length() == 6) {
            int red = Integer.parseInt(colorStr.substring(0, 2), 16);
            int green = Integer.parseInt(colorStr.substring(2, 4), 16);
            int blue = Integer.parseInt(colorStr.substring(4, 6), 16);
            return new Color(red, green, blue);
        } else if (colorStr.length() == 8) {
            int alpha = Integer.parseInt(colorStr.substring(0, 2), 16);
            int red = Integer.parseInt(colorStr.substring(2, 4), 16);
            int green = Integer.parseInt(colorStr.substring(4, 6), 16);
            int blue = Integer.parseInt(colorStr.substring(6, 8), 16);
            return new Color(red, green, blue, alpha);
        } else
            return Color.WHITE;


    }
}
