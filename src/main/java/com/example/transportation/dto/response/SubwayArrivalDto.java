package com.example.transportation.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubwayArrivalDto {

    private int id;

    private String arrivalArea;

    private String arrivalMsg;

    private String currentLocation;
}
