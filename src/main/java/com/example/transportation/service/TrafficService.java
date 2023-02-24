package com.example.transportation.service;

import com.example.transportation.dto.response.ResCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TrafficService {

    @Value("${seoulKey}")
    String key;

    @Transactional
    public ResponseEntity<?> getSubwayArrivalInfo(String keyword) {

        List<Map<String, Object>> realtimeArrivalList = new ArrayList<>();

        try {
            List<Map<String, Object>> arrivalInfoList = new ArrayList<>();

            String url = "http://swopenAPI.seoul.go.kr/api/subway/" + key + "/json/realtimeStationArrival/0/5/" + keyword;

            RestTemplate restTemplate = new RestTemplate();
            String jsonResult = restTemplate.getForObject(url, String.class);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResult);

            JsonNode arrivalList = rootNode.path("realtimeArrivalList");
            if (arrivalList.isArray()) {
                for (JsonNode arrival : arrivalList) {
                    int id = arrival.path("rowNum").asInt();
                    String arrivalArea = arrival.path("trainLineNm").asText();
                    String stationName = arrival.path("statnNm").asText();
                    String arrivalMsg = arrival.path("arvlMsg2").asText();
                    String currentLocation = arrival.path("arvlMsg3").asText();

                    Map<String, Object> arrivalInfo = new HashMap<>();

                    arrivalInfo.put("id", id);
                    arrivalInfo.put("arrivalArea", arrivalArea);
                    arrivalInfo.put("stationName", stationName);
                    arrivalInfo.put("arrivalMsg", arrivalMsg);
                    arrivalInfo.put("currentLocation", currentLocation);

                    arrivalInfoList.add(arrivalInfo);
                }
            } realtimeArrivalList = arrivalInfoList;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return ResponseEntity.status(ResCode.DATA_LOAD_SUCCESS.getStatus()).body(realtimeArrivalList);

        /*
         *list_total_count       총 데이터 건수(정상조회 시 출력됨
         * RESULT.CODE           요청결과 코드
         * RESULT.MESSAGE        요쳥결과 메세지
         * subwayId              지하철호선 Id
         * updnLine              상하행선구분
         * trainLineNm           도착지방면
         * subwayHeading         내리는문 방향
         * statnFid              이전 지하철역 Id
         * statnTid              다음 지하철역 Id
         * statnId               지하철역 Id
         * statnNm               지하철역 명
         * trnsitCo              환승 노선 수
         * ordkey                도착예정 열차순번
         * subwayList            연계호선 Id
         * statnList             연계지하철역 Id
         * btrainSttus           열차종류(급행, ITX)
         * barvIDt               열차도착예정시간(단위: 초)
         * btrainNo              열차번호(현재 운행중인 호선별 열차번호)
         * bstatnId              종착지하철역 Id
         * bstatnNm              종착지하철역 명
         * recptnDt              열차도착정보를 생성한 시각
         * arvlMsg2              첫번째 도착메세지(전역 진입, 전역 도착 등)
         * arvlMsg3              두번째 도착메세지(종합운동장 도착, 12분후(광명사거리) 등)
         * arvlCd                도착 코드 (0:진입, 1:도착, 2:출발, 3:전역출발, 4:전역진입, 5:전역도착, 99:운행중 ) */

    }
}

