package com.example.transportation.dto.response.subway;

import com.example.transportation.entity.SubwayRouteBookmark;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubwayRouteBookmarkListDto {

    private List<SubwayRouteBookmark> bookmarkList;
}
