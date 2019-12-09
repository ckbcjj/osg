package com.mfc.object.storage.gateway;

import com.mfc.object.storage.gateway.tools.ImageTools;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import java.awt.*;

public class ImageToolsTest {

    @Test
    public void testGetColorHexStr() {
        System.out.println("Color string without alpha:" + ImageTools.colorToHexStr(Color.GREEN));
        System.out.println("Color string with  alpha:" + ImageTools.colorToHexStrWithAlpha(Color.GREEN));
    }

    @Test
    public void testFilenameUtils(){
        String filename ="/std/a/b/c/asdsdfd.jpg";
        System.out.println(FilenameUtils.getExtension(filename));
        System.out.println(FilenameUtils.removeExtension(filename));
    }

}
