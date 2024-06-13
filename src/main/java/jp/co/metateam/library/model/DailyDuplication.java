package jp.co.metateam.library.model;

import java.util.List;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DailyDuplication {

    private String stockId;

    private Date expectedRentalOn;

    private Integer dailyCount;
    
}