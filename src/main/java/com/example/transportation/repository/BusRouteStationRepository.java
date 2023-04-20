package com.example.transportation.repository;

import com.example.transportation.entity.BusRouteStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRouteStationRepository extends JpaRepository<BusRouteStation, Long> {
    BusRouteStation findByRouteIdAndStationIdAndStationOrder(Long routeId, Long stationId, int stationOrder);
}
