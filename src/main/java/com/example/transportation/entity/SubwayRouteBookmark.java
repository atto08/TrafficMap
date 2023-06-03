package com.example.transportation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubwayRouteBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String departure;

    @Column(nullable = false)
    private String destination;

    public SubwayRouteBookmark(Member member, String departure, String destination){
        this.member = member;
        this.departure = departure;
        this.destination = destination;
    }
}
