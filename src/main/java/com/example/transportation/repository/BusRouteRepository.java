package com.example.transportation.repository;

import com.example.transportation.entity.BusRoute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRouteRepository extends JpaRepository<BusRoute, String> {
}
