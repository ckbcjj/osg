package com.mfc.object.storage.gateway.service;

import com.mfc.object.storage.gateway.configuration.ObjectStorageGatewayConfiguration;
import com.mfc.object.storage.gateway.controller.ObjectStorageGatewayHttpCode;
import com.mfc.object.storage.gateway.model.APIException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;


@Service
public class LocalStorageService {
    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);

    @Autowired
    private ObjectStorageGatewayConfiguration configuration;


    public byte[] downloadObject(String source, String key) throws Exception {
        String filepath = getStorageFilepath(source, key);
        File file = new File(filepath);
        try {
            return FileUtils.readFileToByteArray(file);
        } catch (Exception e) {
            logger.error("[LOCAL STORAGE]Download [{}] from [{}] local storage error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_LOCAL_STORAGE, ObjectStorageGatewayHttpCode.ERR_LOCAL_STORAGE_DOWNLOAD_ERROR, ObjectStorageGatewayHttpCode.ERR_LOCAL_STORAGE_DOWNLOAD_ERROR_MSG);
        }
    }


    public boolean uploadObject(String source, String key, File file) throws Exception {
        String filepath = getStorageFilepath(source, key);
        try {
            FileUtils.copyFile(file, new File(filepath));
            return true;
        } catch (Exception e) {
            logger.error("[LOCAL STORAGE]Upload [{}] to [{}] local storage error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_LOCAL_STORAGE, ObjectStorageGatewayHttpCode.ERR_LOCAL_STORAGE_UPLOAD_ERROR, ObjectStorageGatewayHttpCode.ERR_LOCAL_STORAGE_UPLOAD_ERROR_MSG);
        }
    }


    private String getStorageFilepath(String source, String key) {
        return configuration.getLocalFileBasePath() + File.separator + source + File.separator + key;
    }
}
