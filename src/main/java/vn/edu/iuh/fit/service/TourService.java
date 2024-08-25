package vn.edu.iuh.fit.service;import org.springframework.data.domain.Page;import vn.edu.iuh.fit.dto.TourDetailDTO;import vn.edu.iuh.fit.dto.TourSummaryDTO;import vn.edu.iuh.fit.entity.Destination;import vn.edu.iuh.fit.entity.Tour;import java.math.BigDecimal;import java.util.List;import java.util.UUID;public interface TourService extends CrudService<Tour, UUID> {    List<Tour> findToursByDestination(Destination destination);    List<Tour> findToursByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);    //Lấy danh sách tour theo trang    Page<TourSummaryDTO> getTours(int page, int size, BigDecimal minPrice, BigDecimal maxPrice);    //Lấy chi tiết tour theo id    TourDetailDTO getTourById(UUID id);}