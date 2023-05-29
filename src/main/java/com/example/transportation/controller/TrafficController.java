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

    @GetMapping(value = "/calculate/travel-time")
    public ResponseEntity<?> calculateTravelTime(@RequestParam String departurePoint,
                                                 @RequestParam String destinationPoint,
                                                 @RequestParam String departureLine,
                                                 @RequestParam String destinationLine) {

        return trafficService.calculateTravelTime(departurePoint, destinationPoint, departureLine, destinationLine);
    }

//    @GetMapping(value = "/busRouteInfo/parse")
//    public ResponseEntity<?> parseBusRouteInfo() {
//
//        return trafficService.parseBusRouteInfo();
//    }
}
