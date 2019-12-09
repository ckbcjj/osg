package com.mfc.object.storage.gateway;

import com.alibaba.fastjson.JSON;
import com.mfc.aes.AESTools;
import com.mfc.config.ConfigTools3;
import com.mfc.object.storage.gateway.model.request.ObjectGetRequest;
import com.mfc.object.storage.gateway.model.request.ObjectUploadRequest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Kemp.Cheng
 * created on: 2019/10/31
 * Copyright Valoroso Ltd. (c) 2019.  All rights reserved.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ObjectStorageMainApplication.class)
@WebAppConfiguration
public class ObjectStorageTest {

    private static final String AES_KEY = "Ebw6MyVpHAC23ZMszboDDQ";

    {
        ConfigTools3.load("cfg");
    }

    @Autowired
    WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void SetUp() {
        System.out.println("test class init...");
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testUpload() throws Exception {
        System.out.println("test begin");

        String filePath = "doc/example.jpg";

        MockMultipartFile file = new MockMultipartFile("file", "example.jpg", MediaType.MULTIPART_FORM_DATA.getType(), new FileInputStream(new File(filePath)));

        ObjectUploadRequest objectUploadRequest = new ObjectUploadRequest();
        objectUploadRequest.setAppId("java-test");
        objectUploadRequest.setSource("test-client");
        objectUploadRequest.setObjId("example.jpg");

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.fileUpload("/api/uploadObject/v1")
                .file(file)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(objectUploadRequest)))
                .andReturn().getResponse();

        System.out.println(String.format("HttpCode:%d,Response-String:[%s]", response.getStatus(), response.getContentAsString()));
    }

    @Test
    public void testDownload() throws Exception {
        ObjectGetRequest objectGetRequest = new ObjectGetRequest();
        objectGetRequest.setSource("test-client");
        objectGetRequest.setAppId("java-test");
        objectGetRequest.setObjId("example.jpg");

        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders.post("/api/getObject/v1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(objectGetRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse();

        FileUtils.writeByteArrayToFile(new File("test-resp/example.jpg"), response.getContentAsByteArray());

    }

    @Test
    public void testMethod1() throws Exception {
        String content = "Qeom56RFUW3ijxFEEhkWPh1EF5pgpqLjv7t4q-UNOYQ";
        System.out.println(AESTools.decrypt(content, AES_KEY));

        String content2 = "d9HgTn9zcVC05bZ0vP9mBQ";
        System.out.println(AESTools.decrypt(content2, AES_KEY));
    }
}
