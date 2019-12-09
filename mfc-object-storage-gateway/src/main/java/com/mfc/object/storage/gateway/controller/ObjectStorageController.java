package com.mfc.object.storage.gateway.controller;

import com.mfc.object.storage.gateway.aop.ObjectStorageApiCall;
import com.mfc.object.storage.gateway.model.APIException;
import com.mfc.object.storage.gateway.model.request.ObjectGetRequest;
import com.mfc.object.storage.gateway.model.request.ObjectUploadRequest;
import com.mfc.object.storage.gateway.model.response.CommonResponse;
import com.mfc.object.storage.gateway.model.response.ObjectUploadResponse;
import com.mfc.object.storage.gateway.service.ObjectStorageGatewayService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class ObjectStorageController {
    private static final Logger logger = LoggerFactory.getLogger(ObjectStorageController.class);

    @Autowired
    private ObjectStorageGatewayService objectGatewayService;

    @RequestMapping(value = "/api/ping/v1", method = {RequestMethod.HEAD, RequestMethod.GET})
    @ResponseBody
    public String ping() {
        return "OK";
    }


    @ObjectStorageApiCall
    @RequestMapping(value = "/api/getObject/v1", method = RequestMethod.GET)
    @ResponseBody
    public Object getObject(HttpServletRequest request, HttpServletResponse response,
                                        @ModelAttribute ObjectGetRequest objectGetRequest) throws Exception {

        if (StringUtils.isEmpty(objectGetRequest.getAppId()))
            throw new APIException(ObjectStorageGatewayHttpCode.RET_DOWNLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_OBJECT_EMPTY_APPID, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_OBJECT_EMPTY_APPID_MSG);
        if (StringUtils.isEmpty(objectGetRequest.getObjId()))
            throw new APIException(ObjectStorageGatewayHttpCode.RET_DOWNLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_OBJECT_EMPTY_OBJECT_ID, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_OBJECT_EMPTY_OBJECT_ID_MSG);
        if (StringUtils.isEmpty(objectGetRequest.getSource()))
            throw new APIException(ObjectStorageGatewayHttpCode.RET_DOWNLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_OBJECT_EMPTY_SOURCE, ObjectStorageGatewayHttpCode.ERR_DOWNLOAD_OBJECT_EMPTY_SOURCE_MSG);
        String filepath = objectGatewayService.getObject(objectGetRequest);
        String contentType = Files.probeContentType(Paths.get(filepath));
        response.setContentType(contentType);
        return new FileSystemResource(filepath);
    }


    @ObjectStorageApiCall
    @RequestMapping(value = "/api/uploadObject/v1", method = RequestMethod.POST)
    @ResponseBody
    public CommonResponse<ObjectUploadResponse> uploadObject(HttpServletRequest request, HttpServletResponse response,
                                                             @RequestParam("file") MultipartFile sourceFile,@ModelAttribute ObjectUploadRequest objectUploadRequest) throws Exception {

        if (StringUtils.isEmpty(objectUploadRequest.getAppId()))
            throw new APIException(ObjectStorageGatewayHttpCode.RET_UPLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_APPID, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_APPID_MSG);
        if (StringUtils.isEmpty(objectUploadRequest.getObjId()))
            throw new APIException(ObjectStorageGatewayHttpCode.RET_UPLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_OBJECT_ID, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_OBJECT_ID_MSG);
        if (StringUtils.isEmpty(objectUploadRequest.getSource()))
            throw new APIException(ObjectStorageGatewayHttpCode.RET_UPLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_SOURCE, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_SOURCE_MSG);
        if (sourceFile.isEmpty() || sourceFile.getSize() <= 0)
            throw new APIException(ObjectStorageGatewayHttpCode.RET_UPLOAD_OBJECT, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_FILE, ObjectStorageGatewayHttpCode.ERR_UPLOAD_OBJECT_EMPTY_FILE_MSG);


        CommonResponse<ObjectUploadResponse> commonResponse = new CommonResponse();
        commonResponse.setData(objectGatewayService.uploadObject(sourceFile, objectUploadRequest));

        return commonResponse;

    }
}
