package com.example.transportation.service;

import com.example.transportation.dto.response.ResCode;
import com.example.transportation.entity.BusStation;
import com.example.transportation.repository.BusRouteRepository;
import com.example.transportation.repository.BusStationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TrafficService {

    private final BusRouteRepository busRouteRepository;
    private final BusStationRepository busStationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String[] emptyArray = {};

    @Value("${subwayKey}")
    String subwayKey;

    @Value("${busKey}")
    String busKey;

    @Value("${googleApiKey}")
    String apiKey;

    @Transactional
    public ResponseEntity<?> getSubwayArrivalInfo(String keyword) {

        Map<String, Object> subwayArrivalList = new HashMap<>();

        if (keyword.isEmpty())
            checkKeywordEmpty(subwayArrivalList, "subwayList");

        try {
            List<Map<String, Object>> arrivalInfoList = new ArrayList<>();

            String url = "http://swopenAPI.seoul.go.kr/api/subway/" + subwayKey + "/json/realtimeStationArrival/0/5/" + keyword;

            RestTemplate restTemplate = new RestTemplate();
            String jsonResult = restTemplate.getForObject(url, String.class);

            JsonNode rootNode = objectMapper.readTree(jsonResult);
            JsonNode arrivalList = rootNode.path("realtimeArrivalList");

            String stationName = arrivalList.get(0).path("statnNm").asText();

            if (arrivalList.isArray()) {
                for (JsonNode arrival : arrivalList) {
                    int id = arrival.path("rowNum").asInt();
                    String arrivalArea = arrival.path("trainLineNm").asText();
                    String arrivalMsg = arrival.path("arvlMsg2").asText();
                    String currentLocation = arrival.path("arvlMsg3").asText();

                    Map<String, Object> arrivalInfo = new HashMap<>();

                    arrivalInfo.put("id", id);
                    arrivalInfo.put("arrivalArea", arrivalArea);
                    arrivalInfo.put("arrivalMsg", arrivalMsg);
                    arrivalInfo.put("currentLocation", currentLocation);

                    arrivalInfoList.add(arrivalInfo);
                }
            }
            subwayArrivalList.put("subwayList", arrivalInfoList);
            subwayArrivalList.put("stationName", stationName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(subwayArrivalList);
    }


    @Transactional
    public ResponseEntity<?> getBusArrivalInfo(String stationNum) {

        Map<String, Object> busArrivalList = new HashMap<>();

        try {
            List<Map<String, Object>> arrivalInfoList = new ArrayList<>();

            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?ServiceKey=" + busKey + "&arsId=" + stationNum + "&resultType=json");

            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode arrivalList = rootNode.path("msgBody").path("itemList");

            if (arrivalList.isNull()) {
                busArrivalList.put("stationName", null);
                busArrivalList.put("busList", emptyArray);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(busArrivalList);
            }

            String stationName = arrivalList.get(0).path("stNm").asText();

            if (arrivalList.isArray()) {
                for (JsonNode arrival : arrivalList) {

                    Long busRouteId = arrival.path("busRouteId").asLong();
                    String busNum = arrival.path("rtNm").asText();
                    String arrivalMsg1 = arrival.path("arrmsg1").asText();
                    String direction = arrival.path("adirection").asText();
                    Long vehicleId = arrival.path("vehId1").asLong();

                    Map<String, Object> arrivalInfo = new HashMap<>();

                    arrivalInfo.put("busRouteId", busRouteId);
                    arrivalInfo.put("busNumber", busNum);
                    arrivalInfo.put("arrivalMsg1", arrivalMsg1);
                    arrivalInfo.put("direction", direction);
                    arrivalInfo.put("vehicleId", vehicleId);

                    arrivalInfoList.add(arrivalInfo);
                }
            }
            busArrivalList.put("busList", arrivalInfoList);
            busArrivalList.put("stationName", stationName);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(busArrivalList);
    }


    @Transactional
    public ResponseEntity<?> searchBusStation(String keyword) {

        Map<String, Object> busStationList = new HashMap<>();

        if (keyword.isEmpty())
            checkKeywordEmpty(busStationList, "stationList");

        try {
            List<Map<String, Object>> busStationInfoList = new ArrayList<>();

            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByName?ServiceKey=" + busKey + "&stSrch=" + keyword + "&resultType=json");

            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode stationList = rootNode.path("msgBody").path("itemList");

            addStationList(busStationList, busStationInfoList, stationList, "stId", "stNm", "arsId", "tmY", "tmX");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(busStationList);
    }


    @Transactional
    public ResponseEntity<?> findNearByStationList(double gpsX, double gpsY, int distance) {

        Map<String, Object> busStationList = new HashMap<>();

        try {
            List<Map<String, Object>> busStationInfoList = new ArrayList<>();

            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByPos?ServiceKey=" + busKey + "&tmX=" + gpsX + "&tmY=" + gpsY + "&radius=" + distance + "&resultType=json");

            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode stationList = rootNode.path("msgBody").path("itemList");

            addStationList(busStationList, busStationInfoList, stationList, "stationId", "stationNm", "arsId", "gpsY", "gpsX");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(busStationList);
    }


    @Transactional
    public ResponseEntity<?> calculateDistance(String startingPoint, String destination) {

        Map<String, Object> subwayInfo = new HashMap<>();

        try {
            // Directions API 요청을 보낼 URL 설정
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + startingPoint + "&destination=" + destination + "&mode=transit&key=" + apiKey;

            // Directions API 요청 보내기
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            // 응답 JSON 파싱
            JsonNode rootNode = objectMapper.readTree(in);
            JsonNode routeInfo = rootNode.path("routes").get(0).get("legs").get(0);

            String departure = routeInfo.get("departure_time").get("text").asText();
            String arrivalTime = routeInfo.get("arrival_time").get("text").asText();
            String duration = routeInfo.get("duration").get("text").asText();

            // 경로 및 이동 수단에 따른 예상 소요 시간 출력
            subwayInfo.put("startingPoint", startingPoint);
            subwayInfo.put("destination", destination);
            subwayInfo.put("departureTime", departure);
            subwayInfo.put("arrivalTime", arrivalTime);
            subwayInfo.put("duration", duration);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(subwayInfo);
    }


    @Transactional
    public ResponseEntity<?> parseBusStationInfo() {

        String filePath = "src/main/resources/busStationLocationInfo.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<String[]> rows = new ArrayList<>();

            // 첫 줄은 컬럼 이름이므로 스킵
            br.readLine();

            // 한 줄씩 읽어서 파싱
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                rows.add(values);
            }

            // 파싱 결과 출력
            for (String[] row : rows) {

                busStationRepository.save(new BusStation(Long.parseLong(row[5]), row[1], Double.parseDouble(row[2]), Double.parseDouble(row[3]),
                        Integer.parseInt(row[6]), row[7], row[8]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("성공");
    }

    public void addStationList(Map<String, Object> map, List<Map<String, Object>> list, JsonNode jsonNode,
                               String stId, String stName, String stNum, String y, String x) {

        if (jsonNode.isArray()) {
            for (JsonNode station : jsonNode) {

                Long stationId = station.path(stId).asLong();
                Long stationNum = station.path(stNum).asLong();
                String stationName = station.path(stName).asText();
                double latitude = station.path(y).asDouble();
                double longitude = station.path(x).asDouble();

                Map<String, Object> stationInfo = new HashMap<>();

                stationInfo.put("stationId", stationId);
                stationInfo.put("stationNum", stationNum);
                stationInfo.put("stationName", stationName);
                stationInfo.put("latitude", latitude);
                stationInfo.put("longitude", longitude);

                list.add(stationInfo);
            }
        }
        map.put("stationList", list);
    }

    public ResponseEntity<?> checkKeywordEmpty(Map<String, Object> map, String key) {

        map.put(key, emptyArray);

        return ResponseEntity.status(ResCode.DATA_EMPTY.getStatus()).body(map);
    }


//    @Transactional
//    public ResponseEntity<?> parseBusRouteInfo() {
//
//        String filePath = "src/main/resources/busRouteId.csv";
//
//        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            List<String[]> rows = new ArrayList<>();
//
//            br.readLine();
//
//            while ((line = br.readLine()) != null) {
//                String[] values = line.split(",");
//                rows.add(values);
//            }
//
//            for (String[] row : rows) {
//
//                busRouteRepository.save(new BusRoute(row[0], Long.parseLong(row[1])));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return ResponseEntity.ok("성공");
//    }
}

