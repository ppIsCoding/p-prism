package com.pp.pprism.exception;

import com.pp.pprism.common.BaseResponse;
import com.pp.pprism.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 统一构造 JSON 响应，避免流式接口（produces = text/event-stream）
     * 在参数校验失败时因内容协商而返回 406。
     */

    private static ResponseEntity<BaseResponse<?>> json(BaseResponse<?> body, HttpStatus status) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return json(ResultUtils.error(e.getCode(), e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * 请求体参数校验失败（@RequestBody + @Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<?>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("请求体参数校验失败: {}", message);
        return json(ResultUtils.error(ErrorCode.PARAMS_ERROR, message), HttpStatus.BAD_REQUEST);
    }

    /**
     * 请求参数绑定/校验失败
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<?>> bindExceptionHandler(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("请求参数绑定失败: {}", message);
        return json(ResultUtils.error(ErrorCode.PARAMS_ERROR, message), HttpStatus.BAD_REQUEST);
    }

    /**
     * 单个参数约束校验失败（@RequestParam + @NotBlank 等）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<?>> constraintViolationExceptionHandler(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数约束校验失败: {}", message);
        return json(ResultUtils.error(ErrorCode.PARAMS_ERROR, message), HttpStatus.BAD_REQUEST);
    }

    /**
     * 缺少必填请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<?>> missingServletRequestParameterExceptionHandler(
            MissingServletRequestParameterException e) {
        String message = "缺少请求参数: " + e.getParameterName();
        log.warn(message);
        return json(ResultUtils.error(ErrorCode.PARAMS_ERROR, message), HttpStatus.BAD_REQUEST);
    }

    /**
     * 请求的资源不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<BaseResponse<?>> noResourceFoundExceptionHandler(NoResourceFoundException e) {
        log.warn("请求资源不存在: {}", e.getResourcePath());
        return json(ResultUtils.error(ErrorCode.NOT_FOUND_ERROR), HttpStatus.NOT_FOUND);
    }

    /**
     * 兜底的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<BaseResponse<?>> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return json(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 兜底的未知异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<?>> exceptionHandler(Exception e) {
        log.error("未处理异常", e);
        return json(ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
