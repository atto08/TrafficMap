package com.example.transportation.repository;

import com.example.transportation.entity.SubwayStation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubwayStationRepository extends JpaRepository<SubwayStation, Long> {

    List<SubwayStation> findAllBySubwayLine(String subwayLine);

    List<SubwayStation> findAllByStationNameContains(String stationName);

    SubwayStation findByStationNameAndSubwayLine(String stationName, String subwayLine);
}
