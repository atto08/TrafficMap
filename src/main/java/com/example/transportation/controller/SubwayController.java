package com.example.transportation.controller;

import com.example.transportation.service.SubwayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/subway")
public class SubwayController {

    private final SubwayService subwayService;

    @GetMapping(value = "/arrival")
    public ResponseEntity<?> getSubwayArrivalInfo(@RequestParam String station) {

        return subwayService.getSubwayArrivalInfo(station);
    }


    @GetMapping(value = "/search/subwayStation")
    public ResponseEntity<?> searchSubwayStation(@RequestParam String station) {

        return subwayService.searchSubwayStation(station);
    }


    @GetMapping(value = "/find/stationList")
    public ResponseEntity<?> findAllSubwayStationList(@RequestParam String subwayLine) {

        return subwayService.findAllSubwayStationList(subwayLine);
    }


    @GetMapping(value = "/find/station")
    public ResponseEntity<?> findSubwayStation(@RequestParam String station) {

        return subwayService.findSubwayStation(station);
    }

    @GetMapping(value = "/parse/subwayLine")
    public ResponseEntity<?> parseSubwayStation() {

        return subwayService.parseSubwayStation();
    }
}
