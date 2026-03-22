package com.shijiawei.storylens.exception;

import lombok.Data;

@Data
public class ErrorCode {

    private final String code;

    private final String msg;

    public ErrorCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
