package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;
import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.Stock;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    
    List<Stock> findAll();

    List<Stock> findByDeletedAtIsNull();

    List<Stock> findByDeletedAtIsNullAndStatus(Integer status);

	Optional<Stock> findById(String id);
    
    List<Stock> findByBookMstIdAndStatus(Long book_id,Integer status);
    //書籍IDと在庫ステータス

    //SQLで取得
    @Query(value = "SELECT s.id " +
            "FROM stocks s " +
            "LEFT JOIN rental_manage rm ON s.id = rm.stock_id " +
            "JOIN book_mst bm ON s.book_id = bm.id " +
            "WHERE bm.id = ?1 AND s.status = '0' AND (rm.status IS NULL OR rm.expected_rental_on > ?2 OR ?2 > rm.expected_return_on) "
            +
            "GROUP BY s.id", nativeQuery = true)
    List<Object[]> calender(Long bookId, Date dayOfMonth);
}
