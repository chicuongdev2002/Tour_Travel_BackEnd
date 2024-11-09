package vn.edu.iuh.fit.service;import jakarta.transaction.Transactional;import org.springframework.data.domain.Page;import org.springframework.hateoas.PagedModel;import org.springframework.http.ResponseEntity;import org.springframework.web.multipart.MultipartFile;import vn.edu.iuh.fit.dto.*;import vn.edu.iuh.fit.dto.request.TourGuideAssignmentRequestDTO;import vn.edu.iuh.fit.entity.*;import vn.edu.iuh.fit.enums.AssignStatus;import vn.edu.iuh.fit.exception.ResourceNotFoundException;import java.io.File;import java.io.IOException;import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.List;import java.util.UUID;import java.util.stream.Collectors;public interface TourGuideService extends CrudService<TourGuide, Long> {     List<TourAssignmentDTO> getGuideAssignments(Long guideId, AssignStatus status);    Page<TourAssignmentDTO> getAllAssignments(int page, int size);    void updateAssignmentStatus(Long guideId, Long departureId, AssignStatus status);    TourGuideKPIDTO getKPI(long userId);    List<TourGuide> getAllTourGuides();    void updateTourGuide(TourGuideUpdateDTO updateDTO);    void updateTourAssignment(Long guideId, Long departureId, TourAssignmentDTO updateDTO);    List<TourGuideAssignment> assignTourGuide(TourGuideAssignmentRequestDTO requestDTO);    void deleteTourGuideAssignment(Long guideId, Long departureId);}