package vn.edu.iuh.fit.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entity.Discount;
import vn.edu.iuh.fit.entity.Tour;
import vn.edu.iuh.fit.repositories.DiscountRepository;
import vn.edu.iuh.fit.service.DiscountService;

import java.time.LocalDateTime;

@Service
public class DiscountServiceImpl extends AbstractCrudService<Discount, Long> implements DiscountService {
    @Autowired
    private DiscountRepository discountRepository;
    @Override
    protected JpaRepository<Discount, Long> getRepository() {
        return discountRepository;
    }

    @Override
    public Discount getDiscountByTour(Tour tour) {
        return discountRepository.findFirstByTourAndCountUseIsNullAndStartDateBeforeAndEndDateAfterOrderByDiscountAmountDesc(tour, LocalDateTime.now(), LocalDateTime.now());
    }
}
