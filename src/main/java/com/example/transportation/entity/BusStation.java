package com.example.transportation.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class BusStation {

    @Id
    @Column(nullable = false)
    private Long busStationId;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private int cityCode;

    @Column(nullable = false)
    private String cityName;

    @Column(nullable = false)
    private String manageCity;


    public BusStation(Long busStationId, String stationName, double latitude, double longitude,
                      int cityCode, String cityName, String manageCity){

        this.busStationId = busStationId;
        this.stationName = stationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cityCode = cityCode;
        this.cityName = cityName;
        this.manageCity = manageCity;
    }

}
