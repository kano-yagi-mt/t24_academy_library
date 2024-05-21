package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 貸出管理DTO
 */

@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    Date date = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());

    SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");

    public int isValidRentalDate(){
        return expectedRentalOn.compareTo(date);
    }

    public int isValidReturnDate(){
        return expectedReturnOn.compareTo(date);
    }

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    //@FutureOrPresent(message="貸出予定日は過去の日付けは選択できません")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    //@FutureOrPresent(message="返却予定日は過去の日付けは選択できません")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    public Optional<String> isValidStatus(Integer preStatus) {

        String errorMassage = "貸出ステータスは「%s」から「%s」に変更できません";

        if(!preStatus.equals(this.status)) {
            
            switch (preStatus) {
                case 0:
                    if (RentalStatus.RETURNED.getValue().equals(this.status)) {
                        return Optional.of ("貸出ステータスは貸出待ちから返却済みに変更できません");
                    }
                    break;
                case 1:
                    if(RentalStatus.RENT_WAIT.getValue().equals(this.status)) {
                        return Optional.of ("貸出ステータスは貸出中から貸出待ちに変更できません");
                    } else if (this.status == RentalStatus.CANCELED.getValue()) {
                        return Optional.of ("貸出ステータスは貸出中からキャンセルに変更できません");
                    } 
                    break;
                case 2:
                    if(RentalStatus.RENT_WAIT.getValue().equals(this.status)){
                        return Optional.of ("貸出ステータスは返却済みから貸出待ちに変更できません");
                    } else if(this.status == RentalStatus.RENTAlING.getValue()){
                        return Optional.of ("貸出ステータスは返却済みから貸出中に変更できません");
                    } else if(this.status == RentalStatus.CANCELED.getValue()){
                        return Optional.of ("貸出ステータスは返却済みからキャンセルに変更できません");
                    } 
                    break; 
                case 3:
                   if(RentalStatus.RENT_WAIT.getValue().equals(this.status)){
                       return Optional.of ("貸出ステータスはキャンセルから貸出待ちに変更できません");
                    } else if(this.status == RentalStatus.RENTAlING.getValue()){
                       return Optional.of ("貸出ステータスはキャンセルから貸出中に変更できません");
                    } else if(this.status == RentalStatus.RETURNED.getValue()){
                       return Optional.of ("貸出ステータスはキャンセルから返却済みに変更できません");
                    } 
                    break; 
            }
        }
        return Optional.empty();
    }
} 