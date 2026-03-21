package com.shijiawei.storylens.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.storylens.service.inter.ChatCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ChatCompletionController
 * Description:
 *
 * @Create 2026/3/20 下午10:19
 */
@RestController
public class ChatCompletionController {

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ChatCompletionService chatCompletionService;


    @PostMapping(value = "/v1/chat/completions", produces = MediaType.APPLICATION_JSON_VALUE)
    public String ChatCompletionStream(String message) throws JsonProcessingException {

        //調用對應的工作流
        chatCompletionService.chatCompletion(message);
        //決定為非串流還是串流
        boolean isStream = isStream(message);

        if(isStream){//串流回應
            return "Test";
        }else {//非串流回應
            return "Test2";
        }

    }

    /**
     * 判斷為串流還是非串流，若JSON中不存在stream屬性，則默認為非串流
     * @param message
     * @return
     * @throws JsonProcessingException
     */
    private boolean isStream(String message) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(message);

        if(jsonNode.isEmpty()){//默認為關閉串流
            return false;
        }

        return jsonNode.path("stream").asBoolean();

    }


}
