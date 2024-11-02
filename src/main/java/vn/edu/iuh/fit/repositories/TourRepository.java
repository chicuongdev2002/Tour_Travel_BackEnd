package vn.edu.iuh.fit.repositories;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entity.Departure;
import vn.edu.iuh.fit.entity.Tour;
import vn.edu.iuh.fit.enums.ParticipantType;
import vn.edu.iuh.fit.enums.TourType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    @Cacheable(value = "toursCache", key = "#minPrice + '-' + #maxPrice")
    @Query("SELECT t, tp, d.startDate, d.availableSeats, d.maxParticipants ," +
            "(SELECT GROUP_CONCAT(img.imageUrl) FROM Image img WHERE img.tour.tourId = t.tourId) " +
            "FROM Tour t " +
            "JOIN Departure d ON d.tour.tourId = t.tourId " +
            "JOIN TourPricing tp ON d.departureId = tp.departure.departureId " +
            "WHERE tp.price = (SELECT MIN(tp2.price) FROM TourPricing tp2 " +
            "JOIN Departure d2 ON d2.departureId = tp2.departure.departureId " +
            "WHERE d2.tour.tourId = t.tourId AND tp2.price BETWEEN :minPrice AND :maxPrice) " +
            "AND d.startDate = (SELECT MIN(d2.startDate) FROM Departure d2 WHERE d2.tour.tourId = t.tourId) " +
            "GROUP BY t.tourId")
    Page<Object[]> findToursWithPriceRange(@Param("minPrice") BigDecimal minPrice,
                                           @Param("maxPrice") BigDecimal maxPrice,
                                           Pageable pageable);
    @Query("SELECT t, tp, d.startDate, d.availableSeats, d.maxParticipants ," +
            "(SELECT GROUP_CONCAT(img.imageUrl) FROM Image img WHERE img.tour.tourId = t.tourId) " +
            "FROM Tour t " +
            "JOIN Departure d ON d.tour.tourId = t.tourId " +
            "JOIN TourPricing tp ON d.departureId = tp.departure.departureId " +
            "WHERE (LOWER(t.tourName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.startLocation) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND d.startDate = (SELECT MIN(d2.startDate) FROM Departure d2 WHERE d2.tour.tourId = t.tourId) " +
            "AND tp.price = (SELECT MIN(tp2.price) FROM TourPricing tp2 " +
            "JOIN Departure d2 ON d2.departureId = tp2.departure.departureId " +
            "WHERE d2.tour.tourId = t.tourId) " +
            "GROUP BY t.tourId")
    Page<Object[]> findtTourByKeyword(@Param("keyword") String keyword, Pageable pageable);
    @Query("SELECT t, tp, d.startDate, d.availableSeats,d.maxParticipants , " +
            "(SELECT GROUP_CONCAT(img.imageUrl) FROM Image img WHERE img.tour.tourId = t.tourId) " +
            "FROM Tour t " +
            "JOIN Departure d ON d.tour.tourId = t.tourId " +
            "JOIN TourPricing tp ON d.departureId = tp.departure.departureId " +
            "WHERE (:minPrice IS NULL OR tp.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR tp.price <= :maxPrice) " +
            "AND (:tourType IS NULL OR t.tourType = :tourType) " +
            "AND (:startLocation IS NULL OR LOWER(t.startLocation) LIKE LOWER(CONCAT('%', :startLocation, '%'))) " +
            "AND (:participantType IS NULL OR tp.participantType = :participantType) " +
            "AND d.startDate = (SELECT MIN(d2.startDate) FROM Departure d2 WHERE d2.tour.tourId = t.tourId) " +
            "AND tp.price = (SELECT MIN(tp2.price) FROM TourPricing tp2 " +
            "JOIN Departure d2 ON d2.departureId = tp2.departure.departureId " +
            "WHERE d2.tour.tourId = t.tourId) " +
            "GROUP BY t.tourId")
    Page<Object[]> searchTours(@Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               @Param("tourType") TourType tourType,
                               @Param("startLocation") String startLocation,
                               @Param("participantType") ParticipantType participantType,
                               Pageable pageable);
    @EntityGraph(attributePaths = {"departures", "tourDestinations", "reviews"})
    Optional<Tour> findById(long id);
    //Tìm kiếm tour theo tên tour hoặc địa điểm bắt đầu
    List<Tour> findByTourNameContainingIgnoreCaseOrStartLocationContainingIgnoreCase(String tourName, String startLocation);

    @Query("SELECT DISTINCT t FROM Tour t " +
            "LEFT JOIN FETCH t.departures d " +
            "LEFT JOIN FETCH t.images " +
            "LEFT JOIN FETCH t.reviews r " +
            "LEFT JOIN FETCH r.user " +
            "LEFT JOIN FETCH t.tourDestinations td " +
            "LEFT JOIN FETCH td.destination dest " +
            "LEFT JOIN FETCH dest.images " +
            "WHERE t.tourId = :id")
    Optional<Tour> findTourWithAllDetails(@Param("id") Long id);
    @Query("SELECT COUNT(tp) FROM TourPricing tp WHERE tp.departure.departureId IN :departureIds")
    long countTourPricingsByDepartureIds(@Param("departureIds") List<Long> departureIds);
}