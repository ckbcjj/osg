package com.mfc.object.storage.gateway.service;

import com.mfc.object.storage.gateway.configuration.ObjectStorageGatewayConfiguration;
import com.mfc.object.storage.gateway.controller.ObjectStorageGatewayHttpCode;
import com.mfc.object.storage.gateway.model.APIException;
import com.mfc.object.storage.gateway.model.StorageSource;
import com.mfc.object.storage.gateway.model.request.ObjectGetRequest;
import com.mfc.object.storage.gateway.model.request.ObjectUploadRequest;
import com.mfc.object.storage.gateway.model.response.ObjectUploadResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ObjectStorageGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(ObjectStorageGatewayService.class);

    @Autowired
    private ObjectStorageGatewayConfiguration objectGatewayConfiguration;

    @Autowired
    private AliyunOSSService aliyunOSSService;
    @Autowired
    private AWSS3Service awss3Service;
    @Autowired
    private LocalStorageService localStorageService;


    private Map<String, Boolean> fileLock = new ConcurrentHashMap<>();

    @Autowired
    private ObjectStorageCacheService objectCacheService;

    public String getObject(ObjectGetRequest objectGetRequest) throws Exception {
        String storageSource = objectGatewayConfiguration.getStorageSource(objectGetRequest.getSource());
        String filepath = objectCacheService.getObject(objectGetRequest.getSource(), objectGetRequest.getObjId());
        if (!StringUtils.isEmpty(filepath))
            return filepath;

        String lockKey = getLockKey(objectGetRequest.getSource(), objectGetRequest.getObjId());
        try {
            fileLock.putIfAbsent(lockKey, true);
            synchronized (fileLock.get(lockKey)) {
                filepath = objectCacheService.getObject(objectGetRequest.getSource(), objectGetRequest.getObjId());
                if (!StringUtils.isEmpty(filepath))
                    return filepath;

                byte[] content = null;
                if (storageSource.equalsIgnoreCase(StorageSource.OSS.name())) {
                    content = aliyunOSSService.downloadObject(objectGetRequest.getSource(), objectGetRequest.getObjId());
                } else if (storageSource.equalsIgnoreCase(StorageSource.S3.name())) {
                    content = awss3Service.downloadObject(objectGetRequest.getSource(), objectGetRequest.getObjId());
                } else if (storageSource.equalsIgnoreCase(StorageSource.LOCAL.name())) {
                    content = localStorageService.downloadObject(objectGetRequest.getSource(), objectGetRequest.getObjId());
                } else
                    throw new APIException(ObjectStorageGatewayHttpCode.RET_DOWNLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_UNKNOWN_SOURCE, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_UNKNOWN_SOURCE_MSG);

                return objectCacheService.cacheObject(objectGetRequest.getSource(), objectGetRequest.getObjId(), content);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            fileLock.remove(lockKey);
        }
    }


    //todo 未来可以通过checksum支持快速上传
    public ObjectUploadResponse uploadObject(MultipartFile sourceFile, ObjectUploadRequest objectUploadRequest) throws Exception {
        String storageSource = objectGatewayConfiguration.getStorageSource(objectUploadRequest.getSource());

        String fileMD5 = DigestUtils.md5Hex(sourceFile.getInputStream());
        if (!StringUtils.isEmpty(objectUploadRequest.getChecksum()) && !objectUploadRequest.getChecksum().equalsIgnoreCase(fileMD5)) {
            throw new APIException(ObjectStorageGatewayHttpCode.RET_UPLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_INVALID_FILE, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_INVALID_FILE_MSG);
        }

        String lockKey = getLockKey(objectUploadRequest.getSource(), objectUploadRequest.getObjId());
        String fileExtension = FilenameUtils.getExtension(sourceFile.getOriginalFilename());
        File uploadFile = new File(objectGatewayConfiguration.getUploadCachePath() + File.separator + fileMD5 + "." + fileExtension);

        try {
            fileLock.putIfAbsent(lockKey, true);
            synchronized (fileLock.get(lockKey)) {
                if (uploadFile.exists()) {
                    if (uploadFile.length() != sourceFile.getSize()) {
                        FileUtils.deleteQuietly(uploadFile);
                        FileUtils.copyInputStreamToFile(sourceFile.getInputStream(), uploadFile);
                    } else
                        logger.info("upload [{}] [{}] file already exists.md5:[{}]", objectUploadRequest.getSource(), objectUploadRequest.getObjId(), fileMD5);
                } else
                    FileUtils.copyInputStreamToFile(sourceFile.getInputStream(), uploadFile);

                boolean ret = false;
                if (storageSource.equalsIgnoreCase(StorageSource.OSS.name())) {
                    ret = aliyunOSSService.uploadObject(objectUploadRequest.getSource(), objectUploadRequest.getObjId(), uploadFile);
                } else if (storageSource.equalsIgnoreCase(StorageSource.S3.name())) {
                    ret = awss3Service.uploadObject(objectUploadRequest.getSource(), objectUploadRequest.getObjId(), uploadFile);
                } else if (storageSource.equalsIgnoreCase(StorageSource.LOCAL.name())) {
                    ret = localStorageService.uploadObject(objectUploadRequest.getSource(), objectUploadRequest.getObjId(), uploadFile);
                } else
                    throw new APIException(ObjectStorageGatewayHttpCode.RET_UPLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_UPLOAD_UNKNOWN_SOURCE, ObjectStorageGatewayHttpCode.ERR_UPLOAD_UNKNOWN_SOURCE_MSG);

                if (ret) {
                    ObjectUploadResponse objectUploadResponse = new ObjectUploadResponse();
                    objectUploadResponse.setObjId(objectUploadRequest.getObjId());
                    objectUploadResponse.setSource(objectUploadRequest.getSource());

                    return objectUploadResponse;
                } else
                    throw new APIException(ObjectStorageGatewayHttpCode.RET_UPLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_UPLOAD_UNKNOWN_FAILED, ObjectStorageGatewayHttpCode.ERR_UPLOAD_UNKNOWN_FAILED_MSG);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            FileUtils.deleteQuietly(uploadFile);
            fileLock.remove(lockKey);
        }


    }

    private String getLockKey(String source, String key) {
        return source + "@" + key;
    }
}
