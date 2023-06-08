package com.example.transportation.repository;

import com.example.transportation.entity.BusStopBookmark;
import com.example.transportation.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusStopBookmarkRepository extends JpaRepository<BusStopBookmark,Long> {

    List<BusStopBookmark> findAllByMember(Member member);

    Optional<BusStopBookmark> findByMemberAndStationId(Member member, Long stationId);

    Boolean existsByMemberAndStationId(Member member, Long stationId);
}
