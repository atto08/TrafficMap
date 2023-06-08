package com.example.transportation.dto.response.bus;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class BusArrivalListDto {

    private Long stationId;

    private String stationName;

    private double latitude;

    private double longitude;

    private int localState;

    private Boolean bookmarkState;

    private List<BusArrivalDto> busArrivalList;
}
