package com.mfc.object.storage.gateway.model.request;

public class ObjectGetRequest extends BaseObjectRequest {
    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }
}
