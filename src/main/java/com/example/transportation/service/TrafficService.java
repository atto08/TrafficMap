package com.example.transportation.service;

import com.example.transportation.dto.response.CalculateDistanceDto;
import com.example.transportation.dto.response.ResCode;
import com.example.transportation.entity.SubwayStation;
import com.example.transportation.repository.SubwayStationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrafficService {

    private final SubwayStationRepository subwayStationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpHeaders headers = new HttpHeaders();


    @Value("${googleApiKey}")
    String apiKey;

    @Transactional
    public ResponseEntity<?> calculateTravelTime(String departurePoint, String destinationPoint, String departureLine, String destinationLine) {

        CalculateDistanceDto arrivalInfo = new CalculateDistanceDto();

        // 출발/도착 지점의 지하철 역 정보 조회
        SubwayStation origin = subwayStationRepository.findByStationNameAndSubwayLine(departurePoint,departureLine);
        SubwayStation destination = subwayStationRepository.findByStationNameAndSubwayLine(destinationPoint,destinationLine);

        try {
            // Directions API 요청을 보낼 URL 설정
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.getLatitude() + "," + origin.getLongitude()
                    + "&destination=" + destination.getLatitude() + "," + destination.getLongitude() + "&mode=transit&key=" + apiKey;

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
            arrivalInfo.setDeparture(departurePoint);
            arrivalInfo.setDestination(destinationPoint);
            arrivalInfo.setDepartureTime(departureTime);
            arrivalInfo.setArrivalTime(arrivalTime);
            arrivalInfo.setDurationTime(duration);

            // polyline 추출
            JsonNode polylineList = routeInfo.get("steps");

            List<String> polylines = new ArrayList<>();

            if (polylineList.isArray()){
                for (JsonNode polyline : polylineList){
                    String poly = polyline.get("polyline").get("points").asText();

                    polylines.add(poly);
                }
            }

            arrivalInfo.setPolylineList(polylines);

        } catch (IOException e) {
            e.printStackTrace();
        }

        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(arrivalInfo, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }

}

