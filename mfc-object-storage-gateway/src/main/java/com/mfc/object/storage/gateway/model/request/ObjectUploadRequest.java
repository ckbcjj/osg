package com.mfc.object.storage.gateway.model.request;

public class ObjectUploadRequest extends BaseObjectRequest {
    private String appId;
    private String checksum;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
