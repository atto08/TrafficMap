package com.example.transportation.dto.subway;

import com.example.transportation.entity.SubwayStation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SubwayListDto {

    private List<SubwayStation> stationList;
}
