package com.shijiawei.storylens.config;

import com.shijiawei.storylens.codeEnum.HttpCodeEnum;
import com.shijiawei.storylens.exception.ServiceException;
import com.shijiawei.storylens.utils.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex) {
        log.warn("[missingServletRequestParameterExceptionHandler]", ex);
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(),
                String.format("請求參數缺失: %s", ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex) {
        log.warn("[methodArgumentTypeMismatchExceptionHandler]", ex);
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(),
                String.format("請求參數類型錯誤: %s", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        log.warn("[methodArgumentNotValidExceptionHandler]", ex);
        String errorMessage = null;
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError == null) {
            List<ObjectError> allErrors = ex.getBindingResult().getAllErrors();
            if (!allErrors.isEmpty()) {
                errorMessage = allErrors.get(0).getDefaultMessage();
            }
        } else {
            errorMessage = fieldError.getDefaultMessage();
        }
        if (errorMessage == null || errorMessage.isEmpty()) {
            return R.error(HttpCodeEnum.BAD_REQUEST);
        }
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(),
                String.format("請求參數不正確: %s", errorMessage));
    }

    @ExceptionHandler(BindException.class)
    public R<?> bindExceptionHandler(BindException ex) {
        log.warn("[bindExceptionHandler]", ex);
        FieldError fieldError = ex.getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "參數綁定錯誤";
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(),
                String.format("請求參數不正確: %s", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<?> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        log.warn("[constraintViolationExceptionHandler]", ex);
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(),
                String.format("請求參數不正確: %s", constraintViolation.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<?> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException ex) {
        log.warn("[httpMessageNotReadableExceptionHandler]", ex);
        if (ex.getMessage() != null && ex.getMessage().startsWith("Required request body is missing")) {
            return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), "請求參數類型錯誤: request body 缺失");
        }
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(),
                String.format("請求參數解析錯誤: %s", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<?> maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException ex) {
        log.warn("[maxUploadSizeExceededExceptionHandler]", ex);
        return R.error(HttpCodeEnum.BAD_REQUEST.getCode(), "上傳檔案過大，請調整後重試");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex) {
        log.warn("[httpRequestMethodNotSupportedExceptionHandler]", ex);
        return R.error(HttpCodeEnum.METHOD_ERR.getCode(),
                String.format("請求方式錯誤: %s", ex.getMessage()));
    }

    @ExceptionHandler(ServiceException.class)
    public R<?> serviceExceptionHandler(ServiceException ex) {
        log.warn("[serviceExceptionHandler] code={}, msg={}", ex.getCode(), ex.getMessage());
        return R.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<?> defaultExceptionHandler(HttpServletRequest req, Exception ex) {
        if (ex.getCause() instanceof ServiceException serviceException) {
            return serviceExceptionHandler(serviceException);
        }
        log.error("[defaultExceptionHandler] url={}", req.getRequestURI(), ex);
        return R.error(HttpCodeEnum.INTERNAL_SERVER_ERROR);
    }

}
