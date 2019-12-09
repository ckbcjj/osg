package com.mfc.object.storage.gateway.service;

import com.mfc.object.storage.gateway.configuration.ObjectStorageGatewayConfiguration;
import com.mfc.object.storage.gateway.controller.ObjectStorageGatewayHttpCode;
import com.mfc.object.storage.gateway.model.APIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class AWSS3Service {
    private static final Logger logger = LoggerFactory.getLogger(AWSS3Service.class);

    @Autowired
    private ObjectStorageGatewayConfiguration configuration;

    private Map<String, S3Client> s3ClientMap = new ConcurrentHashMap<>();
    private Map<String, String> bucketNameMap = new ConcurrentHashMap<>();
    private Map<String, StorageClass> storageClassMap = new ConcurrentHashMap<>();

    //todo 目前没找到好的方式判断，不太想通过get方法获取对象是否存在
    public boolean objectExist(String source, String key) throws Exception {
        S3Client s3Client = getS3Client(source);
        try {
            return false;
        } catch (Exception e) {
            logger.error("[AWSS3]query object exist [{}] from [{}] aws s3 error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_AWS_S3, ObjectStorageGatewayHttpCode.ERR_AWS_S3_QUERY_OBJECT_EXIST_ERROR, ObjectStorageGatewayHttpCode.ERR_AWS_S3_QUERY_OBJECT_EXIST_ERROR_MSG);
        }
    }


    public byte[] downloadObject(String source, String key) throws Exception {
        S3Client s3Client = getS3Client(source);
        try {
            return s3Client.getObject(GetObjectRequest.builder().bucket(bucketNameMap.get(source)).key(key).build(), ResponseTransformer.toBytes()).asByteArray();
        } catch (Exception e) {
            logger.error("[AWSS3]Download [{}] from [{}] aws s3 error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_AWS_S3, ObjectStorageGatewayHttpCode.ERR_AWS_S3_DOWNLOAD_ERROR, ObjectStorageGatewayHttpCode.ERR_AWS_S3_DOWNLOAD_ERROR_MSG);
        }
    }


    public boolean uploadObject(String source, String key, File file) throws Exception {
        if (file.length() > configuration.getUploadMultipartSize())
            return multipartUploadObject(source, key, file);
        else
            return directUploadObject(source, key, file);

    }

    public boolean directUploadObject(String source, String key, File file) throws Exception {
        S3Client s3Client = getS3Client(source);
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, inputStream.available());
            PutObjectResponse response = s3Client.putObject(PutObjectRequest.builder().bucket(bucketNameMap.get(source)).key(key).storageClass(storageClassMap.get(source)).acl(ObjectCannedACL.PRIVATE).build(), requestBody);

            return response.sdkHttpResponse().isSuccessful();
        } catch (Exception e) {
            logger.error("[AWSS3]Direct upload [{}] from [{}] aws s3 error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_AWS_S3, ObjectStorageGatewayHttpCode.ERR_AWS_S3_UPLOAD_DIRECT_ERROR, ObjectStorageGatewayHttpCode.ERR_AWS_S3_UPLOAD_DIRECT_ERROR_MSG);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

    }

    public boolean multipartUploadObject(String source, String key, File file) throws Exception {
        S3Client s3Client = getS3Client(source);

        RandomAccessFile randomAccessFile = null;
        FileChannel fileChannel = null;
        try {
            CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder().bucket(bucketNameMap.get(source)).key(key).storageClass(storageClassMap.get(source)).acl(ObjectCannedACL.PRIVATE).build();
            CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);

            String uploadId = response.uploadId();
            randomAccessFile = new RandomAccessFile(file, "r");
            fileChannel = randomAccessFile.getChannel();

            long size = fileChannel.size();
            int count = (int) (size / configuration.getUploadMultipartSize());
            if (size % configuration.getUploadMultipartSize() !=0)
                count++;

            Collection<CompletedPart> parts = new ArrayList<CompletedPart>();
            for (int i = 0; i < count; i++) {
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder().bucket(bucketNameMap.get(source)).key(key).uploadId(uploadId).partNumber(i + 1).build();
                long mapSize = i == count - 1 ? size % configuration.getUploadMultipartSize() : configuration.getUploadMultipartSize();
                //文件内存映射
                MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, i * configuration.getUploadMultipartSize(), mapSize);
                //分段上传
                String etag = s3Client.uploadPart(uploadPartRequest, RequestBody.fromByteBuffer(mappedByteBuffer)).eTag();
                CompletedPart part = CompletedPart.builder().partNumber(i + 1).eTag(etag).build();
                parts.add(part);
            }

            //告知s3合并各部分
            CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder().parts(parts).build();
            CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketNameMap.get(source))
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(completedMultipartUpload)
                    .build();
            CompleteMultipartUploadResponse completeMultipartUploadResponse = s3Client.completeMultipartUpload(completeMultipartUploadRequest);
            return completeMultipartUploadResponse.sdkHttpResponse().isSuccessful();

        } catch (Exception e) {
            logger.error("[AWSS3]Multipart upload [{}] from [{}] aws s3 error.", key, source);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_AWS_S3, ObjectStorageGatewayHttpCode.ERR_AWS_S3_UPLOAD_MULTIPART_ERROR, ObjectStorageGatewayHttpCode.ERR_AWS_S3_UPLOAD_MULTIPART_ERROR_MSG);

        } finally {
            if (fileChannel != null)
                fileChannel.close();
            if (randomAccessFile != null)
                randomAccessFile.close();
        }

    }

    private S3Client getS3Client(String source) throws Exception {
        try {
            S3Client s3Client = s3ClientMap.get(source);
            if (s3Client == null) {
                synchronized (this) {
                    s3Client = s3ClientMap.get(source);
                    if (s3Client != null)
                        return s3Client;
                    Region region = Region.of(configuration.getRegion(source));
                    StorageClass storageClass = StorageClass.valueOf(configuration.getStorageClass(source));
                    String bucketName = configuration.getBucketName(source);

                    AwsCredentials awsCreds = AwsBasicCredentials.create(configuration.getAccessKeyId(source), configuration.getSecretAccessKey(source));
                    s3Client = S3Client
                            .builder()
                            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                            .region(region)
                            .build();

                    s3ClientMap.put(source, s3Client);
                    bucketNameMap.put(source, bucketName);
                    storageClassMap.put(source, storageClass);
                }
            }

            return s3Client;
        } catch (Exception e) {
            logger.error("[AWSS3]Get [{}] aws s3 client error.", source, e);
            throw new APIException(ObjectStorageGatewayHttpCode.RET_AWS_S3, ObjectStorageGatewayHttpCode.ERR_AWS_S3_CLIENT_INIT_ERROR, ObjectStorageGatewayHttpCode.ERR_AWS_S3_CLIENT_INIT_ERROR_MSG);
        }


    }
}
