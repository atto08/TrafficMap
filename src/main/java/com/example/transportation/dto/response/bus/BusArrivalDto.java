package com.example.transportation.dto.response.bus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BusArrivalDto {

    private Long routeId;

    private String busNumber;

    private String arrivalMsg1;

    private String locationNow;

}
