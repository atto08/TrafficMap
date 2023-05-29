package com.example.transportation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice // 각 컨트롤러에 advice 역할을 하는 어노테이션, 빈 등록 포함

public class CustomExceptionHandler {

    // BAD_REQUEST 예외처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(CustomException.class)
    public CustomErrorResponse handleException(
            CustomException e,
            HttpServletRequest request
    ) {
        log.error("errorCode : {}, url {}, message: {}",
                e.getCustomErrorCode(), request.getRequestURI(), e.getDetaliMessage());

        return CustomErrorResponse.builder()
                .status(e.getCustomErrorCode())
                .statusMessage(e.getDetaliMessage())
                .build();
    }

    // INTERNAL_SERVER_ERROR 예외처리
    @ResponseStatus(HttpStatus.GONE)
    @ExceptionHandler(ServerException.class)
    public CustomErrorResponse serverHandleException(
            ServerException e,
            HttpServletRequest request
    ) {
        log.error("errorCode : {}, url {}, message: {}",
                e.getCustomErrorCode(), request.getRequestURI(), e.getDetaliMessage());
        return CustomErrorResponse.builder()
                .status(e.getCustomErrorCode())
                .statusMessage(e.getDetaliMessage())
                .build();
    }

    // 예외처리 하기 힘든 예외처리
    @ExceptionHandler(value = {
            HttpRequestMethodNotSupportedException.class, // get, post 등 메소드(요청)가 매치하지 않았을경우
            MethodArgumentNotValidException.class, // 컨트롤러 내부 진입 전에 밸리데이션으로 발생하는 에러를 잡음
    })
    public CustomErrorResponse handleBadRequest(
            Exception e, HttpServletRequest request
    ) {
        log.error("url {}, message: {}",
                request.getRequestURI(), e.getMessage());

        return CustomErrorResponse.builder()
                .status(CustomErrorCode.INVALID_REQUEST)
                .statusMessage(CustomErrorCode.INVALID_REQUEST.getStatusMessage())
                .build();
    }

    // 알수없거나 알아내기 힘들 오류의 최후 처리
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public CustomErrorResponse handleException(
            Exception e, HttpServletRequest request
    ) {
        log.error("url {}, message: {}",
                request.getRequestURI(), e.getMessage());

        e.printStackTrace();
        return CustomErrorResponse.builder()
                .status(CustomErrorCode.INTERNAL_SERVER_ERROR)
                .statusMessage(CustomErrorCode.INVALID_REQUEST.getStatusMessage())
                .build();
    }
}
