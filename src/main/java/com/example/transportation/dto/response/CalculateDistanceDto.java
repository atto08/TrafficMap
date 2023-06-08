package com.example.transportation.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CalculateDistanceDto {

    private String departure;

    private String destination;

    private String departureLine;

    private String destinationLine;

    private String departureTime;

    private String arrivalTime;

    private String durationTime;

    private Boolean bookmarkState;

    private List<String> polylineList;
}
