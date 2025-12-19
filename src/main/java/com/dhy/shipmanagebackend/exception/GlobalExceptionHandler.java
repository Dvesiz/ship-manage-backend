package com.dhy.shipmanagebackend.exception;

import com.dhy.shipmanagebackend.entity.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理参数校验异常 (ConstraintViolationException)
    // 比如：@Pattern, @Email, @NotBlank 等注解校验失败时抛出的异常
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        // 提取错误信息，例如 "register.code: 验证码不能为空" -> "验证码不能为空"
        String message = e.getMessage();
        if (StringUtils.hasLength(message)) {
            // 取冒号后面的部分
            String[] split = message.split(": ");
            if (split.length > 1) {
                message = split[1];
            }
        }
        return Result.error(message);
    }

    // 处理业务异常 (RuntimeException)
    // 比如：UserServiceImpl 中抛出的 "验证码错误"、"用户名已存在"
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        e.printStackTrace(); // 打印堆栈信息方便调试
        return Result.error(e.getMessage() != null ? e.getMessage() : "系统繁忙，请稍后重试");
    }

    // 处理其他未知异常
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        e.printStackTrace();
        return Result.error("系统内部错误");
    }
}