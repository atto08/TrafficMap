package com.example.transportation.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@NoArgsConstructor
public class BusStop {

    @Id
    private Long stationId;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private String region;

    public BusStop(Long stationId, String stationName, double latitude, double longitude, String region) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
    }
}
