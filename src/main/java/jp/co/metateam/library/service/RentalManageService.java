package jp.co.metateam.library.service; 

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
 
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.repository.AccountRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;
import jp.co.metateam.library.values.RentalStatus;
 
@Service
public class RentalManageService {
 
    private final AccountRepository accountRepository;
    private final RentalManageRepository rentalManageRepository;
    private final StockRepository stockRepository;
 
     @Autowired
    public RentalManageService(
        AccountRepository accountRepository,
        RentalManageRepository rentalManageRepository,
        StockRepository stockRepository
    ) {
        this.accountRepository = accountRepository;
        this.rentalManageRepository = rentalManageRepository;
        this.stockRepository = stockRepository;
    }
 
    @Transactional
    public List <RentalManage> findAll() {
        List <RentalManage> rentalManageList = this.rentalManageRepository.findAll();
 
        return rentalManageList;
    }

    //rental editで使うList SQLで取得したレコード
    @Transactional
    public List<RentalManage> findByStockIdAndStatusIn(String Id, Long rentalId){
        List <RentalManage> rentalManages = this.rentalManageRepository.findByStockIdAndStatusIn(Id, rentalId);
        return rentalManages;
    }

    //rental addで使うList SQLで取得したレコード
    @Transactional
    public List <RentalManage> findByStockIdAndStatusIn(String Id){
        List <RentalManage> rentalManages = this.rentalManageRepository.findByStockAndStatusIn(Id);
        return rentalManages;
    }
 
    @Transactional
    public RentalManage findById(Long id) {
        return this.rentalManageRepository.findById(id).orElse(null);
    }
 
    @Transactional
    public void save(RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }
 
            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }
 
            RentalManage rental = new RentalManage();
            rental = setRentalStatusDate(rental, rentalManageDto.getStatus());
            rental.setAccount(account);
            rental.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rental.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rental.setStatus(rentalManageDto.getStatus());
            rental.setStock(stock);
 
            // データベースへの保存
            this.rentalManageRepository.save(rental);
        } catch (Exception e) {
            throw e;
        }
    }
 
    @Transactional
    public void update(Long id, RentalManageDto rentalManageDto) throws Exception {
        try {
            Account account = this.accountRepository.findByEmployeeId(rentalManageDto.getEmployeeId()).orElse(null);
            if (account == null) {
                throw new Exception("Account not found.");
            }//アカウント情報をデータベースから引っ張ってくる・nullの場合は例外に投げる処理
 
            Stock stock = this.stockRepository.findById(rentalManageDto.getStockId()).orElse(null);
            if (stock == null) {
                throw new Exception("Stock not found.");
            }//在庫情報をデータベースから引っ張ってくる・nullの場合は例外に投げる処理
            RentalManage rentalManage = findById(id);
            if (rentalManage == null) {
                throw new Exception("RentalManage record not found.");
            }//RentalManageがnullだった場合例外に投げる処理

            setRentalStatusDate(rentalManage, rentalManageDto.getStatus());
            rentalManage.setId(rentalManageDto.getId());
            rentalManage.setAccount(account);
            rentalManage.setExpectedRentalOn(rentalManageDto.getExpectedRentalOn());
            rentalManage.setExpectedReturnOn(rentalManageDto.getExpectedReturnOn());
            rentalManage.setStatus(rentalManageDto.getStatus());
            rentalManage.setStock(stock);
           
            // データベースへの保存
            this.rentalManageRepository.save(rentalManage);
        } catch (Exception e) {
            throw e;//rentalManageオブジェクトをデータベースに保存・例外がある場合はエラーを呼び出す
        }
    }
 
    private RentalManage setRentalStatusDate(RentalManage rentalManage, Integer status){
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
       
        if (status == RentalStatus.RENTAlING.getValue()) {
            rentalManage.setRentaledAt(timestamp);
        } else if (status == RentalStatus.RETURNED.getValue()) {
            rentalManage.setReturnedAt(timestamp);
        } else if (status == RentalStatus.CANCELED.getValue()) {
            rentalManage.setCanceledAt(timestamp);
        }
 
        return rentalManage;
    }
}
