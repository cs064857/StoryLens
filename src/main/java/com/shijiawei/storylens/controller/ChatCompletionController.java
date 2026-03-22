package com.shijiawei.storylens.controller;

import com.shijiawei.storylens.service.inter.ChatCompletionService;
import com.shijiawei.storylens.utils.R;
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
    private ChatCompletionService chatCompletionService;

    @PostMapping(value = "/v1/chat/completions", produces = MediaType.APPLICATION_JSON_VALUE)
    public R<String> chatCompletionStream(String message) {
        return chatCompletionService.handleChatCompletion(message);
    }
}
