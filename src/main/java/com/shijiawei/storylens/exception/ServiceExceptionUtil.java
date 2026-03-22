package com.shijiawei.storylens.exception;

import com.shijiawei.storylens.codeEnum.HttpCodeEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceExceptionUtil {

    public static ServiceException exception(ErrorCode errorCode) {
        return exception0(errorCode.getCode(), errorCode.getMsg());
    }

    public static ServiceException exception(ErrorCode errorCode, Object... params) {
        return exception0(errorCode.getCode(), errorCode.getMsg(), params);
    }

    public static ServiceException exception(HttpCodeEnum httpCodeEnum) {
        return new ServiceException(httpCodeEnum.getCode(), httpCodeEnum.getDescription());
    }

    public static ServiceException exception0(String code, String messagePattern, Object... params) {
        String message = doFormat(code, messagePattern, params);
        return new ServiceException(code, message);
    }

    public static ServiceException invalidParamException(String messagePattern, Object... params) {
        return exception0(HttpCodeEnum.OPERATION_ERR.getCode(), messagePattern, params);
    }

    static String doFormat(String code, String messagePattern, Object... params) {
        StringBuilder sbuf = new StringBuilder(messagePattern.length() + 50);
        int i = 0;
        int j;
        int l;
        for (l = 0; l < params.length; l++) {
            j = messagePattern.indexOf("{}", i);
            if (j == -1) {
                log.error("[doFormat][參數過多：錯誤碼({})|錯誤內容({})|參數({})", code, messagePattern, params);
                if (i == 0) {
                    return messagePattern;
                } else {
                    sbuf.append(messagePattern.substring(i));
                    return sbuf.toString();
                }
            } else {
                sbuf.append(messagePattern, i, j);
                sbuf.append(params[l]);
                i = j + 2;
            }
        }
        if (messagePattern.indexOf("{}", i) != -1) {
            log.error("[doFormat][參數過少：錯誤碼({})|錯誤內容({})|參數({})", code, messagePattern, params);
        }
        sbuf.append(messagePattern.substring(i));
        return sbuf.toString();
    }

}
