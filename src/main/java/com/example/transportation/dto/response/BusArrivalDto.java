package com.example.transportation.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusArrivalDto {

    private Long routeId;

    private String busNumber;

    private int stationOrder;

    private String arrivalMsg1;

    private String locationNow;

    public BusArrivalDto(Long routeId, String busNumber, int stationOrder, String estimatedArrival, String locationNow){
        this.routeId = routeId;
        this.busNumber = busNumber;
        this.stationOrder = stationOrder;
        this.arrivalMsg1 = estimatedArrival;
        this.locationNow = locationNow;
    }
}
