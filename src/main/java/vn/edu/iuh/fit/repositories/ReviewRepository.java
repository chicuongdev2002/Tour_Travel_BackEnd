package vn.edu.iuh.fit.repositories;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.data.jpa.repository.Query;import org.springframework.stereotype.Repository;import vn.edu.iuh.fit.entity.Review;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.entity.TourGuide;import vn.edu.iuh.fit.entity.User;import java.util.List;import java.util.UUID;@Repositorypublic interface ReviewRepository extends JpaRepository<Review, Long> {    List<Review> findReviewByTour(Tour tour);    List<Review> findReviewByUser(User user);}