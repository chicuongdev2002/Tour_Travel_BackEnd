package vn.edu.iuh.fit.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.domain.Page;import org.springframework.data.domain.PageRequest;import org.springframework.data.domain.Pageable;import org.springframework.format.annotation.DateTimeFormat;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import vn.edu.iuh.fit.dto.MonthlyReviewStatistics;import vn.edu.iuh.fit.dto.TourReviewStatistics;import vn.edu.iuh.fit.dto.UserReviewStatistics;import vn.edu.iuh.fit.dto.request.ReviewRequest;import vn.edu.iuh.fit.dto.respone.ReviewResponseDTO;import vn.edu.iuh.fit.entity.Review;import vn.edu.iuh.fit.service.ReviewService;import java.time.LocalDate;import java.time.LocalDateTime;import java.util.List;@RestController@RequestMapping("/api/reviews")@CrossOrigin(        origins = "https://two2-webtour.onrender.com",        allowedHeaders = "*",        allowCredentials = "true")public class ReviewController {    @Autowired    private ReviewService reviewService;    @GetMapping("/statistics-tour-review")    public List<TourReviewStatistics> getAllTourReviewStatistics() {        return reviewService.getAllTourReviewStatistics();    }    @GetMapping("/monthly-statistics")    public List<MonthlyReviewStatistics> getMonthlyReviewStatistics() {        return reviewService.getMonthlyReviewStatistics();    }    @GetMapping("/user-statistics")    public List<UserReviewStatistics> getUserReviewStatistics() {        return reviewService.getUserReviewStatistics();    }    @PostMapping    public ResponseEntity<ReviewResponseDTO> addReview(@RequestBody ReviewRequest reviewRequest) {        try {            ReviewResponseDTO savedReview = reviewService.addReview(reviewRequest);            return new ResponseEntity<>(savedReview, HttpStatus.CREATED);        } catch (IllegalArgumentException e) {            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);        }    }    @PutMapping("/{id}")    public ResponseEntity<ReviewResponseDTO> editReview(@PathVariable Long id, @RequestBody ReviewRequest reviewRequest) {        try {            ReviewResponseDTO existingReview = reviewService.getReviewById(id);            if (existingReview == null) {                return new ResponseEntity<>(HttpStatus.NOT_FOUND);            }            ReviewResponseDTO updatedReview = reviewService.updateReview(reviewRequest, id);            return new ResponseEntity<>(updatedReview, HttpStatus.OK);        } catch (IllegalArgumentException e) {            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);        }    }    @DeleteMapping("/{id}")    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {        try {            reviewService.deleteReview(id);            return new ResponseEntity<>(HttpStatus.NO_CONTENT);        } catch (IllegalArgumentException e) {            return new ResponseEntity<>(HttpStatus.NOT_FOUND);        }    }//    @GetMapping//    public ResponseEntity<Page<Review>> getAllReviews(//            @RequestParam(defaultValue = "0") int page,//            @RequestParam(defaultValue = "10") int size) {//        Pageable pageable = PageRequest.of(page, size);//        Page<Review> reviews = reviewService.getAllReviews(pageable);//        return ResponseEntity.ok(reviews);//    }@GetMappingpublic Page<Review> getReviews(        @RequestParam(defaultValue = "0") int page,        @RequestParam(defaultValue = "10") int size,        @RequestParam(required = false) String keyword,        @RequestParam(required = false) Integer rating,        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,        @RequestParam(required = false) Boolean active) {    return reviewService.getReviews(page, size, keyword, rating, startDate, endDate, active);}    @PatchMapping("/{reviewId}/status")    public ResponseEntity<Review> updateReviewStatus(            @PathVariable long reviewId,            @RequestParam boolean isActive) {        Review updatedReview = reviewService.updateReviewStatus(reviewId, isActive);        return ResponseEntity.ok(updatedReview);    }}