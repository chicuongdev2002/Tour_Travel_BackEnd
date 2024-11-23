package vn.edu.iuh.fit.repositories;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.data.jpa.repository.Query;import org.springframework.data.repository.query.Param;import org.springframework.stereotype.Repository;import vn.edu.iuh.fit.entity.Departure;import vn.edu.iuh.fit.entity.TourPricing;import java.util.List;import java.util.UUID;@Repositorypublic interface TourPricingRepository extends JpaRepository<TourPricing, Long> {    @Query("SELECT tp FROM TourPricing tp JOIN FETCH tp.departure d WHERE d.departureId IN :departureIds")    List<TourPricing> findTourPricingByDepartureIds(@Param("departureIds") List<Long> departureIds);    List<TourPricing> findByDeparture(Departure departure);}