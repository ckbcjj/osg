package com.mfc.object.storage.gateway.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.mfc.object.storage.gateway.configuration.ObjectStorageGatewayConfiguration;
import com.mfc.object.storage.gateway.controller.ObjectStorageGatewayHttpCode;
import com.mfc.object.storage.gateway.model.APIException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AliyunOSSService {
    private static final Logger logger = LoggerFactory.getLogger(AliyunOSSService.class);

    @Autowired
    private ObjectStorageGatewayConfiguration configuration;

    private Map<String, OSS> ossClientMap = new ConcurrentHashMap<>();
    private Map<String, String> bucketNameMap = new ConcurrentHashMap<>();

    public boolean objectExist(String source, String key) throws Exception {
        OSS ossClient = getOSSClient(source);
        try {
            return ossClient.doesObjectExist(bucketNameMap.get(source), key);
        } catch (Exception e) {
            logger.error("[OSS]query object exist [{}] from [{}] aliyun oss error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_ALIYUN_OSS, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_QUERY_OBJECT_EXIST_ERROR, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_QUERY_OBJECT_EXIST_ERROR_MSG);
        }
    }

    public byte[] downloadObject(String source, String key) throws Exception {
        OSS ossClient = getOSSClient(source);
        try {
            OSSObject ossObject = ossClient.getObject(bucketNameMap.get(source), key);
            return IOUtils.toByteArray(ossObject.getObjectContent());
        } catch (Exception e) {
            logger.error("[OSS]Download [{}] from [{}] aliyun oss error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_ALIYUN_OSS, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_DOWNLOAD_ERROR, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_DOWNLOAD_ERROR_MSG);
        }
    }


    public byte[] getResizeImageObject(String source, String key, String style) throws Exception {
        OSS ossClient = getOSSClient(source);
        try {
            GetObjectRequest request = new GetObjectRequest(bucketNameMap.get(source), key);
            request.setProcess(style);

            OSSObject ossObject = ossClient.getObject(request);
            return IOUtils.toByteArray(ossObject.getObjectContent());
        } catch (Exception e) {
            logger.error("[OSS]Download [{}] from [{}] aliyun oss error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_ALIYUN_OSS, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_DOWNLOAD_ERROR, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_DOWNLOAD_ERROR_MSG);
        }
    }

    public boolean uploadObject(String source, String key, File file) throws Exception {
        if (file.length() > configuration.getUploadMultipartSize())
            return multipartUploadObject(source, key, file);
        else
            return directUploadObject(source, key, file);

    }

    private boolean directUploadObject(String source, String key, File file) throws Exception {
        OSS ossClient = getOSSClient(source);
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketNameMap.get(source), key, file);
            PutObjectResult putObjectResult = ossClient.putObject(putObjectRequest);
            return putObjectResult.getResponse().isSuccessful();
        } catch (Exception e) {
            logger.error("[OSS]Direct upload [{}] from [{}] aliyun oss error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_ALIYUN_OSS, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_UPLOAD_DIRECT_ERROR, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_UPLOAD_DIRECT_ERROR_MSG);
        }

    }

    private boolean multipartUploadObject(String source, String key, File file) throws Exception {
        OSS ossClient = getOSSClient(source);

        try {
            InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketNameMap.get(source), key);
            // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个ID来发起相关的操作，如取消分片上传、查询分片上传等。
            InitiateMultipartUploadResult upresult = ossClient.initiateMultipartUpload(request);
            String uploadId = upresult.getUploadId();

            long partSize = configuration.getUploadMultipartSize();
            long fileLength = file.length();
            int count = (int) (fileLength / partSize) + 1;
            if (fileLength % partSize != 0)
                count++;

            List<PartETag> partETags = new ArrayList<PartETag>();
            for (int i = 0; i < count; i++) {
                long startPos = i * partSize;
                long curPartSize = (i + 1 == count) ? (fileLength - startPos) : partSize;
                InputStream instream = new FileInputStream(file);
                // 跳过已经上传的分片。
                instream.skip(startPos);
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(bucketNameMap.get(source));
                uploadPartRequest.setKey(key);
                uploadPartRequest.setUploadId(uploadId);
                uploadPartRequest.setInputStream(instream);
                // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
                uploadPartRequest.setPartSize(curPartSize);
                // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
                uploadPartRequest.setPartNumber(i + 1);
                // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
                UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
                // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
                partETags.add(uploadPartResult.getPartETag());
            }

            // 创建CompleteMultipartUploadRequest对象。
            // 在执行完成分片上传操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
            CompleteMultipartUploadRequest completeMultipartUploadRequest =
                    new CompleteMultipartUploadRequest(bucketNameMap.get(source), key, uploadId, partETags);

            // 完成上传。
            CompleteMultipartUploadResult completeMultipartUploadResult = ossClient.completeMultipartUpload(completeMultipartUploadRequest);
            return completeMultipartUploadResult.getResponse().isSuccessful();

        } catch (Exception e) {
            logger.error("[OSS]Multipart upload [{}] from [{}] aliyun oss error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_AWS_S3, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_UPLOAD_MULTIPART_ERROR, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_UPLOAD_MULTIPART_ERROR_MSG);

        }

    }

    private OSS getOSSClient(String source) throws Exception {
        try {
            OSS ossClient = ossClientMap.get(source);
            if (ossClient == null) {
                synchronized (this) {
                    ossClient = ossClientMap.get(source);
                    if (ossClient != null)
                        return ossClient;

                    String endpoint = configuration.getRegion(source);
                    String bucketName = configuration.getBucketName(source);

                    ossClient = new OSSClientBuilder().build(endpoint, configuration.getAccessKeyId(source), configuration.getSecretAccessKey(source));
                    ossClientMap.put(source, ossClient);
                    bucketNameMap.put(source, bucketName);
                }
            }
            return ossClient;
        } catch (Exception e) {
            logger.error("[OSS]Get [{}] aliyun oss client error.", source, e);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_ALIYUN_OSS, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_CLIENT_INIT_ERROR, ObjectStorageGatewayHttpCode.ERR_ALIYUN_OSS_CLIENT_INIT_ERROR_MSG);
        }

    }
}
