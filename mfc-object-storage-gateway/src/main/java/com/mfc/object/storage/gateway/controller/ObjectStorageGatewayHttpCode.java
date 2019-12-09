package com.mfc.object.storage.gateway.controller;

public class ObjectStorageGatewayHttpCode {
    public static final int SERVER_ERROR = 555; // Server error
    public static final String SERVER_ERROR_MSG = "Server Internal Error.";


    public static final int RET_DOWNLOAD_OBJECT = 6001;
    public static final int ERR_DOWNLOAD_OBJECT_EMPTY_APPID = 1;//
    public static final String ERR_DOWNLOAD_OBJECT_EMPTY_APPID_MSG = "Empty app id.";

    public static final int ERR_DOWNLOAD_OBJECT_EMPTY_OBJECT_ID = 2;//
    public static final String ERR_DOWNLOAD_OBJECT_EMPTY_OBJECT_ID_MSG = "Empty object id.";

    public static final int ERR_DOWNLOAD_OBJECT_EMPTY_SOURCE = 3;//
    public static final String ERR_DOWNLOAD_OBJECT_EMPTY_SOURCE_MSG = "Empty object source.";

    public static final int ERR_DOWNLOAD_UNKNOWN_SOURCE = 4;//
    public static final String ERR_DOWNLOAD_UNKNOWN_SOURCE_MSG = "unknown source.";

    public static final int ERR_DOWNLOAD_CACHE_ERROR = 5;//
    public static final String ERR_DOWNLOAD_CACHE_ERROR_MSG = "cache error.";


    public static final int RET_UPLOAD_OBJECT = 6002;
    public static final int ERR_UPLOAD_OBJECT_EMPTY_APPID = 1;//
    public static final String ERR_UPLOAD_OBJECT_EMPTY_APPID_MSG = "Empty app id.";

    public static final int ERR_UPLOAD_OBJECT_EMPTY_OBJECT_ID = 2;//
    public static final String ERR_UPLOAD_OBJECT_EMPTY_OBJECT_ID_MSG = "Empty object id.";

    public static final int ERR_UPLOAD_OBJECT_EMPTY_SOURCE = 3;//
    public static final String ERR_UPLOAD_OBJECT_EMPTY_SOURCE_MSG = "Empty object source.";

    public static final int ERR_UPLOAD_OBJECT_EMPTY_FILE = 4;//
    public static final String ERR_UPLOAD_OBJECT_EMPTY_FILE_MSG = "Empty object.";

    public static final int ERR_UPLOAD_OBJECT_INVALID_FILE = 5;//
    public static final String ERR_UPLOAD_OBJECT_INVALID_FILE_MSG = "Invalid file.";

    public static final int ERR_UPLOAD_UNKNOWN_SOURCE = 6;//
    public static final String ERR_UPLOAD_UNKNOWN_SOURCE_MSG = "unknown source.";

    public static final int ERR_UPLOAD_UNKNOWN_FAILED = 7;//
    public static final String ERR_UPLOAD_UNKNOWN_FAILED_MSG = "unknown source.";


    public static final int RET_IMAGE_RESIZE = 6101;
    public static final int ERR_IMAGE_RESIZE_OSS_HTTP_ERROR = 1;//
    public static final String ERR_IMAGE_RESIZE_OSS_HTTP_ERROR_MSG = "Oss image resize http error.";

    public static final int ERR_IMAGE_RESIZE_OSS_UNKNOWN_ERROR = 2;//
    public static final String ERR_IMAGE_RESIZE_OSS_UNKNOWN_ERROR_MSG = "Oss image resize unknown error.";

    public static final int ERR_IMAGE_RESIZE_ERROR = 3;//
    public static final String ERR_IMAGE_RESIZE_ERROR_MSG = "Oss image resize error.";


    public static final int RET_AWS_S3 = 7001;
    public static final int ERR_AWS_S3_CLIENT_INIT_ERROR = 1;//
    public static final String ERR_AWS_S3_CLIENT_INIT_ERROR_MSG = "AWS Client init error.";

    public static final int ERR_AWS_S3_DOWNLOAD_ERROR = 2;//
    public static final String ERR_AWS_S3_DOWNLOAD_ERROR_MSG = "AWS download error.";

    public static final int ERR_AWS_S3_UPLOAD_DIRECT_ERROR = 3;//
    public static final String ERR_AWS_S3_UPLOAD_DIRECT_ERROR_MSG = "AWS direct upload error.";

    public static final int ERR_AWS_S3_UPLOAD_MULTIPART_ERROR = 4;//
    public static final String ERR_AWS_S3_UPLOAD_MULTIPART_ERROR_MSG = "AWS multipart upload error.";

    public static final int ERR_AWS_S3_QUERY_OBJECT_EXIST_ERROR = 5;//
    public static final String ERR_AWS_S3_QUERY_OBJECT_EXIST_ERROR_MSG = "AWS query object exist error.";


    public static final int RET_ALIYUN_OSS = 7002;
    public static final int ERR_ALIYUN_OSS_CLIENT_INIT_ERROR = 1;//
    public static final String ERR_ALIYUN_OSS_CLIENT_INIT_ERROR_MSG = "OSS Client init error.";

    public static final int ERR_ALIYUN_OSS_DOWNLOAD_ERROR = 2;//
    public static final String ERR_ALIYUN_OSS_DOWNLOAD_ERROR_MSG = "oss download error.";

    public static final int ERR_ALIYUN_OSS_UPLOAD_DIRECT_ERROR = 3;//
    public static final String ERR_ALIYUN_OSS_UPLOAD_DIRECT_ERROR_MSG = "oss direct upload error.";

    public static final int ERR_ALIYUN_OSS_UPLOAD_MULTIPART_ERROR = 4;//
    public static final String ERR_ALIYUN_OSS_UPLOAD_MULTIPART_ERROR_MSG = "oss multipart upload error.";

    public static final int ERR_ALIYUN_OSS_QUERY_OBJECT_EXIST_ERROR = 5;//
    public static final String ERR_ALIYUN_OSS_QUERY_OBJECT_EXIST_ERROR_MSG = "oss query object exist error.";

    public static final int RET_LOCAL_STORAGE = 7003;

    public static final int ERR_LOCAL_STORAGE_DOWNLOAD_ERROR = 1;//
    public static final String ERR_LOCAL_STORAGE_DOWNLOAD_ERROR_MSG = "local storage download error.";

    public static final int ERR_LOCAL_STORAGE_UPLOAD_ERROR = 2;//
    public static final String ERR_LOCAL_STORAGE_UPLOAD_ERROR_MSG = "local storage upload error.";


}
