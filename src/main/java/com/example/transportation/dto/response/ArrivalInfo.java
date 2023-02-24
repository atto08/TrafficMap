package com.example.transportation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArrivalInfo {
    private int id;
    private String stationName;
    private String arrivalArea;
    private String arrivalMsg1;
    private String arrivalMsg2;
}

