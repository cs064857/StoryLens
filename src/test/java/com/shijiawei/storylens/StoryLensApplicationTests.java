package com.shijiawei.storylens;

import com.shijiawei.storylens.service.inter.ChatCompletionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("local")
@SpringBootTest
class StoryLensApplicationTests {

    @Autowired
    private ChatCompletionService chatCompletionService;

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

}
