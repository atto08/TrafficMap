package com.example.transportation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
@AllArgsConstructor
public class BusStopDto {

    @NotNull
    private Long stationId;

    @NotNull
    private String stationName;

    @NotNull
    private double latitude;

    @NotNull
    private double longitude;

    @NotNull
    private int localState;
}
