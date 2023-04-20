package com.example.transportation.service;

import com.example.transportation.dto.response.BusArrivalDto;
import com.example.transportation.dto.response.BusArrivalListDto;
import com.example.transportation.dto.response.ResCode;
import com.example.transportation.entity.BusRouteStation;
import com.example.transportation.repository.BusRouteStationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRouteStationRepository busRouteStationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpHeaders headers = new HttpHeaders();

    private final String[] emptyArray = {};

    @Value("${busKey}")
    String busKey;


    // 정류소에 도착예정 버스목록 제공 기능
    @Transactional
    public ResponseEntity<?> getBusArrivalInfo(String stationNum) {

        Map<String, Object> busArrivalList = new HashMap<>();

        try {
            List<Map<String, Object>> arrivalInfoList = new ArrayList<>();

            // 정류소에 도착하는 실시간 버스 도착 정보 Open Api
            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?ServiceKey=" + busKey + "&arsId=" + stationNum + "&resultType=json");

            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode arrivalList = rootNode.path("msgBody").path("itemList");

            // 도착정보를 제공하는 정류소인지 체크
            if (arrivalList.isNull()) {
                busArrivalList.put("stationName", null);
                busArrivalList.put("busList", emptyArray);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(busArrivalList);
            }

            String stationName = arrivalList.get(0).path("stNm").asText();

            // 도착 예정 버스목록 관련 데이터 파싱
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
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busArrivalList, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    @Transactional
    public ResponseEntity<?> getBusArrivalGyeonggi(Long stationId) {

        BusArrivalListDto busArrivalListDto = new BusArrivalListDto();

        try {
            List<BusArrivalDto> arrivalInfoList = new ArrayList<>();

            // 정류소에 도착하는 실시간 버스 도착 정보 Open Api
            URI url = new URI("https://apis.data.go.kr/6410000/busarrivalservice/getBusArrivalList?serviceKey=" + busKey + "&stationId=" + stationId);

            RestTemplate restTemplate = new RestTemplate();
            String xmlResult = restTemplate.getForObject(url, String.class);

            XmlMapper xmlMapper = new XmlMapper();
            JsonNode rootNode = xmlMapper.readTree(xmlResult);

            JsonNode arrivalList = rootNode.path("msgBody").path("busArrivalList");
            System.out.println("arrivalList = " + arrivalList);

//            // 도착정보를 제공하는 정류소인지 체크
//            if (arrivalList.isNull()) {
//                busArrivalList.put("stationName", null);
//                busArrivalList.put("busList", emptyArray);
//
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(busArrivalList);
//            }

            JsonNode forStationName = arrivalList.path(0);

            BusRouteStation busStation = busRouteStationRepository.findByRouteIdAndStationIdAndStationOrder(forStationName.path("routeId").asLong(), stationId, forStationName.path("staOrder").asInt());
            String stationName = busStation.getStationName();

            // 도착 예정 버스목록 관련 데이터 파싱
            if (arrivalList.isArray()) {
                for (JsonNode arrival : arrivalList) {

                    Long routeId = arrival.path("routeId").asLong();
                    int stationOrder = arrival.path("staOrder").asInt();
                    int estimatedArrival1 = arrival.path("predictTime1").asInt();
                    int estimatedArrival2 = arrival.path("predictTime2").asInt();
                    int locationNow = arrival.path("locationNo1").asInt();

                    BusRouteStation bus = busRouteStationRepository.findByRouteIdAndStationIdAndStationOrder(routeId, stationId, stationOrder);
                    String busNumber = bus.getBusNumber();

                    BusArrivalDto arrivalInfo = new BusArrivalDto();

                    arrivalInfo.setRouteId(routeId);
                    arrivalInfo.setBusNumber(busNumber);
                    arrivalInfo.setStationOrder(stationOrder);
                    arrivalInfo.setArrivalMsg1(estimatedArrival1 + "분전");
                    arrivalInfo.setLocationNow(locationNow + "정류소 전");

                    arrivalInfoList.add(arrivalInfo);
                }
            }
            busArrivalListDto.setBusArrivalList(arrivalInfoList);
            busArrivalListDto.setStationName(stationName);

        } catch (URISyntaxException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busArrivalListDto, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    // 버스 정류소 검색
    @Transactional
    public ResponseEntity<?> searchBusStation(String station) {

        Map<String, Object> busStationList = new HashMap<>();

        // null 체크
        if (station.isEmpty())
            checkStationEmpty(busStationList, "stationList");

        try {
            List<Map<String, Object>> busStationInfoList = new ArrayList<>();

            // 키워드에 해당되는 정류소 값을 Response 해주는 Open Api
            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByName?ServiceKey=" + busKey + "&stSrch=" + station + "&resultType=json");

            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode stationList = rootNode.path("msgBody").path("itemList");

            // 정류소 관련 데이터 추가 및 파싱
            addStationList(busStationList, busStationInfoList, stationList, "stId", "stNm", "arsId", "tmY", "tmX");

        } catch (IOException e) {
            e.printStackTrace();
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busStationList, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    // 근처 정류소 찾기
    @Transactional
    public ResponseEntity<?> findNearByStationList(double gpsX, double gpsY, int distance) {

        Map<String, Object> busStationList = new HashMap<>();

        try {
            List<Map<String, Object>> busStationInfoList = new ArrayList<>();

            // 현재 위치 기준 근처 정류소 값을 Response 해주는 Open Api
            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByPos?ServiceKey=" + busKey
                    + "&tmX=" + gpsX + "&tmY=" + gpsY + "&radius=" + distance + "&resultType=json");

            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode stationList = rootNode.path("msgBody").path("itemList");

            // 정류소 관련 데이터 추가 및 파싱
            addStationList(busStationList, busStationInfoList, stationList, "stationId", "stationNm", "arsId", "gpsY", "gpsX");

        } catch (IOException e) {
            e.printStackTrace();
        }
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busStationList, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    // 정류소 목록에 관련된 데이터 파싱작업을 추가
    public void addStationList(Map<String, Object> map, List<Map<String, Object>> list, JsonNode jsonNode,
                               String stId, String stName, String stNum, String y, String x) {

        // 정류소 관련 데이터 파싱
        if (jsonNode.isArray()) {
            for (JsonNode station : jsonNode) {

                Long stationId = station.path(stId).asLong();
                Long stationNum = station.path(stNum).asLong();
                String stationName = station.path(stName).asText();
                double latitude = station.path(y).asDouble();
                double longitude = station.path(x).asDouble();


                Map<String, Object> stationInfo = new HashMap<>();

//                if (stationNum!=0){
                stationInfo.put("stationId", stationId);
                stationInfo.put("stationNum", stationNum);
                stationInfo.put("stationName", stationName);
                stationInfo.put("latitude", latitude);
                stationInfo.put("longitude", longitude);

                list.add(stationInfo);
//                }
            }
        }
        map.put("stationList", list);
    }


    @Transactional
    public ResponseEntity<?> parseBusRouteStation() {

        String filePath = "src/main/resources/busRouteStation.csv";

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

            for (String[] row : rows) {
                // BusRouteStation Table 에 저장.
                busRouteStationRepository.save(new BusRouteStation(Long.parseLong(row[0]), Long.parseLong(row[1]), row[2]
                        , Integer.parseInt(row[3]), row[4], row[5]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("저장성공");
    }


    public ResponseEntity<?> checkStationEmpty(Map<String, Object> map, String key) {

        map.put(key, emptyArray);

        return ResponseEntity.status(ResCode.DATA_EMPTY.getStatus()).body(map);
    }
}
