package com.example.transportation.dto.response.bus;

import com.example.transportation.entity.BusStopBookmark;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BusStopBookmarkListDto {

    private List<BusStopBookmark> bookmarkList;
}
