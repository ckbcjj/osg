package com.mfc.object.storage.gateway;

import com.mfc.object.storage.gateway.tools.ImageTools;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Canvas;
import net.coobird.thumbnailator.geometry.Positions;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ThumbnailTest {

    private String orignalImagePath = "doc/example.jpg";

    @Test
    public void getImageSize() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        System.out.println(bufferedImage.getType());
        System.out.println("Width:" + bufferedImage.getWidth());
        System.out.println("Height:" + bufferedImage.getHeight());
    }

    @Test
    public void resize01() throws IOException {
        //填充，使用Canvas filter
        Thumbnails.of(orignalImagePath).size(8000, 8000).addFilter(new Canvas(8000, 8000, Positions.CENTER, ImageTools.colorFromHexStr("ff00ff00"))).outputFormat("jpg").toFile(new File("doc/resize01"));
    }

    //单边缩略--按高度,将图缩略成高度为 100，宽度按比例处理。
    @Test
    public void resize02() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        int targetHeight = 100;
        double hp = (double) targetHeight / bufferedImage.getHeight();
        Thumbnails.of(bufferedImage).scale(hp).outputFormat("jpg").toFile("doc/resize02-1");
        //humbnails.of(bufferedImage).size(Double.valueOf(bufferedImage.getWidth() * hp).intValue(), targetHeight).outputFormat("jpg").toFile("doc/resize02-2");

    }

    //单边缩略--按宽度
    @Test
    public void resize03() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        int targetWidth = 100;
        double wp = (double) targetWidth / bufferedImage.getHeight();
        Thumbnails.of(bufferedImage).scale(wp).outputFormat("jpg").toFile("doc/resize03-1");
        //Thumbnails.of(bufferedImage).size(targetWidth, Double.valueOf(bufferedImage.getHeight() * wp).intValue()).outputFormat("jpg").toFile("doc/resize03-2");

    }


    //图片缩放（长边优先） 将图最长边限制在 100 像素，短边按比例处理。
    //关于长短边：“长边”是指原尺寸与目标尺寸的比值大的那条边，“短边”同理。
    // 例如，原图400x200，缩放为 800x100，由于 400/800=0.5，200/100=2，0.5 < 2，所以在这个缩放中 200 那条是长边，400 那条是短边。
    @Test
    public void resize04() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));

        int longSize = 100;

        int sourceWidth = bufferedImage.getWidth();
        int sourceHeight = bufferedImage.getHeight();
        double scale = 1;
        if (sourceWidth > sourceHeight) {
            scale = (double) longSize / sourceWidth;
        } else {
            scale = (double) longSize / sourceHeight;
        }
        Thumbnails.of(bufferedImage).scale(scale).outputFormat("jpg").toFile("doc/resize04");
    }


    //强制宽高缩略：将图强制缩略成宽度为 100，高度为 100。
    @Test
    public void resize05() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        int targetWidth = 100;
        int targetHeight = 100;

        double widthScale = (double) targetWidth / bufferedImage.getWidth();
        double heightScale = (double) targetHeight / bufferedImage.getHeight();

        Thumbnails.of(bufferedImage).scale(widthScale, heightScale).outputFormat("jpg").toFile("doc/resize05");
    }


    //等比缩放，限定在矩形框内
    //将图缩略成宽度为 100，高度为 100，按长边优先。
    @Test
    public void resize06() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        int targetWidth = 100;
        int targetHeight = 100;

        int sourceWidth = bufferedImage.getWidth();
        int sourceHeight = bufferedImage.getHeight();
        double scale = 1;
        if (sourceWidth > sourceHeight) {
            scale = (double) targetWidth / sourceWidth;
        } else {
            scale = (double) targetHeight / sourceHeight;
        }

        Thumbnails.of(bufferedImage).scale(scale).outputFormat("jpg").toFile("doc/resize06");
    }

    //等比缩放，限定在矩形框外
    //将图缩略成宽度为 100，高度为 100，按短边优先。
    @Test
    public void resize07() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        int targetWidth = 100;
        int targetHeight = 100;

        int sourceWidth = bufferedImage.getWidth();
        int sourceHeight = bufferedImage.getHeight();

        double scale = 1;
        if (sourceWidth < sourceHeight) {
            scale = (double) targetWidth / sourceWidth;
        } else {
            scale = (double) targetHeight / sourceHeight;
        }

        Thumbnails.of(bufferedImage).scale(scale).outputFormat("jpg").toFile("doc/resize07");
    }


    //固定宽高，自动裁剪
    //将图自动裁剪成宽度为 100，高度为 100 的效果图。
    @Test
    public void resize08() throws IOException {
        //先按短边优先缩放
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        int targetWidth = 100;
        int targetHeight = 100;

        int sourceWidth = bufferedImage.getWidth();
        int sourceHeight = bufferedImage.getHeight();

        double wp = (double) targetWidth / sourceWidth;
        double hp = (double) targetHeight / sourceHeight;

        BufferedImage image = null;
        if (wp > 1 || hp > 1)
            image = bufferedImage;

        else {
            if (wp > hp) {
                image = Thumbnails.of(bufferedImage).height(targetHeight).asBufferedImage();
            } else {
                image = Thumbnails.of(bufferedImage).height(targetWidth).asBufferedImage();
            }
        }

        Thumbnails.of(image).sourceRegion(Positions.CENTER, 100, 100).size(100, 100).outputFormat("jpg").toFile("doc/resize08");
    }


    //固定宽高，缩略填充
    //将原图指定按短边缩略 100x100，剩余的部分以白色填充。
    @Test
    public void resize09() throws IOException {
        //先按短边优先缩放
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        int targetWidth = 100;
        int targetHeight = 100;

        int sourceWidth = bufferedImage.getWidth();
        int sourceHeight = bufferedImage.getHeight();

        double wp = (double) targetWidth / sourceWidth;
        double hp = (double) targetHeight / sourceHeight;

        Thumbnails.Builder builder = null;
        if (wp > 1 || hp > 1)
            builder = Thumbnails.of(bufferedImage).scale(1);

        else {
            if (wp > hp) {
                builder = Thumbnails.of(bufferedImage).scale(hp);
            } else {
                builder = Thumbnails.of(bufferedImage).scale(wp);
            }
        }

        builder.addFilter(new Canvas(100, 100, Positions.CENTER, ImageTools.colorFromHexStr("FF0000"))).outputFormat("jpg").toFile(new File("doc/resize09"));
    }

    //将图按比例缩略到原来的 1/2。
    @Test
    public void resize10() throws IOException {
        BufferedImage bufferedImage = ImageIO.read(new File(orignalImagePath));
        Thumbnails.of(bufferedImage).scale(0.5).outputFormat("jpg").toFile("doc/resize10");
    }
}
