package com.shijiawei.storylens.codeEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: HttpCodeEnum
 * Description:
 *
 * @Create 2026/3/21 下午8:10
 */
@Getter
@AllArgsConstructor
public enum HttpCodeEnum {

    /**
     * 操作成功
     */
    SUCCESS("200", "操作成功!"),

    //======================= 其他枚舉 ==============================

    /**
     * 操作失敗
     */
    OPERATION_ERR("400", "操作失敗!"),

    /**
     * 沒有權限
     */
    NO_PERMISSION("403", "您沒有操作權限!"),

    /**
     * 頁面不存在
     */
    PAGE_NOT_FOUND("404", "未找到您請求的資源!"),

    /**
     * 請求方式錯誤
     */
    METHOD_ERR("405", "請求方式錯誤,請檢查後重試!"),

    /**
     * 請求限流
     */
    TOO_MANY_REQUESTS("406","請求次數過多，請稍後再試。"),

    /**
     * 參數格式不合法
     */
    VERIFY_ERR("500", "參數格式不合法,請檢查後重試!"),

    /**
     * 請求參數不正確
     */
    BAD_REQUEST("400", "請求參數不正確!"),

    /**
     * 帳號未登入
     */
    UNAUTHORIZED("401", "帳號未登入!"),

    /**
     * 系統異常
     */
    INTERNAL_SERVER_ERROR("500", "系統異常!"),

    /**
     * 未知異常
     */
    UNKNOWN_ERR("600","未知異常");



    /**
     * 回傳碼
     */
    private final String code;
    /**
     * 描述
     */
    private final String description;
}

