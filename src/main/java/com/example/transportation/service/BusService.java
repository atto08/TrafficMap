package com.example.transportation.service;

import com.example.transportation.dto.bus.BusArrivalDto;
import com.example.transportation.dto.bus.BusArrivalListDto;
import com.example.transportation.dto.bus.BusStationListDto;
import com.example.transportation.dto.bus.SearchBusStationListDto;
import com.example.transportation.dto.ResCode;
import com.example.transportation.entity.BusRouteStation;
import com.example.transportation.entity.BusStop;
import com.example.transportation.repository.BusRouteStationRepository;
import com.example.transportation.repository.BusStopRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

    private final BusStopRepository busStopRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpHeaders headers = new HttpHeaders();

    private final String[] emptyArray = {};

    @Value("${busKey}")
    String busKey;


    @Transactional
    public ResponseEntity<?> getBusArrival(Long station, int state) {

        if (state == 0) {
            // 상태가 0일 때 서울 버스 정류소 도착정보 제공
            return getBusArrivalSeoul(station);
        }
        // 상태가 0이 아닐 때 경기도 버스 정류소 도착정보 제공
        return getBusArrivalGyeonggi(station);
    }

    // 서울 버스 도착정보
    @Transactional
    public ResponseEntity<?> getBusArrivalSeoul(Long stationNum) {
        // 데이터를 운반할 Dto class 생성
        BusArrivalListDto busArrivalListDto = new BusArrivalListDto();
        List<BusArrivalDto> arrivalInfoList = new ArrayList<>();

        try {
            // 정류소에 도착하는 실시간 버스 도착 정보 Open Api
            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?ServiceKey=" + busKey + "&arsId=" + stationNum + "&resultType=json");

            // 응답 JSON 파싱
            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode arrivalList = rootNode.path("msgBody").path("itemList");

            String stationName = arrivalList.get(0).path("stNm").asText();

            // 도착 예정 버스목록 관련 데이터 파싱
            if (arrivalList.isArray()) {
                // 순차적으로 찾은 정보를 List 에 담기
                for (JsonNode arrival : arrivalList) {

                    Long routeId = arrival.path("busRouteId").asLong();
                    String busNum = arrival.path("rtNm").asText();
                    String arrivalMsg1 = arrival.path("arrmsg1").asText();
                    String localNow = arrival.path("stationNm1").asText();

                    BusArrivalDto arrivalInfo = new BusArrivalDto();

                    arrivalInfo.setRouteId(routeId);
                    arrivalInfo.setBusNumber(busNum);
                    arrivalInfo.setArrivalMsg1(arrivalMsg1);
                    arrivalInfo.setLocationNow(localNow);

                    // BusArrivalDto 에 List 로 정보 담기
                    arrivalInfoList.add(arrivalInfo);
                }
            }
            // BusArrivalListDto 에 정보 담기
            busArrivalListDto.setBusArrivalList(arrivalInfoList);
            busArrivalListDto.setStationName(stationName);

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 출력 형식을 Json 형식으로 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busArrivalListDto, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    // 경기도 버스 도착정보
    @Transactional
    public ResponseEntity<?> getBusArrivalGyeonggi(Long stationId) {

        // 데이터를 운반할 Dto class 생성
        BusArrivalListDto busArrivalListDto = new BusArrivalListDto();
        List<BusArrivalDto> arrivalInfoList = new ArrayList<>();

        try {
            // 경기도 버스 정류소에 도착하는 실시간 버스 도착 정보 Open Api
            URI url = new URI("https://apis.data.go.kr/6410000/busarrivalservice/getBusArrivalList?serviceKey=" + busKey + "&stationId=" + stationId);

            // Xml 형식 데이터를 문자로 저장
            RestTemplate restTemplate = new RestTemplate();
            String xmlResult = restTemplate.getForObject(url, String.class);

            // Xml 형식 데이터를 Json 형식으로 변환하기 위해 사용
            XmlMapper xmlMapper = new XmlMapper();
            JsonNode rootNode = xmlMapper.readTree(xmlResult);      // xml to Json

            JsonNode arrivalList = rootNode.path("msgBody").path("busArrivalList");
            JsonNode forStationName = arrivalList.path(0);

            // 정류소 이름을 특정 짓기 위해 DB 에서 해당하는 정류소명 불러오기
            BusRouteStation busStation = busRouteStationRepository.findByRouteIdAndStationIdAndStationOrder
                    (forStationName.path("routeId").asLong(), stationId, forStationName.path("staOrder").asInt());
            String stationName = busStation.getStationName();

            // 도착 예정 버스목록 관련 데이터 파싱
            if (arrivalList.isArray()) {
                // 순차적으로 찾은 정보를 List 에 담기
                for (JsonNode arrival : arrivalList) {

                    Long routeId = arrival.path("routeId").asLong();
                    int stationOrder = arrival.path("staOrder").asInt();
                    int estimatedArrival1 = arrival.path("predictTime1").asInt();
                    int estimatedArrival2 = arrival.path("predictTime2").asInt();
                    int locationNow = arrival.path("locationNo1").asInt();
                    // 버스 번호 (ex. 10-1) 를 DB 에서 불러오기
                    BusRouteStation bus = busRouteStationRepository.findByRouteIdAndStationIdAndStationOrder(routeId, stationId, stationOrder);
                    String busNumber = bus.getBusNumber();

                    BusArrivalDto arrivalInfo = new BusArrivalDto();

                    arrivalInfo.setRouteId(routeId);
                    arrivalInfo.setBusNumber(busNumber);
                    arrivalInfo.setArrivalMsg1(estimatedArrival1 + "분전");
                    arrivalInfo.setLocationNow(locationNow + "정류소 전");

                    // BusArrivalDto 에 List 로 정보 담기
                    arrivalInfoList.add(arrivalInfo);
                }
            }
            // BusArrivalListDto 에 정보 담기
            busArrivalListDto.setBusArrivalList(arrivalInfoList);
            busArrivalListDto.setStationName(stationName);

        } catch (URISyntaxException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
        // 출력 형식을 Json 형식으로 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busArrivalListDto, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    // 버스 정류소 검색
    @Transactional
    public ResponseEntity<?> searchBusStation(String station) {
        // 데이터를 운반할 Dto class 생성
        SearchBusStationListDto busStationList = new SearchBusStationListDto();
        List<BusStationListDto> busStationInfoList = new ArrayList<>();

        // Null Check
        if (station.isEmpty()){
            busStationList.setBusStationList(busStationInfoList);

            // 출력 형식을 Json 형식으로 설정
            return ResponseEntity.status(ResCode.DATA_EMPTY.getStatus()).body(busStationList);
        }

        try {
            // 키워드에 해당되는 정류소 값을 Response 해주는 Open Api
            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByName?ServiceKey=" + busKey + "&stSrch=" + station + "&resultType=json");

            // 응답 JSON 파싱
            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode stationList = rootNode.path("msgBody").path("itemList");

            // 서울 정류소 찾기 및 추가
            addStationList(busStationInfoList, stationList, "stNm", "arsId", "tmY", "tmX");

            // 경기도 정류소 찾기 및 추가
            List<BusStop> busStationList2 = busStopRepository.findAllByStationNameContains(station);

            // 순차적으로 찾은 정보를 List 에 담기
            for (BusStop busStation : busStationList2) {
                String stationName = busStation.getStationName();
                Long stationId = busStation.getStationId();
                double latitude = busStation.getLatitude();
                double longitude = busStation.getLongitude();

                BusStationListDto busStationListDto = new BusStationListDto();

                busStationListDto.setStationId(stationId);
                busStationListDto.setStationName(stationName);
                busStationListDto.setLatitude(latitude);
                busStationListDto.setLongitude(longitude);
                busStationListDto.setLocalState(1);

                // BusStationListDto 에 정보 담기
                busStationInfoList.add(busStationListDto);
            }
            // SearchBusStationListDto 에 정보 담기
            busStationList.setBusStationList(busStationInfoList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 출력 형식을 Json 형식으로 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busStationList, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    // 근처 정류소 찾기
    @Transactional
    public ResponseEntity<?> findNearByStationList(double gpsX, double gpsY, int distance) {

        Map<String, Object> busStationList = new HashMap<>();
        List<BusStationListDto> busStationInfoList = new ArrayList<>();

        try {
            // 현재 위치 기준 근처 정류소 값을 Response 해주는 Open Api
            URL url = new URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByPos?ServiceKey=" + busKey
                    + "&tmX=" + gpsX + "&tmY=" + gpsY + "&radius=" + distance + "&resultType=json");

            // 응답 JSON 파싱
            JsonNode rootNode = objectMapper.readTree(url);
            JsonNode stationList = rootNode.path("msgBody").path("itemList");

            // 정류소 관련 데이터 추가 및 파싱
            addStationList(busStationInfoList, stationList, "stationNm", "arsId", "gpsY", "gpsX");

            busStationList.put("stationList", busStationInfoList);

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 출력 형식을 Json 형식으로 설정
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(busStationList, headers, ResCode.DATA_LOAD_SUCCESS.getStatus());
    }


    // 정류소 목록에 관련된 데이터 파싱작업을 추가
    public void addStationList(List<BusStationListDto> list, JsonNode jsonNode,
                               String stName, String stNum, String y, String x) {

        // 정류소 관련 데이터 파싱
        if (jsonNode.isArray()) {
            // 순차적으로 찾은 정보를 List 에 담기
            for (JsonNode station : jsonNode) {

                Long stationId = station.path(stNum).asLong();
                String stationName = station.path(stName).asText();
                double latitude = station.path(y).asDouble();
                double longitude = station.path(x).asDouble();

                BusStationListDto busStationListDto = new BusStationListDto();

                // stationId 가 0으로 제공되는 경기도 버스는 제거
                if (stationId != 0) {
                    busStationListDto.setStationId(stationId);
                    busStationListDto.setStationName(stationName);
                    busStationListDto.setLatitude(latitude);
                    busStationListDto.setLongitude(longitude);
                    busStationListDto.setLocalState(0);

                    list.add(busStationListDto);
                }
            }
        }
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

        return ResponseEntity.ok("버스 노선/번호 저장성공");
    }

    @Transactional
    public ResponseEntity<?> parseBusStop() {

        String filePath = "src/main/resources/busStop.csv";

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
                // BusStop Table 에 저장.
                busStopRepository.save(new BusStop(Long.parseLong(row[0]), row[1], Double.parseDouble(row[5]), Double.parseDouble(row[4]), row[6]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok("버스정류장 저장성공");
    }
}
