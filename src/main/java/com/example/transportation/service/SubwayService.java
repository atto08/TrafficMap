package com.example.transportation.service;

import com.example.transportation.dto.response.ArrivalInfo;
import com.example.transportation.dto.response.FailMsgDto;
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
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubwayService {

    private final SubwayStationRepository subwayStationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String[] emptyArray = {};

    @Value("${subwayKey}")
    String subwayKey;


    @Transactional
    public ResponseEntity<?> getSubwayArrivalInfo(String station) {

        Map<String, Object> subwayArrivalList = new HashMap<>();

        if (station.isEmpty())
            checkStationEmpty(subwayArrivalList, "subwayList");

        try {
            List<Map<String, Object>> arrivalInfoList = new ArrayList<>();

            if (station.equals("응암")){
                station = "응암순환(상선)";

            } else if (station.equals("공릉")) {
                station = "공릉(서울산업대입구)";

            } else if (station.equals("남한산성입구(성남법원·검찰청)")){
                station = "남한산성입구(성남법원, 검찰청)";
            }

            String url = "http://swopenAPI.seoul.go.kr/api/subway/" + subwayKey + "/json/realtimeStationArrival/0/5/" + station;

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
    public ResponseEntity<?> searchSubwayStation(String station) {

        Map<String, Object> stationList = new HashMap<>();

        if (station.isEmpty())
            checkStationEmpty(stationList, "stationList");

        List<SubwayStation> subwayStationList = subwayStationRepository.findAllByStationNameContains(station);
        List<SubwayStation> subwayStations = new ArrayList<>();

        for (SubwayStation subwayStation : subwayStationList) {
            subwayStations.add(subwayStation);
        }

        stationList.put("stationList", subwayStations);

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(stationList);
    }


    @Transactional
    public ResponseEntity<?> findAllSubwayStationList(String subwayLine) {

        Map<String, Object> stationList = new HashMap<>();

        List<SubwayStation> subwayStationList = subwayStationRepository.findAllBySubwayLine(subwayLine);
        List<SubwayStation> subwayStations = new ArrayList<>();

        for (SubwayStation subwayStation : subwayStationList) {
            subwayStations.add(subwayStation);
        }

        stationList.put("stationList", subwayStations);

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(stationList);
    }


    @Transactional
    public ResponseEntity<?> findSubwayStation(String station) {

        List<SubwayStation> subwayStationList = subwayStationRepository.findAllByStationNameContains(station);
        List<SubwayStation> subwayStations = new ArrayList<>();

        subwayStations.addAll(subwayStationList);


        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(station);
    }


    public String stationNameToDirections(double latitude, double longitude){

        return latitude + "," + longitude;
    }


    @Transactional
    public ResponseEntity<?> parseSubwayStation() {

        String filePath = "src/main/resources/subwayStationList.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            List<String[]> rows = new ArrayList<>();

            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                rows.add(values);
            }

            for (String[] row : rows) {

                subwayStationRepository.save(new SubwayStation(row[0], row[1], row[2], Double.parseDouble(row[3]), Double.parseDouble(row[4])));

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


//    public ResponseEntity<?> convertStationName(String station){
//
//        if (station.equals("광명")||station.equals("서동탄")){
//            return ResponseEntity.status(ResCode.NO_CONTENT.getStatus()).body(new FailMsgDto(ResCode.NO_CONTENT.getMsg()));
//
//        } else if (station.equals("별내별가람")||station.equals("오남")||station.equals("진접")) {
//            return ResponseEntity.status(ResCode.NO_CONTENT.getStatus()).body(new FailMsgDto(ResCode.NO_CONTENT.getMsg()));
//
//        } else if (station.equals("강일")||station.equals("미사")||station.equals("하남검단산")||station.equals("하남시청")||station.equals("하남풍산")){
//            return ResponseEntity.status(ResCode.NO_CONTENT.getStatus()).body(new FailMsgDto(ResCode.NO_CONTENT.getMsg()));
//
//        } else if (station.equals("응암")) {
//
//
//        }
//
//    }

}
