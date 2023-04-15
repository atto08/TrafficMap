package com.example.transportation.controller;

import com.example.transportation.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/bus")
public class BusController {

    private final BusService busService;

    @GetMapping(value = "/arrival")
    public ResponseEntity<?> getBusArrivalInfo(@RequestParam String stationNum) {

        return busService.getBusArrivalInfo(stationNum);
    }


    @GetMapping(value = "/search/busStation")
    public ResponseEntity<?> searchBusStation(@RequestParam String station) {

        return busService.searchBusStation(station);
    }


    @GetMapping(value = "/nearby/busStation")
    public ResponseEntity<?> findNearByBusStationList(@RequestParam double x,
                                                      @RequestParam double y,
                                                      @RequestParam int distance) {

        return busService.findNearByStationList(x, y, distance);
    }


    @GetMapping(value = "/parse/busStop")
    public ResponseEntity<?> parseBusRouteStation() {

        return busService.parseBusRouteStation();
    }
}
