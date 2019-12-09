package com.mfc.object.storage.gateway.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mfc.object.storage.gateway.controller.HttpTools;
import com.mfc.object.storage.gateway.controller.ObjectStorageGatewayHttpCode;
import com.mfc.object.storage.gateway.model.APIException;
import com.mfc.object.storage.gateway.model.response.CommonResponse;
import com.mfc.object.storage.gateway.service.ObjectStorageGatewayStatService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class ObjectStorageCallAspect {

    private final Logger logger = LoggerFactory.getLogger(ObjectStorageCallAspect.class);

    @Autowired
    private ObjectStorageGatewayStatService statService;

    @Pointcut("@annotation(com.mfc.object.storage.gateway.aop.ObjectStorageApiCall)")
    public void excute() {
    }

    @Around("excute()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        HttpServletRequest request = (HttpServletRequest) joinPoint.getArgs()[0];
        HttpServletResponse response = (HttpServletResponse) joinPoint.getArgs()[1];

        String apiName = request.getRequestURI();
        String ip = HttpTools.getRemoteHost(request);
        Object[] args = joinPoint.getArgs();

        Object result = null;
        long duration = -1L;
        try {
            result = joinPoint.proceed(args);
            duration = System.currentTimeMillis() - startTime;
            logger.info("Got [{}] request from [{}],args:[{}], timeMs:[{}] ms", apiName, ip, args, duration);
            statService.put(apiName, duration);
            return result;
        } catch (Exception e) {
            CommonResponse<?> commonResponse = new CommonResponse<>();

            if (e instanceof APIException) {
                APIException apiException = (APIException)e;
                commonResponse.setCode(apiException.getRetCode()+"-"+apiException.getErrCode());
                commonResponse.setMsg(apiException.getMsg());
            }else{
                commonResponse.setCode(ObjectStorageGatewayHttpCode.SERVER_ERROR+"");
                commonResponse.setMsg(ObjectStorageGatewayHttpCode.SERVER_ERROR_MSG);
            }

            response.setStatus(ObjectStorageGatewayHttpCode.SERVER_ERROR);
            logger.error("Got [{}] request error from [{}],args:[{}].", apiName, ip, args, e);
            return  commonResponse;
        }

    }
}
