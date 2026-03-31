package vn.edu.iuh.fit.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entity.Review;
import vn.edu.iuh.fit.entity.Tour;
import vn.edu.iuh.fit.entity.User;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findReviewByTour(Tour tour);
    List<Review> findReviewByUser(User user);
    Page<Review> findAll(Pageable pageable);
    List<Review> findByTour_TourIdIn(List<Long> tourIds);
}
