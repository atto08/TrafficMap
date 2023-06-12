package com.example.transportation.controller;

import com.example.transportation.security.MemberDetailsImpl;
import com.example.transportation.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/traffic")
public class TrafficController {

    private final TrafficService trafficService;

    @GetMapping(value = "/calculate/travel-time")
    public ResponseEntity<?> calculateTravelTime(@AuthenticationPrincipal MemberDetailsImpl memberDetails,
                                                 @RequestParam String departurePoint,
                                                 @RequestParam String destinationPoint,
                                                 @RequestParam String departureLine,
                                                 @RequestParam String destinationLine) {
        if (memberDetails == null){
            return trafficService.calculateTravelTime(null, departurePoint, destinationPoint, departureLine, destinationLine);
        } else {
            return trafficService.calculateTravelTime(memberDetails.getMember(), departurePoint, destinationPoint, departureLine, destinationLine);
        }
    }
}
