package jp.co.metateam.library.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Date;
import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.constants.Constants;
import jp.co.metateam.library.controller.StockController;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.CalendarDto;
import jp.co.metateam.library.model.DailyDuplication;
import jp.co.metateam.library.model.Stock;
import jp.co.metateam.library.model.StockDto;
import jp.co.metateam.library.repository.BookMstRepository;
import jp.co.metateam.library.repository.RentalManageRepository;
import jp.co.metateam.library.repository.StockRepository;

//import java.util.Map;

@Service
public class StockService {
    private final BookMstRepository bookMstRepository;
    private final StockRepository stockRepository;
    private final RentalManageRepository rentalManageRepository;
    



    @Autowired
    public StockService(BookMstRepository bookMstRepository, StockRepository stockRepository, RentalManageRepository rentalManageRepository){ 
        this.bookMstRepository = bookMstRepository;
        this.stockRepository = stockRepository;
        this.rentalManageRepository = rentalManageRepository;
    }

    @Transactional
    public List<Stock> findAll() {
        List<Stock> stocks = this.stockRepository.findByDeletedAtIsNull();

        return stocks;
    }
    
    @Transactional
    public List <Stock> findStockAvailableAll() {
        List <Stock> stocks = this.stockRepository.findByDeletedAtIsNullAndStatus(Constants.STOCK_AVAILABLE);

        return stocks;
    }

    @Transactional
    public Stock findById(String id) {
        return this.stockRepository.findById(id).orElse(null);
    }

    @Transactional 
    public void save(StockDto stockDto) throws Exception {
        try {
            Stock stock = new Stock();
            BookMst bookMst = this.bookMstRepository.findById(stockDto.getBookId()).orElse(null);
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setBookMst(bookMst);
            stock.setId(stockDto.getId());
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    @Transactional 
    public void update(String id, StockDto stockDto) throws Exception {
        try {
            Stock stock = findById(id);
            if (stock == null) {
                throw new Exception("Stock record not found.");
            }

            BookMst bookMst = stock.getBookMst();
            if (bookMst == null) {
                throw new Exception("BookMst record not found.");
            }

            stock.setId(stockDto.getId());
            stock.setBookMst(bookMst);
            stock.setStatus(stockDto.getStatus());
            stock.setPrice(stockDto.getPrice());

            // データベースへの保存
            this.stockRepository.save(stock);
        } catch (Exception e) {
            throw e;
        }
    }

    public List<Object> generateDaysOfWeek(int year, int month, LocalDate startDate, int daysInMonth) {
        List<Object> daysOfWeek = new ArrayList<>();
        for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            LocalDate date = LocalDate.of(year, month, dayOfMonth);
            DateTimeFormatter formmater = DateTimeFormatter.ofPattern("dd(E)", Locale.JAPANESE);
            daysOfWeek.add(date.format(formmater));
        }

        return daysOfWeek;
    }
    
    public List<CalendarDto> generateValues(Integer year, Integer month, Integer daysInMonth) {
    //指定された年・月に対応するカレンダーの情報(CalendarDtoのリスト)を生成

        List<CalendarDto> values = new ArrayList<>();
        List<BookMst> countByLendableBooks = this.bookMstRepository.findAll();
        //findAll()で全ての書籍情報を取得し、countByLendableBooksリストに格納

        for (int i = 0; i < countByLendableBooks.size(); i++) {
        //書籍Id分の繰り返しの処理(書籍名と利用可能在庫数の表示)

            BookMst book = countByLendableBooks.get(i);
            //iに対応する要素を取得し、BookMst型の変数bookに格納
            List<Stock> stockCount = this.stockRepository.findByBookMstIdAndStatus(book.getId(), Constants.STOCK_AVAILABLE);
            //指定された書籍IDと在庫状態(=Constants.STOCK_AVAILABLE)に基づいて在庫情報を取得し、stockCountリストに格納

            CalendarDto calendarDto = new CalendarDto();
            calendarDto.setTitle(book.getTitle());
            //書籍のタイトルをCalendarDtoオブジェクトのtitleフィールドに設定(書籍名)
            calendarDto.setTotalCount(stockCount.size());
            //stockCountリストの要素数を取得し、それをCalendarDtoオブジェクトのtotalCountフィールドに設定(利用可能在庫数)

            List<DailyDuplication> dailyDuplication = new ArrayList<>();

            for (int dayOfMonth = 1; dayOfMonth <= daysInMonth; dayOfMonth++) {
            //日にち分の繰り返しの処理(日付けごとの在庫数)

                Calendar cl = Calendar.getInstance();
                //現在の日付・時刻に対応するCalendarオブジェクトを取得
                cl.set(Calendar.YEAR, year);
                //年を指定されたyearに設定
                cl.set(Calendar.MONTH, month - 1);
                //月を指定されたmonthに設定
                cl.set(Calendar.DATE, dayOfMonth);
                //日を指定されたdayOfMonthに設定
                Date date = new Date();
                date = cl.getTime();
                //設定された年月日に対応する日付を取得し、dateに設定

                DailyDuplication dailyList = new DailyDuplication();
                List<Object[]> stockList = stockRepository.calender(book.getId(),date);
                //指定された書籍IDと日付に対応する在庫情報を取得し、結果をstockListというリストに格納

                dailyList.setExpectedRentalOn(date);
                //貸出予定日の設定
                dailyList.setStockId(stockList.isEmpty() ? null : stockList.get(0)[0].toString());
                //在庫管理番号を設定
                dailyList.setDailyCount(stockList.size());
                //日ごとの貸出可能な在庫数(=stockListの要素数)を設定

                dailyDuplication.add(dailyList);
            }

            calendarDto.setCountAvailableRental(dailyDuplication);
            //CalendarDtoオブジェクトのcountAvailableRentalフィールドにdailyDuplicationリストを設定

            values.add(calendarDto);
        }
        return values;
    }
}
    