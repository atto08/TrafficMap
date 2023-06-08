package com.example.transportation.repository;

import com.example.transportation.entity.Member;
import com.example.transportation.entity.SubwayRouteBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubwayRouteBookmarkRepository extends JpaRepository<SubwayRouteBookmark, Long> {

    List<SubwayRouteBookmark> findAllByMember(Member member);

    Optional<SubwayRouteBookmark> findByMemberAndDepartureAndDestination(Member member, String departure, String destination);

    Boolean existsByMemberAndDepartureAndDestinationAndDepartureLineAndDestinationLine(Member member, String departure, String destination,
                                                                                       String departureLine, String destinationLine);
}
