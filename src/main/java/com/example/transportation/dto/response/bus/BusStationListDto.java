package com.example.transportation.dto.response.bus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusStationListDto {

    private Long stationId;

    private String stationName;

    private double latitude;

    private double longitude;

    private int localState;
}
