package jp.co.metateam.library.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CalendarDto {

    private String title;

    private int totalCount;

    private List<DailyDuplication> countAvailableRental;
    
}
