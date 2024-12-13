package vn.edu.iuh.fit.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.iuh.fit.entity.ExtendBooking;

import java.util.List;

public interface ExtendBookingRepository extends JpaRepository<ExtendBooking, String> {
    List<ExtendBooking> findAllByTourId(long tourId);
}