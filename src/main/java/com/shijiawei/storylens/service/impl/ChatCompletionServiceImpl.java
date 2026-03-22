package com.shijiawei.storylens.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.storylens.service.inter.ChatCompletionService;
import com.shijiawei.storylens.tools.CalculatorTool;
import com.shijiawei.storylens.utils.R;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ClassName: ChatCompletionServiceImpl
 * Description:
 *
 * @Create 2026/3/20 下午11:52
 */
@Service
@Slf4j
public class ChatCompletionServiceImpl implements ChatCompletionService {

    @Autowired
    private CalculatorTool calculatorTool;

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ObjectMapper objectMapper;


    @Override
    public String chatCompletion(String message) {
////
////        List<Message> messages = new ArrayList<>();
////        SystemMessage systemMessage = SystemMessage.builder().content("我是langchain4j").build();
////        UserMessage userMessage = UserMessage.builder().content("你是誰?").build();
////        messages.add(systemMessage);
////        messages.add(userMessage);
//
//        List<ChatMessage> messages = new ArrayList<>();
//        dev.langchain4j.data.message.SystemMessage systemMessage = new dev.langchain4j.data.message.SystemMessage("我是langchain4j");
//        dev.langchain4j.data.message.UserMessage userMessage = new dev.langchain4j.data.message.UserMessage("你是誰?");
//        messages.add(systemMessage);
//        messages.add(userMessage);
//
//
//        String temperatureStr = "0.5";
//        Double temperature = Double.parseDouble(temperatureStr);
//        ChatRequest chatRequest = ChatRequest.builder().messages(messages).build();
//
//        OpenAiChatRequestParameters chatRequestParameters = OpenAiChatRequestParameters.builder().temperature(temperature).build();
//
//        ChatResponse chatMessageResult = chatModel.chat(messages);
//        log.info("chatMessageResult: {}", chatMessageResult);
//        ChatResponse chatRequestResult = chatModel.chat(chatRequest);
//        log.info("chatRequestResult: {}", chatRequestResult);
//
//
//
////
////
////        ChatMessage chatMessage = new AiMessage();
////        AiMessage.builder().attributes()..build();
////
////
//
//        OpenAiChatRequestParameters openAiChatRequestParameters = OpenAiChatRequestParameters.builder().temperature(temperature).build();
//        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().defaultRequestParameters(openAiChatRequestParameters).build();//openAiChatRequestParameters
////        OpenAiChatModel openAiChatModel = OpenAiChatModel.builder().defaultRequestParameters(chatRequestParameters).build();//不可將ChatRequestParameters放入否則報錯ClassCastException
//        ChatResponse chatResponse = openAiChatModel.doChat(chatRequest);
//        log.info("ChatResponse: {}", chatResponse);
//        String chatResponseString = chatResponse.toString();
//        log.info("chatResponseString: {}", chatResponseString);
//        AiMessage aiMessage = chatResponse.aiMessage();
//        log.info("AiMessage: {}", aiMessage);


        dev.langchain4j.data.message.SystemMessage systemMessage = new dev.langchain4j.data.message.SystemMessage("我是langchain4j");
        dev.langchain4j.data.message.UserMessage userMessage = new dev.langchain4j.data.message.UserMessage("你是誰?1+2等於多少?");
        ChatRequestParameters chatRequestParameters = ChatRequestParameters.builder()
                .temperature(0.7)
                .toolSpecifications(ToolSpecifications.toolSpecificationsFrom(calculatorTool))
                .build();

        ChatRequest chatRequest = ChatRequest.builder().messages(systemMessage, userMessage).parameters(chatRequestParameters).build();

        ChatResponse chatResponse = chatModel.chat(chatRequest);

        AiMessage aiMessage = chatResponse.aiMessage();

        boolean toolExecutionRequests = aiMessage.hasToolExecutionRequests();

        log.info("是否調用工具:{}", toolExecutionRequests);

        if (aiMessage.hasToolExecutionRequests()) {
            ToolExecutionRequest toolRequest = aiMessage.toolExecutionRequests().get(0);

            String argsJson = toolRequest.arguments();

            int result = calculatorTool.add(123, 456);


            ToolExecutionResultMessage toolResultMessage = ToolExecutionResultMessage.from(
                    toolRequest.id(),
                    toolRequest.name(),
                    String.valueOf(result) // 將算出結果封裝進去
            );


            ChatRequest request2 = ChatRequest.builder()
                    .messages(
                            UserMessage.from("123加 456等於多少？"), // 原始提問
                            aiMessage, // 第一輪模型的回覆 (包含 ToolExecutionRequest)
                            toolResultMessage // 你執行的結果回報
                    )
                    .build();

            ChatResponse finalResponse = chatModel.chat(request2);
            System.out.println(finalResponse.aiMessage().text());


        }
        return "";
    }

    @Override
    public R<String> handleChatCompletion(String message) {
        chatCompletion(message);
        boolean isStream = isStream(message);
        if (isStream) {
            return R.ok("Test");
        } else {
            return R.ok("Test2");
        }
    }

    private boolean isStream(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);
            if (jsonNode.isEmpty()) {
                return false;
            }
            return jsonNode.path("stream").asBoolean();
        } catch (JsonProcessingException e) {
            log.warn("解析串流參數失敗，預設為非串流: {}", e.getMessage());
            return false;
        }
    }
}
