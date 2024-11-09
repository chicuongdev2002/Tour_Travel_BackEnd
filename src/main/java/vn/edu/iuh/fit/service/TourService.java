package vn.edu.iuh.fit.service;import org.springframework.data.domain.Page;import org.springframework.hateoas.PagedModel;import org.springframework.web.multipart.MultipartFile;import vn.edu.iuh.fit.dto.TourDetailDTO;import vn.edu.iuh.fit.dto.TourSimpleDTO;import vn.edu.iuh.fit.dto.TourInfoDTO;import vn.edu.iuh.fit.dto.TourSummaryDTO;import vn.edu.iuh.fit.dto.TourWithDeparturesDTO;import vn.edu.iuh.fit.entity.Destination;import vn.edu.iuh.fit.entity.Image;import vn.edu.iuh.fit.entity.Tour;import java.io.File;import java.io.IOException;import java.math.BigDecimal;import java.util.List;import java.util.UUID;public interface TourService extends CrudService<Tour, Long> {    List<Tour> findToursByDestination(Destination destination);    Page<TourSummaryDTO> getTours(String keyword, int page, int size, BigDecimal minPrice, BigDecimal maxPrice,                                  String tourType, String startLocation, String participantType);    //Lấy chi tiết tour theo id    TourDetailDTO getTourById(long id);    Tour convertDtoToEntity(TourDetailDTO tourDetailDTO);    String uploadImageToAWS(File file) throws IOException;    File convertMultiPartToFile(MultipartFile file) throws IOException;    List<TourSimpleDTO> getAllTours();    List<TourWithDeparturesDTO> getAllToursAndDeparture();    TourInfoDTO convertToDTO(Tour tour);}