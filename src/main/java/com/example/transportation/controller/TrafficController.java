package com.example.transportation.controller;

import com.example.transportation.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/traffic")
public class TrafficController {

    private final TrafficService trafficService;

    @GetMapping(value = "/subway/arrival")
    public ResponseEntity<?> getSubwayArrivalInfo(@RequestParam String keyword) {

        return trafficService.getSubwayArrivalInfo(keyword);
    }


    @GetMapping(value = "/bus/arrival")
    public ResponseEntity<?> getBusArrivalInfo(@RequestParam String stationNum) {

        return trafficService.getBusArrivalInfo(stationNum);
    }


    @GetMapping(value = "/search/busStation")
    public ResponseEntity<?> searchBusStation(@RequestParam String keyword){

        return trafficService.searchBusStation(keyword);
    }


    @GetMapping(value = "/nearby/busStation")
    public ResponseEntity<?> findNearByBusStationList(@RequestParam double x,
                                                      @RequestParam double y,
                                                      @RequestParam int distance){

        return trafficService.findNearByStationList(x, y, distance);
    }


    @GetMapping(value = "/busStop/info/parse")
    public ResponseEntity<?> parseBusInfo() {

        return trafficService.parseBusStationInfo();
    }

    @GetMapping(value = "/calculate/distance")
    public ResponseEntity<?> calculateDistance(@RequestParam String startingPoint, @RequestParam String destination) {

        return trafficService.calculateDistance(startingPoint, destination);
    }

//    @GetMapping(value = "/busRouteInfo/parse")
//    public ResponseEntity<?> parseBusRouteInfo() {
//
//        return trafficService.parseBusRouteInfo();
//    }
}
