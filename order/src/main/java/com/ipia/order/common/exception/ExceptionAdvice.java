package com.ipia.order.common.exception;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.ipia.order.common.exception.general.GeneralException;
import com.ipia.order.common.exception.general.status.ErrorStatus;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(
        annotations = { RestController.class },
        basePackages = { "com.ipia.order.web.controller" }
)
public class ExceptionAdvice {

    /**
     * GeneralException 처리
     * @param exception GeneralException
     * @return ApiResponse - GeneralException
     */
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException exception) {
        log.error("COMM:CTRL:GENERAL:::GeneralException msg({})", exception.getStatus().getMessage());
        return ApiResponse.onFailure(exception.getStatus());
    }

    /**
     * Exception 처리
     * @param exception Exception
     * @return ApiResponse - INTERNAL_SERVER_ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception exception) {
        log.error("COMM:CTRL:____:::Exception msg({})", exception.getMessage());
        return ApiResponse.onFailure(ErrorStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    /**
     * MethodArgumentTypeMismatchException 처리 - 잘못된 값 입력
     * @param exception MethodArgumentTypeMismatchException
     * @return ApiResponse - BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.error("ARG_:CTRL:MISMATCH:::MethodArgumentTypeMismatchException msg({})", exception.getMessage());
        return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * IllegalArgumentException 처리 - 잘못된 인수
     * @param exception IllegalArgumentException
     * @return ApiResponse - BAD_REQUEST
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<String>> handleIllegalArgumentException(IllegalArgumentException exception) {
        log.error("ARG_:CTRL:ILLEGAL:::IllegalArgumentException msg({})", exception.getMessage());
        return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * MethodArgumentNotValidException 처리 - @Valid 검증 실패
     * @param exception MethodArgumentNotValidException
     * @return ApiResponse - BAD_REQUEST
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        log.error("ARG_:CTRL:VALID:::MethodArgumentNotValidException msg({})", exception.getMessage());
        return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * HttpMessageNotReadableException 처리 - JSON 파싱 오류
     * @param exception HttpMessageNotReadableException
     * @return ApiResponse - BAD_REQUEST
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.error("ARG_:CTRL:READABLE:::HttpMessageNotReadableException msg({})", exception.getMessage());
        return ApiResponse.onFailure(ErrorStatus.BAD_REQUEST, exception.getMessage());
    }

    /**
     * HttpMediaTypeNotSupportedException 처리 - 지원하지 않는 Content-Type
     * @param exception HttpMediaTypeNotSupportedException
     * @return ApiResponse - UNSUPPORTED_MEDIA_TYPE
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        log.error("ARG_:CTRL:MEDIA:::HttpMediaTypeNotSupportedException msg({})", exception.getMessage());
        return ApiResponse.onFailure(ErrorStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage());
    }

    /**
     * HttpRequestMethodNotSupportedException 처리 - 지원하지 않는 HTTP 메서드
     * @param exception HttpRequestMethodNotSupportedException
     * @return ApiResponse - METHOD_NOT_ALLOWED
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("ARG_:CTRL:METHOD:::HttpRequestMethodNotSupportedException msg({})", exception.getMessage());
        return ApiResponse.onFailure(ErrorStatus.METHOD_NOT_ALLOWED, exception.getMessage());
    }
}
