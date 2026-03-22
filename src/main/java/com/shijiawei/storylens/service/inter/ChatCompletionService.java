package com.shijiawei.storylens.service.inter;

import com.shijiawei.storylens.utils.R;

/**
 * ClassName: ChatCompletionService
 * Description:
 *
 * @Create 2026/3/20 下午10:59
 */

public interface ChatCompletionService {

    String chatCompletion(String message);

    R<String> handleChatCompletion(String message);

}
