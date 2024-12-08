package vn.edu.iuh.fit.service;import org.springframework.data.domain.Page;import org.springframework.data.domain.Pageable;import vn.edu.iuh.fit.dto.MonthlyReviewStatistics;import vn.edu.iuh.fit.dto.TourReviewStatistics;import vn.edu.iuh.fit.dto.UserReviewStatistics;import vn.edu.iuh.fit.dto.request.ReviewRequest;import vn.edu.iuh.fit.dto.respone.ReviewResponseDTO;import vn.edu.iuh.fit.entity.Review;import java.time.LocalDate;import java.time.LocalDateTime;import java.util.List;import java.util.UUID;public interface ReviewService extends CrudService<Review, Long> {     List<TourReviewStatistics> getAllTourReviewStatistics();     List<MonthlyReviewStatistics> getMonthlyReviewStatistics();     List<UserReviewStatistics> getUserReviewStatistics();     ReviewResponseDTO addReview(ReviewRequest reviewRequest);     ReviewResponseDTO updateReview(ReviewRequest reviewRequest, Long id);     ReviewResponseDTO getReviewById(Long id);     void deleteReview(Long id);//      Page<Review> getAllReviews(Pageable pageable);   Page<Review> getReviews(int page, int size, String keyword, Integer rating,                           LocalDate startDate, LocalDate endDate,                           Boolean active);      Review updateReviewStatus(long reviewId, boolean isActive);}