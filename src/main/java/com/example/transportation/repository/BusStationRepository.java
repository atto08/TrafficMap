package com.example.transportation.repository;

import com.example.transportation.entity.BusStation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusStationRepository extends JpaRepository<BusStation, Long> {
}
