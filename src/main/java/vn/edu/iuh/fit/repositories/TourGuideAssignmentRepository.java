package vn.edu.iuh.fit.repositories;import org.springframework.data.domain.Page;import org.springframework.data.domain.Pageable;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.data.jpa.repository.Query;import org.springframework.data.repository.query.Param;import org.springframework.stereotype.Repository;import vn.edu.iuh.fit.entity.Review;import vn.edu.iuh.fit.entity.TourGuide;import vn.edu.iuh.fit.entity.TourGuideAssignment;import vn.edu.iuh.fit.enums.AssignStatus;import vn.edu.iuh.fit.pks.TourGuideAssignmentId;import java.time.LocalDateTime;import java.util.List;import java.util.Optional;@Repositorypublic interface TourGuideAssignmentRepository extends JpaRepository<TourGuideAssignment, TourGuideAssignmentId> {    List<TourGuideAssignment> findByDeparture_DepartureId(long departureId); List<TourGuideAssignment> findByTourGuide(TourGuide tourGuide);//    Page<TourGuideAssignment> findAll(Pageable pageable);   Page<TourGuideAssignment> findAllByOrderByAssignmentDateDesc(Pageable pageable);    @Query("SELECT a FROM TourGuideAssignment a " +            "JOIN FETCH a.tourGuide " +            "JOIN FETCH a.departure " +            "WHERE a.departure.departureId IN :departureIds")    List<TourGuideAssignment> findByDeparture_DepartureIdIn(@Param("departureIds") List<Long> departureIds);    List<TourGuideAssignment> findByTourGuideUserId(Long guideId);    List<TourGuideAssignment> findByTourGuideUserIdAndStatus(Long guideId, AssignStatus status);    Optional<TourGuideAssignment> findByTourGuideUserIdAndDepartureDepartureId(Long guideId, Long departureId);    int countByTourGuide(TourGuide tourGuide);    @Query("SELECT COUNT(tga) FROM TourGuideAssignment tga WHERE tga.tourGuide = ?1 AND tga.status = 'ACCEPT'")    int countAcceptToursByTourGuide(TourGuide tourGuide);    @Query("SELECT COUNT(tga) FROM TourGuideAssignment tga WHERE tga.tourGuide = ?1 AND tga.status = 'REJECT'")    int countRejectToursByTourGuide(TourGuide tourGuide);    @Query("SELECT COUNT(tga) FROM TourGuideAssignment tga WHERE tga.tourGuide = ?1 AND tga.status = 'TODO'")    int countTodoToursByTourGuide(TourGuide tourGuide);    @Query("""    SELECT AVG(r.rating)     FROM Review r     JOIN Tour t ON r.tour = t     JOIN TourGuideAssignment tga ON t = tga.departure.tour     WHERE tga.tourGuide = ?1""")    Float  findAverageRatingByTourGuide(TourGuide tourGuide);    @Query("""    SELECT COUNT(tga)     FROM TourGuideAssignment tga     JOIN tga.departure d     WHERE tga.tourGuide = ?1 AND d.endDate < CURRENT_TIMESTAMP AND tga.status = 'ACCEPT'""")    int countCompletedToursByTourGuide(TourGuide tourGuide);    // Tỷ lệ khách hàng hài lòng    @Query("""    SELECT COUNT(r.rating)     FROM Review r     JOIN Tour t ON r.tour = t     JOIN TourGuideAssignment tga ON t = tga.departure.tour     WHERE tga.tourGuide = ?1 AND r.rating >= 4""")    int countSatisfactoryReviewsByTourGuide(TourGuide tourGuide);    @Query("""    SELECT COUNT(r.rating)     FROM Review r     JOIN Tour t ON r.tour = t     JOIN TourGuideAssignment tga ON t = tga.departure.tour     WHERE tga.tourGuide = ?1 """)    int countTotalReviewsByTourGuide(TourGuide tourGuide);    @Query("""    SELECT SUM(t.duration)    FROM Tour t     JOIN TourGuideAssignment tga ON t = tga.departure.tour      JOIN tga.departure d     WHERE tga.tourGuide = ?1 AND d.endDate < CURRENT_TIMESTAMP AND tga.status = 'ACCEPT'""")    Integer countTotalDurationByTourGuide(TourGuide tourGuide);    // Thời gian trung bình cho mỗi chuyến đi    @Query("SELECT AVG(DATEDIFF(d.endDate, d.startDate)) FROM Departure d JOIN TourGuideAssignment tga ON d = tga.departure WHERE tga.tourGuide = ?1")    float findAverageDurationByTourGuide(TourGuide tourGuide);    // Tổng số khách hàng đã phục vụ    @Query("SELECT COUNT(DISTINCT b.user) FROM Booking b JOIN TourGuideAssignment tga ON b.departure = tga.departure WHERE tga.tourGuide = ?1")    int countUniqueCustomersByTourGuide(TourGuide tourGuide);//    @Query("SELECT SUM(b.) FROM Booking b JOIN TourGuideAssignment tga ON b.departure = tga.departure WHERE tga.tourGuide = ?1")//    Integer countTotalParticipantsByTourGuide(TourGuide tourGuide);    // Số lượng tour được phân công    @Query("SELECT COUNT(tga) FROM TourGuideAssignment tga WHERE tga.tourGuide = ?1")    int countAssignedToursByTourGuide(TourGuide tourGuide);    // Số lượng tour đã hoàn thành theo loại hình tour    @Query("SELECT t.tourType, COUNT(tga) FROM TourGuideAssignment tga JOIN tga.departure d JOIN d.tour t WHERE tga.tourGuide = ?1 AND tga.status='ACCEPT' AND d.endDate < CURRENT_TIMESTAMP GROUP BY t.tourType")    List<Object[]> countCompletedToursByType(TourGuide tourGuide);    // Thống kê phản hồi từ khách hàng// Phương thức này có thể tùy chỉnh theo yêu cầu cụ thể    @Query("""    SELECT DISTINCT r.comment, r.reviewDate    FROM Review r    JOIN Tour t ON r.tour = t     JOIN TourGuideAssignment tga ON t = tga.departure.tour     WHERE tga.tourGuide = ?1""")    List<Object[]> findReviewsByTourGuide(TourGuide tourGuide);    @Query("SELECT COUNT(a) FROM TourGuideAssignment a")    int countAllAssignments();    @Query("SELECT COUNT(a) FROM TourGuideAssignment a WHERE a.status = 'TODO'")    int countAlTodoAssignments();    @Query("SELECT COUNT(a) FROM TourGuideAssignment a WHERE a.status = 'ACCEPT'")    int countAllAcceptedAssignments();    @Query("SELECT COUNT(a) FROM TourGuideAssignment a WHERE a.status = 'REJECT'")    int countAllRejectedAssignments();    @Query("""    SELECT COUNT(tga)     FROM TourGuideAssignment tga     JOIN tga.departure d     WHERE d.endDate < CURRENT_TIMESTAMP AND tga.status = 'ACCEPT'""")    int countTotalCompletedToursByTourGuide();    @Query("""    SELECT tga.tourGuide.fullName, SUM(t.duration)     FROM TourGuideAssignment tga     JOIN tga.departure d     JOIN d.tour t     WHERE d.startDate >= :startDate AND d.endDate <= :endDate AND  tga.status = 'ACCEPT'    GROUP BY tga.tourGuide""")    List<Object[]> countTotalWorkingHoursByTourGuide(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);}