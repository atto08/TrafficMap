package com.example.transportation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
public enum ResCode {
    DATA_LOAD_SUCCESS(HttpStatus.OK,"LOAD_SUCCESS","데이터 불러오기 성공."),
    NO_CONTENT(HttpStatus.NO_CONTENT,"NO_CONTENT","내용이 존재하지 않습니다."),
    DATA_EMPTY(HttpStatus.ACCEPTED,"DATA_EMPTY","데이터 X");

    private final HttpStatus status;
    private final String code;
    private final String msg;
}
