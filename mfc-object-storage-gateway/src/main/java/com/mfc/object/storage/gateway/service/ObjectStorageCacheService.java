package com.mfc.object.storage.gateway.service;

import com.mfc.object.storage.gateway.controller.ObjectStorageGatewayHttpCode;
import com.mfc.object.storage.gateway.configuration.ObjectStorageGatewayConfiguration;
import com.mfc.object.storage.gateway.model.APIException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ObjectStorageCacheService {
    private static Logger logger = LoggerFactory.getLogger(ObjectStorageCacheService.class);

    private static final String DOWNLOAD_TEMP_SUFFIX = ".tmp";
    private Map<String, Boolean> cachedObjectMap = new ConcurrentHashMap<>();
    private Map<String, Long> cachedObjectAccessMap = new ConcurrentHashMap<>();


    @Autowired
    private ObjectStorageGatewayConfiguration objectGatewayConfiguration;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;


    @PostConstruct
    private void init() {
        cleanUncompleteUploadObjects();
        loadCachedObjects();

        //定时清理不必要的缓存
        threadPoolTaskScheduler.schedule(() -> {
            cleanCache();
        }, new CronTrigger(objectGatewayConfiguration.getObjectCacheCleanSchedulerStr()));
    }

    private void cleanUncompleteUploadObjects() {
        File uploadDir = new File(objectGatewayConfiguration.getUploadCachePath());
        if (!uploadDir.exists())
            uploadDir.mkdir();
        for (File file : FileUtils.listFiles(uploadDir, null, true)) {
            logger.info("Clean uncomplete [{}] object.", file.getAbsolutePath());
            FileUtils.deleteQuietly(file);
        }
    }

    private void loadCachedObjects() {
        File cacheDir = new File(objectGatewayConfiguration.getDownloadCachePath());
        if (!cacheDir.exists())
            cacheDir.mkdir();
        for (File file : FileUtils.listFiles(cacheDir, null, true)) {
            if (file.getName().endsWith(DOWNLOAD_TEMP_SUFFIX))
                file.delete();
            else
                cachedObjectMap.put(file.getAbsolutePath(), true);
        }
        logger.info("Load [{}] object index into cache.", cachedObjectMap.size());
    }


    private void cleanCache() {
        logger.info("Start to clean cache [{}].", objectGatewayConfiguration.getDownloadCachePath());

        //修改文件的最后修改时间
        for (String filepath : cachedObjectAccessMap.keySet()) {
            File file = new File(filepath);
            long lastModifyTime = cachedObjectAccessMap.remove(filepath);
            if (file.exists())
                file.setLastModified(lastModifyTime);
            else
                logger.error("File[{}] not exists when cache clean.", filepath);
        }


        long now = System.currentTimeMillis();
        File cacheDir = new File(objectGatewayConfiguration.getDownloadCachePath());

        LinkedHashMap<File, Long> fileSizeMap = new LinkedHashMap<>();
        for (File file : FileUtils.listFiles(cacheDir, null, true)) {
            if (now - file.lastModified() > objectGatewayConfiguration.getObjectCacheMaxKeepHours() * 3600 * 1000) {
                cachedObjectMap.remove(file.getAbsolutePath());
                FileUtils.deleteQuietly(file);
            } else
                fileSizeMap.put(file, file.lastModified());
        }

        logger.info("finish to clean expired cache [{}].", objectGatewayConfiguration.getDownloadCachePath());

        LinkedHashMap<File, Long> sortedfileSizeMap = fileSizeMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(new Comparator<File>() {
                    public int compare(File lFile, File rFile) {
                        return lFile.lastModified() > rFile.lastModified() ? -1 : 1;
                    }
                })). // file last modified desc order
                collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        long totalSize = 0;
        for (Map.Entry<File, Long> entry : sortedfileSizeMap.entrySet()) {
            totalSize = totalSize + entry.getKey().length();
            if (totalSize > objectGatewayConfiguration.getObjectCacheMaxSize()) {
                cachedObjectMap.remove(entry.getKey().getAbsolutePath());
                FileUtils.deleteQuietly(entry.getKey());
            }
        }

        logger.info("finish to clean extra cache [{}].", objectGatewayConfiguration.getDownloadCachePath());
    }

    public String getObject(String source, String objId) {
        String objectPath = getCachedPath(source, objId);
        File file = new File(objectPath);
        if (cachedObjectMap.containsKey(file.getAbsolutePath())) {
            cachedObjectAccessMap.put(file.getAbsolutePath(), System.currentTimeMillis());
            return file.getAbsolutePath();
        } else
            return null;
    }

    public String cacheObject(String source, String objId, byte[] content) throws Exception {
        String objectPath = getObject(source, objId);
        if (!StringUtils.isEmpty(objectPath))
            return objectPath;
        objectPath = getCachedPath(source, objId);
        File file = new File(objectPath);

        File tmpFile = new File(objectPath + DOWNLOAD_TEMP_SUFFIX);
        if (tmpFile.exists())
            tmpFile.delete();

        try {
            FileUtils.writeByteArrayToFile(tmpFile, content);
            tmpFile.renameTo(file);

            cachedObjectMap.put(file.getAbsolutePath(), true);
            return file.getAbsolutePath();
        } catch (Exception e) {
            FileUtils.deleteQuietly(tmpFile);
            FileUtils.deleteQuietly(file);

            throw new APIException(ObjectStorageGatewayHttpCode.RET_DOWNLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_CACHE_ERROR, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_CACHE_ERROR_MSG);
        }
    }

    private String getCachedPath(String source, String objectId) {
        String objectPath = objectId.replace("/", File.separator);
        return objectGatewayConfiguration.getDownloadCachePath() + File.separator + source + File.separator + objectPath;
    }


}
