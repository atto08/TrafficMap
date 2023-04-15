package com.example.transportation.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class BusRouteStation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long routeId;

    @Column(nullable = false)
    private Long stationId;

    @Column(nullable = false)
    private String upDown;

    @Column(nullable = false)
    private int stationOrder;

    @Column(nullable = false)
    private String busNum;

    @Column(nullable = false)
    private String stationName;


    public BusRouteStation(Long routeId, Long stationId, String upDown, int stationOrder, String busNum, String stationName){
        this.routeId = routeId;
        this.stationId = stationId;
        this.upDown = upDown;
        this.stationOrder = stationOrder;
        this.busNum = busNum;
        this.stationName = stationName;
    }
}
