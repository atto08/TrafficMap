package com.example.transportation.dto.bus;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class BusArrivalListDto {

    private String stationName;

    private List<BusArrivalDto> busArrivalList;
}
