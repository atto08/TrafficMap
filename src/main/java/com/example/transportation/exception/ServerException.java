package com.example.transportation.exception;

import lombok.Getter;

@Getter
public class ServerException extends RuntimeException { // runtimeException 상속

    private CustomErrorCode customErrorCode;
    private String detaliMessage;

    public ServerException(CustomErrorCode customErrorCode) {
        super(customErrorCode.getStatusMessage()); // runtimeException
        this.customErrorCode = customErrorCode;
        this.detaliMessage = customErrorCode.getStatusMessage();
    }

    public ServerException(CustomErrorCode customErrorCode, String detaliMessage) {
        super(detaliMessage); // runtimeException
        this.customErrorCode = customErrorCode;
        this.detaliMessage = detaliMessage;
    }
}
