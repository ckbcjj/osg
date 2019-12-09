package com.mfc.object.storage.gateway.service;

import com.mfc.object.storage.gateway.configuration.ObjectStorageGatewayConfiguration;
import com.mfc.object.storage.gateway.controller.ObjectStorageGatewayHttpCode;
import com.mfc.object.storage.gateway.model.APIException;
import com.mfc.object.storage.gateway.model.ResizeMode;
import com.mfc.object.storage.gateway.model.request.ImageResizeRequest;
import com.mfc.object.storage.gateway.model.request.ObjectGetRequest;
import com.mfc.object.storage.gateway.tools.ImageTools;

import com.mfc.object.storage.gateway.tools.MD5;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.filters.Canvas;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class ImageResizeGatewayService {
    private static final Logger logger = LoggerFactory.getLogger(ImageResizeGatewayService.class);

    private static final String MFC_SOURCE_V1 = "mfcv1";

    private Map<String, Boolean> fileLock = new ConcurrentHashMap<>();


    @Autowired
    private ObjectStorageGatewayConfiguration configuration;

    @Autowired
    private ObjectStorageGatewayService objectGatewayService;

    @Autowired
    private ObjectStorageCacheService objectCacheService;


    public String getImageV1(String path, String size) throws Exception {
        String url = configuration.getOssBaseUrl(MFC_SOURCE_V1) + path;
        if (!StringUtils.isEmpty(size))
            url = url + size;
        return getImageFromOss(MFC_SOURCE_V1, path, url);
    }


    public String getResizeImage(ImageResizeRequest resizeRequest) throws Exception {
        String storageSource = configuration.getStorageSource(resizeRequest.getSource());
        if (StringUtils.isEmpty(storageSource)) {
            storageSource = configuration.getDefaultImageSource();
            resizeRequest.setSource(MFC_SOURCE_V1);
        }

        if (storageSource.equalsIgnoreCase("oss") && configuration.useOssResize(resizeRequest.getSource())) {
            return getResizeImageFromOSS(resizeRequest);
        } else {
            String objectId = renameObjectId(resizeRequest.getImagePath(), MD5.MD5(resizeRequest.toString()));

            String filepath = objectCacheService.getObject(resizeRequest.getSource(), objectId);
            if (!StringUtils.isEmpty(filepath))
                return filepath;
            String lockKey = getLockKey(resizeRequest.getSource(), objectId);
            try {
                fileLock.putIfAbsent(lockKey, true);
                synchronized (fileLock.get(lockKey)) {
                    filepath = objectCacheService.getObject(resizeRequest.getSource(), objectId);
                    if (!StringUtils.isEmpty(filepath))
                        return filepath;

                    ObjectGetRequest objectGetRequest = new ObjectGetRequest();
                    objectGetRequest.setObjId(resizeRequest.getImagePath());
                    objectGetRequest.setSource(resizeRequest.getSource());

                    filepath = objectGatewayService.getObject(objectGetRequest);
                    String format = FilenameUtils.getExtension(filepath);
                    byte[] image = resizeImages(FileUtils.readFileToByteArray(new File(filepath)), format, resizeRequest);
                    if (image != null) {
                        return objectCacheService.cacheObject(resizeRequest.getSource(), objectId, image);
                    } else
                        throw new APIException(ObjectStorageGatewayHttpCode.RET_IMAGE_RESIZE, ObjectStorageGatewayHttpCode.ERR_IMAGE_RESIZE_ERROR, ObjectStorageGatewayHttpCode.ERR_IMAGE_RESIZE_ERROR_MSG);

                }
            } catch (Exception e) {
                throw e;
            } finally {
                fileLock.remove(lockKey);
            }

        }

    }


    private byte[] resizeImages(byte[] sourcesIamge, String outFormat, ImageResizeRequest resizeRequest) throws Exception {
        if (StringUtils.isEmpty(resizeRequest.getMode()))
            return sourcesIamge;
        Builder builder = null;
        ByteArrayInputStream inputStream = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            inputStream = new ByteArrayInputStream(sourcesIamge);
            BufferedImage sourceBufferedImage = ImageIO.read(inputStream);

            //等比缩放，限制在指定w与h的矩形内的最大图片。
            if (ResizeMode.fromString(resizeRequest.getMode()) == ResizeMode.LFIT) {
                builder = getLFITBuilder(resizeRequest, sourceBufferedImage);
            }
            //等比缩放，延伸出指定w与h的矩形框外的最小图片。
            else if (ResizeMode.fromString(resizeRequest.getMode()) == ResizeMode.MFIT) {
                builder = getMFITBuilder(resizeRequest, sourceBufferedImage);
            } else if (ResizeMode.fromString(resizeRequest.getMode()) == ResizeMode.FILL) {
                builder = getFILLBuilder(resizeRequest, sourceBufferedImage);

            } else if (ResizeMode.fromString(resizeRequest.getMode()) == ResizeMode.PAD) {
                builder = getPADBuilder(resizeRequest, sourceBufferedImage);
            }

            if (builder == null)
                return sourcesIamge;
            builder.outputFormat(outFormat).toOutputStream(outputStream);

            return outputStream.toByteArray();


        } finally {
            if (inputStream != null)
                inputStream.close();
            if (outputStream != null)
                outputStream.close();
        }
    }


    //要找长边
    private Builder getLFITBuilder(ImageResizeRequest resizeRequest, BufferedImage sourceBufferedImage) {
        int sourceWidth = sourceBufferedImage.getWidth();
        int sourceHeight = sourceBufferedImage.getHeight();

        double wp = Double.MAX_VALUE;
        double hp = Double.MAX_VALUE;

        if (resizeRequest.getWidth() > 0) {
            wp = (double) resizeRequest.getWidth() / sourceWidth;
        }
        if (resizeRequest.getHeight() > 0) {
            hp = (double) resizeRequest.getHeight() / sourceHeight;
        }

        double scale = wp > hp ? hp : wp;
        if (resizeRequest.getLongSize() > 0) {
            double p = sourceWidth > sourceHeight ? ((double) resizeRequest.getLongSize() / sourceWidth) : ((double) resizeRequest.getLongSize() / sourceHeight);
            scale = (p > scale) ? scale : p;
        }
        if (resizeRequest.getShortSize() > 0) {
            double p = sourceHeight > sourceWidth ? ((double) resizeRequest.getShortSize() / sourceWidth) : ((double) resizeRequest.getShortSize() / sourceHeight);
            scale = (p > scale) ? scale : p;
        }

        if (resizeRequest.isLimit() && scale == Double.MAX_VALUE) {
            return null;
        }
        return Thumbnails.of(sourceBufferedImage).scale(scale);
    }


    //要找短边
    private Builder getMFITBuilder(ImageResizeRequest resizeRequest, BufferedImage sourceBufferedImage) {
        int sourceWidth = sourceBufferedImage.getWidth();
        int sourceHeight = sourceBufferedImage.getHeight();

        double wp = 0;
        double hp = 0;

        if (resizeRequest.getWidth() > 0) {
            wp = (double) resizeRequest.getWidth() / sourceWidth;
        }
        if (resizeRequest.getHeight() > 0) {
            hp = (double) resizeRequest.getHeight() / sourceHeight;
        }

        double scale = wp > hp ? wp : hp;

        if (resizeRequest.getLongSize() > 0) {
            double p = sourceWidth > sourceHeight ? ((double) resizeRequest.getLongSize() / sourceHeight) : ((double) resizeRequest.getLongSize() / sourceWidth);
            scale = (p > scale) ? p : scale;
        }
        if (resizeRequest.getShortSize() > 0) {
            double p = sourceHeight > sourceWidth ? ((double) resizeRequest.getShortSize() / sourceHeight) : ((double) resizeRequest.getShortSize() / sourceWidth);
            scale = (p > scale) ? p : scale;
        }

        if (resizeRequest.isLimit() && scale >= 1) {
            return null;
        }
        return Thumbnails.of(sourceBufferedImage).scale(scale);
    }


    private Builder getFILLBuilder(ImageResizeRequest resizeRequest, BufferedImage sourceBufferedImage) throws Exception {
        Builder mfitBuilder = getMFITBuilder(resizeRequest, sourceBufferedImage);
        if (mfitBuilder == null)
            return null;

        Builder builder = Thumbnails.of(mfitBuilder.asBufferedImage());
        if (builder != null) {
            int targetWidth = resizeRequest.getWidth() > 0 ? resizeRequest.getWidth() : resizeRequest.getHeight();
            int targetHeigth = resizeRequest.getHeight() > 0 ? resizeRequest.getHeight() : resizeRequest.getWidth();

            if (targetWidth <= 0 && targetHeigth <= 0) {
                if (resizeRequest.getLongSize() > 0) {
                    targetWidth = resizeRequest.getLongSize();
                    targetHeigth = resizeRequest.getLongSize();
                } else if (resizeRequest.getShortSize() > 0) {
                    targetWidth = resizeRequest.getShortSize();
                    targetHeigth = resizeRequest.getShortSize();
                } else {
                    targetWidth = sourceBufferedImage.getWidth();
                    targetHeigth = sourceBufferedImage.getHeight();
                }
            }
            builder.sourceRegion(Positions.CENTER, targetWidth, targetHeigth).size(targetWidth, targetHeigth);
        }

        return builder;
    }

    private Builder getPADBuilder(ImageResizeRequest resizeRequest, BufferedImage sourceBufferedImage) {
        Builder lfitBuilder = getLFITBuilder(resizeRequest, sourceBufferedImage);
        if (lfitBuilder == null)
            return null;

        int targetWidth = resizeRequest.getWidth() > 0 ? resizeRequest.getWidth() : resizeRequest.getHeight();
        int targetHeigth = resizeRequest.getHeight() > 0 ? resizeRequest.getHeight() : resizeRequest.getWidth();

        if (targetWidth <= 0 && targetHeigth <= 0) {
            if (resizeRequest.getLongSize() > 0) {
                targetWidth = resizeRequest.getLongSize();
                targetHeigth = resizeRequest.getLongSize();
            } else if (resizeRequest.getShortSize() > 0) {
                targetWidth = resizeRequest.getShortSize();
                targetHeigth = resizeRequest.getShortSize();
            } else {
                targetWidth = sourceBufferedImage.getWidth();
                targetHeigth = sourceBufferedImage.getHeight();
            }
        }
        lfitBuilder.addFilter(new Canvas(targetWidth, targetHeigth, Positions.CENTER, ImageTools.colorFromHexStr(resizeRequest.getColor())));
        return lfitBuilder;
    }

    private String getResizeImageFromOSS(ImageResizeRequest resizeRequest) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(configuration.getOssBaseUrl(resizeRequest.getSource())).append(resizeRequest.getImagePath());

        if (!StringUtils.isEmpty(resizeRequest.getMode())) {
            sb.append("?").append("x-oss-process=image/resize").append(",m_").append(ResizeMode.fromString(resizeRequest.getMode()).getMode());

            if (resizeRequest.getWidth() > 0)
                sb.append(",w_").append(resizeRequest.getWidth());
            if (resizeRequest.getHeight() > 0)
                sb.append(",h_").append(resizeRequest.getHeight());
            if (resizeRequest.getLongSize() > 0)
                sb.append(",l_").append(resizeRequest.getLongSize());
            if (resizeRequest.getShortSize() > 0)
                sb.append(",s_").append(resizeRequest.getShortSize());
            if (!resizeRequest.isLimit()) {
                sb.append(",limit_0");
            } else
                sb.append(",limit_1");
            if (!StringUtils.isEmpty(resizeRequest.getColor()))
                sb.append(",color_").append(resizeRequest.getColor());

        }

        String filepath = getImageFromOss(StringUtils.isEmpty(resizeRequest.getSource()) ? MFC_SOURCE_V1 : resizeRequest.getSource(), resizeRequest.getImagePath(), sb.toString());
        return filepath;
    }


    private String getImageFromOss(String source, String path, String url) throws Exception {
        String objectId = renameObjectId(path, MD5.MD5(url));
        String filepath = objectCacheService.getObject(source, objectId);
        if (!StringUtils.isEmpty(filepath))
            return filepath;

        String lockKey = getLockKey(source, objectId);
        try {
            fileLock.putIfAbsent(lockKey, true);
            synchronized (fileLock.get(lockKey)) {
                filepath = objectCacheService.getObject(source, objectId);
                if (!StringUtils.isEmpty(filepath))
                    return filepath;

                Response res = Jsoup.connect(url).method(Method.GET).maxBodySize(1024 * 1024 * 100)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0")
                        .validateTLSCertificates(false)
                        .timeout(60000).ignoreContentType(true).execute();
                return objectCacheService.cacheObject(source, objectId, res.bodyAsBytes());
            }
        } catch (Exception e) {
            logger.error("Get image from oss error.", e);
            if (e instanceof HttpStatusException) {
                throw new APIException(ObjectStorageGatewayHttpCode.RET_IMAGE_RESIZE, ObjectStorageGatewayHttpCode.ERR_IMAGE_RESIZE_OSS_HTTP_ERROR, ObjectStorageGatewayHttpCode.ERR_IMAGE_RESIZE_OSS_HTTP_ERROR_MSG);
            } else
                throw new APIException(ObjectStorageGatewayHttpCode.RET_IMAGE_RESIZE, ObjectStorageGatewayHttpCode.ERR_IMAGE_RESIZE_OSS_UNKNOWN_ERROR, ObjectStorageGatewayHttpCode.ERR_IMAGE_RESIZE_OSS_UNKNOWN_ERROR_MSG);
        } finally {
            fileLock.remove(lockKey);
        }
    }

    private String getLockKey(String source, String key) {
        return source + "@" + key;
    }


    public String renameObjectId(String objectId, String concatName) {
        return FilenameUtils.removeExtension(objectId) + "_" + concatName + "." + FilenameUtils.getExtension(objectId);
    }
}
