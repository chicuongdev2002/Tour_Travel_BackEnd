
package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.entity.Discount;
import vn.edu.iuh.fit.entity.Tour;

import java.time.LocalDateTime;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
  Discount findFirstByTourAndCountUseIsNullAndStartDateBeforeAndEndDateAfterOrderByDiscountAmountDesc(Tour tour, LocalDateTime startDate, LocalDateTime endDate);
}

