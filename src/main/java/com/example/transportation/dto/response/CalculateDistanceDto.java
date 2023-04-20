package com.example.transportation.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CalculateDistanceDto {

    private String departure;

    private String destination;

    private String departureTime;

    private String arrivalTime;

    private String durationTime;
}
