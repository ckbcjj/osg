package com.mfc.object.storage.gateway.model.request;

public class ImageResizeRequest extends BaseImageRequest{

    //缩略的模式   lfit：等比缩放，限制在指定w与h的矩形内的最大图片。
    //mfit：等比缩放，延伸出指定w与h的矩形框外的最小图片。
    //fill：固定宽高，将延伸出指定w与h的矩形框外的最小图片进行居中裁剪。
    //pad：固定宽高，缩略填充。
    //fixed：固定宽高，强制缩略。
    private String mode ;

    //目标缩略图的宽度。
    private int width = -1;
    //目标缩略图的高度。
    private int height = -1;
    //目标缩略图的最长边。
    private int longSize = -1;
    //目标缩略图的最短边。
    private int shortSize = -1;

    //指定当目标缩略图大于原图时是否处理。值是 true 表示不处理；值是 false 表示处理。
    boolean limit = true;

    //当缩放模式选择为 pad（缩略填充）时，可以选择填充的颜色(默认是白色)参数的填写方式：采用 16 进制颜色码表示，如 00FF00（绿色）
    private String color = "FFFFFF";

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getLongSize() {
        return longSize;
    }

    public void setLongSize(int longSize) {
        this.longSize = longSize;
    }

    public int getShortSize() {
        return shortSize;
    }

    public void setShortSize(int shortSize) {
        this.shortSize = shortSize;
    }

    public boolean isLimit() {
        return limit;
    }

    public void setLimit(boolean limit) {
        this.limit = limit;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
