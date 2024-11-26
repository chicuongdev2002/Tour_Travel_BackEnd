package vn.edu.iuh.fit.service;

import vn.edu.iuh.fit.entity.Discount;
import vn.edu.iuh.fit.entity.Tour;

public interface DiscountService extends CrudService<Discount, Long> {
    Discount getDiscountByTour(Tour tour);
}
