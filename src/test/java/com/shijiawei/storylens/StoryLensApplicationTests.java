package com.shijiawei.storylens;

import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.net.url.UrlBuilder;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.storylens.controller.NovelController;
import com.shijiawei.storylens.entity.Novel;
import com.shijiawei.storylens.service.inter.ChatCompletionService;
import com.shijiawei.storylens.service.inter.NovelService;
import com.shijiawei.storylens.utils.R;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("local")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StoryLensApplicationTests {

    @org.springframework.boot.test.web.server.LocalServerPort
    private int port;

    @Autowired
    private ChatCompletionService chatCompletionService;

    @Autowired
    private NovelController novelController;

    @Autowired
    private NovelService novelService;


    @Test
    void contextLoads() {
    }

    /**
     * 測試Chat
     */
    @Test
    void chat(){

        String result = chatCompletionService.chatCompletion("{\n" +
                "    \"model\": \"glm\",\n" +
                "    \"messages\": [\n" +
                "    {\n" +
                "      \"content\": \"你是誰?\"\n" +
                "    }\n" +
                "    ],\n" +
                "    \"stream\": false,\n" +
                "    \"max_tokens\": 128000\n" +
                "\n" +
                "}");


        System.out.println(result);

    }

    @Test
    void uploadNovel() throws Exception {

        File file = new File("src/test/resources/全職法師.txt");

        try (FileInputStream fis = new FileInputStream(file)) {
            MockMultipartFile multipartFile = new MockMultipartFile(
                    "file", 
                    file.getName(), 
                    MediaType.TEXT_PLAIN_VALUE, 
                    fis
            );
            
            R<Map<String, Object>> mapR = novelService.uploadNovel(multipartFile);
            System.out.println("上傳成功: " + mapR);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    @Test
    void readNovelPage() throws Exception {

//
        RestClient restClient = RestClient.create("http://localhost:" + port);

        ParameterizedTypeReference<R<Page<Novel>>> typeReferenceR = new ParameterizedTypeReference<R<Page<Novel>>>() {};

        R<Page<Novel>> reponse = restClient.get()
                .uri(uruBuilder -> uruBuilder
                        .path("/api/novel/page")
                        .queryParam("current", 1)
                        .queryParam("size", 5)
                        .build()
                ).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(typeReferenceR);

        assertNotNull(reponse);

        System.out.println(reponse);

        Page<Novel> page = reponse.getData();
        assertNotNull(page);

        System.out.println("總筆數: " + page.getTotal());
        System.out.println("當前頁資料: " + page.getRecords());


    }

    @Test
    void readNovelFromMinio() throws Exception {
        Long novelId = 2035559294023274497L;
        String novel = novelService.readNovelFromMinio(novelId);

        System.out.println(novel);
    }



}
