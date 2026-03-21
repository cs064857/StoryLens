package com.shijiawei.storylens.tools;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

/**
 * ClassName: CalculatorTool
 * Description:
 *
 * @Create 2026/3/21 下午7:41
 */
@Component
public class CalculatorTool {

    @Tool("計算兩個整數的總和")
    public int add(int a , int b){
        System.out.println("LLM 自動調用了計算機 Tool，計算：" + a + " + " + b);
        return a + b;
    }

}
