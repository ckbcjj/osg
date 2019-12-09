package com.mfc.object.storage.gateway.controller;

import com.mfc.object.storage.gateway.aop.ObjectStorageApiCall;
import com.mfc.object.storage.gateway.model.request.ImageResizeRequest;
import com.mfc.object.storage.gateway.service.ImageResizeGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class ImageObjectResizeController {
    private static final Logger LOG = LoggerFactory.getLogger(ImageObjectResizeController.class);

    @Autowired
    private ImageResizeGatewayService imageGatewayService;

    /**
     * 这个接口是旧的MFC目前使用的图片接口，不能随意改动
     *
     * @param request
     * @param response
     * @param path
     * @param size
     */
    @ObjectStorageApiCall
    @RequestMapping(value = "/api/get/v1", method = RequestMethod.GET)
    @ResponseBody
    public Object get(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(value = "path", required = true) String path,
                                  @RequestParam(value = "size", required = false) String size) throws Exception {
        String filepath = imageGatewayService.getImageV1(path, size);
        return new FileSystemResource(filepath);
    }


    @ObjectStorageApiCall
    @RequestMapping(value = "/api/resize/v1", method = RequestMethod.GET)
    @ResponseBody
    public Object get(HttpServletRequest request, HttpServletResponse response,@ModelAttribute ImageResizeRequest resizeRequest) throws Exception {

        String filepath = imageGatewayService.getResizeImage(resizeRequest);
        String contentType = Files.probeContentType(Paths.get(filepath));
        response.setContentType(contentType);
        return new FileSystemResource(filepath);
    }

}
