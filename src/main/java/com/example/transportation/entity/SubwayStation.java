package com.example.transportation.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class SubwayStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subwayLine;

    @Column(nullable = false)
    private String stationName;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    public SubwayStation(String subwayLine, String stationName, String address, double latitude, double longitude){

        this.subwayLine = subwayLine;
        this.stationName = stationName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
