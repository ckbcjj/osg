package com.mfc.object.storage.gateway;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mfc.config.ConfigTools3;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

/**
 *
 * @author allen.wang
 *
 */

@SpringBootApplication(scanBasePackages = {"com.mfc.object.storage.gateway"})
@EnableScheduling
public class ObjectStorageMainApplication {


	/**
	 * main function
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		ConfigTools3.load("config");
		SpringApplication.run(ObjectStorageMainApplication.class, args);

		System.out.println("****************MFC Object Storage Gateway Start*************************");
	}

	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		//单个文件最大
		factory.setMaxFileSize(DataSize.parse(ConfigTools3.getConfigAsString("mfc.osg.upload.file.size.max", "100MB")));
		/// 设置总上传数据总大小
		factory.setMaxRequestSize(DataSize.parse(ConfigTools3.getConfigAsString("mfc.osg.upload.request.size.max", "100MB")));

		return factory.createMultipartConfig();
	}

	/*@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}*/
}
