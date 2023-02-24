package com.example.transportation.controller;

import com.example.transportation.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/traffic")
public class TrafficController {

    private final TrafficService trafficService;

    @GetMapping(value = "/subway/arrival")
    public ResponseEntity<?> getSubwayArrivalInfo(@RequestParam String keyword) {

        return trafficService.getSubwayArrivalInfo(keyword);
    }
}
