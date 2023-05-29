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
    public ResponseEntity<?> getBusArrival(@RequestParam Long stationId,
                                           @RequestParam int localState){

        return busService.getBusArrival(stationId, localState);
    }

    @GetMapping(value = "/arrival/seoul")
    public ResponseEntity<?> getBusArrivalInfo(@RequestParam Long stationNum) {

        return busService.getBusArrivalSeoul(stationNum);
    }

    @GetMapping("/arrival/gyeonggi")
    public ResponseEntity<?> getBusArrivalGyeonggi(@RequestParam Long stationId){

        return busService.getBusArrivalGyeonggi(stationId);
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


    @GetMapping(value = "/parse/busRouteStation")
    public ResponseEntity<?> parseBusRouteStation() {

        return busService.parseBusRouteStation();
    }


    @GetMapping(value = "/parse/busStop")
    public ResponseEntity<?> parseBusStop() {

        return busService.parseBusStop();
    }
}
