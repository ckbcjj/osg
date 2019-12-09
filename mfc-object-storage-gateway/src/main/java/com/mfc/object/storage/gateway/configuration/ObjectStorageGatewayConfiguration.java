package com.mfc.object.storage.gateway.configuration;

import com.mfc.config.ConfigAESTools;
import com.mfc.object.storage.gateway.tools.StringSizeUtils;
import org.springframework.stereotype.Service;

import com.mfc.config.ConfigTools3;

@Service
public class ObjectStorageGatewayConfiguration {

    private static final String AES_KEY = "Ebw6MyVpHAC23ZMszboDDQ";

    public static final String STORAGE_SOURCE_PREFIX = "mfc.osg.source";

    public static final String STORAGE_ACCESSKEYID_PREFIX = "mfc.osg.accesskeyId";
    public static final String STORAGE_SECRETACCESSKEY_PREFIX = "mfc.osg.accesskey";
    public static final String STORAGE_STORAGE_REGION_PREFIX = "mfc.osg.region";
    public static final String STORAGE_BUCKETNAME_PREFIX = "mfc.osg.bucket";
    public static final String STORAGE_STORAGE_CLASS_PREFIX = "mfc.osg.storageclass";

    public static final String STOAGE_OSS_BASE_URL_PREFIX = "mfc.osg.oss.baseUrl";
    public static final String STORAGE_OSS_IMAGE_RESIZE = "mfc.osg.oss.image.resize";


    public String getDownloadCachePath() {
        return ConfigTools3.getConfigAsString("mfc.osg.download.cache.path");
    }

    public String getUploadCachePath() {
        return ConfigTools3.getConfigAsString("mfc.osg.upload.cache.path");
    }

    public String getObjectCacheCleanSchedulerStr() {
        return ConfigTools3.getConfigAsString("mfc.osg.cache.clean.scheduler.str");
    }

    public long getObjectCacheMaxSize() {
        return StringSizeUtils.parseSizeString(ConfigTools3.getConfigAsString("mfc.osg.cache.max.size.str"));
    }

    public long getObjectCacheMaxKeepHours() {
        return ConfigTools3.getConfigAsLong("mfc.osg.cache.max.keep.hour");
    }

    public long getUploadMultipartSize() {
        return ConfigTools3.getConfigAsLong("mfc.osg.upload.multipart.size", 10 * 1024 * 1024L);
    }

    public String getDefaultImageSource() {
        return ConfigTools3.getConfigAsString("mfc.osg.image.default.source", "OSS");
    }

    public String getStorageSource(String source) {
        String key = String.format("%s.%s", STORAGE_SOURCE_PREFIX, source);
        return ConfigTools3.getConfigAsString(key);
    }

    public String getOssBaseUrl(String source) {
        String key = String.format("%s.%s", STOAGE_OSS_BASE_URL_PREFIX, source);
        return ConfigTools3.getConfigAsString(key);
    }

    public String getLocalFileBasePath() {
        return ConfigTools3.getConfigAsString("mfc.osg.localfile.base.path");
    }

    public String getAccessKeyId(String source) {
        String key = String.format("%s.%s", STORAGE_ACCESSKEYID_PREFIX, source);
        return ConfigAESTools.getConfigAsString(key, AES_KEY);
    }

    public String getSecretAccessKey(String source) {
        String key = String.format("%s.%s", STORAGE_SECRETACCESSKEY_PREFIX, source);
        return ConfigAESTools.getConfigAsString(key, AES_KEY);
    }


    public String getBucketName(String source) {
        String key = String.format("%s.%s", STORAGE_BUCKETNAME_PREFIX, source);
        return ConfigTools3.getConfigAsString(key);
    }

    public String getRegion(String source) {
        String key = String.format("%s.%s", STORAGE_STORAGE_REGION_PREFIX, source);
        return ConfigTools3.getConfigAsString(key);
    }

    public String getStorageClass(String source) {
        String key = String.format("%s.%s", STORAGE_STORAGE_CLASS_PREFIX, source);
        return ConfigTools3.getConfigAsString(key);
    }

    public boolean useOssResize(String source) {
        String key = String.format("%s.%s", STORAGE_OSS_IMAGE_RESIZE, source);
        return ConfigTools3.getAsBoolean(key, false);
    }


}
