package jp.co.metateam.library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jp.co.metateam.library.model.RentalManage;

@Repository
public interface RentalManageRepository extends JpaRepository<RentalManage, Long> {
    List<RentalManage> findAll();

	Optional<RentalManage> findById(Long id);

     //SQLでの取得
@Query("select rm"
        + " from RentalManage rm" + " where (rm.status = 0 or rm.status = 1)"
        + " and rm.stock.id = ?1 "
        + " and rm.id <> ?2")
    List<RentalManage> findByStockIdAndStatusIn(String Id,Long rentalId);
 
@Query("select rm"
        + " from RentalManage rm" + " where (rm.status = 0 or rm.status = 1)"
        + " and rm.stock.id = ?1 ")
    List<RentalManage> findByStockAndStatusIn(String Id);  
}
