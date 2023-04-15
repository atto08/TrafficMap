package com.example.transportation.service;

import com.example.transportation.dto.response.ResCode;
import com.example.transportation.entity.SubwayStation;
import com.example.transportation.repository.SubwayStationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrafficService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final SubwayStationRepository subwayStationRepository;

    @Value("${googleApiKey}")
    String apiKey;

    @Transactional
    public ResponseEntity<?> calculateTravelTime(String departurePoint, String destinationPoint, String departureLine, String destinationLine) {

        Map<String, Object> subwayInfo = new HashMap<>();

        SubwayStation origin = subwayStationRepository.findByStationNameAndSubwayLine(departurePoint,departureLine);
        SubwayStation destination = subwayStationRepository.findByStationNameAndSubwayLine(destinationPoint,destinationLine);

        try {
            // Directions API 요청을 보낼 URL 설정
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.getLatitude() + "," + origin.getLongitude() + "&destination=" + destination.getLatitude() + "," + destination.getLongitude() + "&mode=transit&key=" + apiKey;

            // Directions API 요청 보내기
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // 응답 JSON 파싱
            JsonNode rootNode = objectMapper.readTree(in);
            JsonNode routeInfo = rootNode.path("routes").get(0).get("legs").get(0);

            String departureTime = routeInfo.get("departure_time").get("text").asText();
            String arrivalTime = routeInfo.get("arrival_time").get("text").asText();
            String duration = routeInfo.get("duration").get("text").asText();

            // 경로 및 이동 수단에 따른 예상 소요 시간 출력
            subwayInfo.put("departure", departurePoint);
            subwayInfo.put("destination", destinationPoint);
            subwayInfo.put("departureTime", departureTime);
            subwayInfo.put("arrivalTime", arrivalTime);
            subwayInfo.put("duration", duration);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(subwayInfo);
    }

}

