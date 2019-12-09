package com.mfc.object.storage.gateway.model;

public enum ResizeMode {
    LFIT("lfit"),
    MFIT("mfit"),
    FILL("fill"),
    PAD("pad"),
    FIXED("fixed");

    private String mode;

    public String getMode() {
        return mode;
    }

    ResizeMode(String mode) {
        this.mode = mode;
    }

    public static ResizeMode fromString(String modelName) {
        try {
            return ResizeMode.valueOf(modelName);
        } catch (IllegalArgumentException ex) {
            return LFIT;
        }
    }
}
