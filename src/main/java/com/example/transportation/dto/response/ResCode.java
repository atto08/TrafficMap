package com.example.transportation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
@AllArgsConstructor
public enum ResCode {
    DATA_LOAD_SUCCESS(HttpStatus.OK,"LOAD_SUCCESS","데이터 불러오기 성공.");

    private final HttpStatus status;
    private final String code;
    private final String msg;
}
