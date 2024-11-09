package vn.edu.iuh.fit.service.implement;import jakarta.transaction.Transactional;import lombok.RequiredArgsConstructor;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.http.ResponseEntity;import org.springframework.messaging.simp.SimpMessagingTemplate;import org.springframework.stereotype.Service;import vn.edu.iuh.fit.dto.AddressDTO;import vn.edu.iuh.fit.dto.TourGuideKPIDTO;import vn.edu.iuh.fit.dto.TourAssignmentDTO;import vn.edu.iuh.fit.dto.TourGuideUpdateDTO;import vn.edu.iuh.fit.dto.request.TourGuideAssignmentRequestDTO;import vn.edu.iuh.fit.entity.*;import vn.edu.iuh.fit.enums.AccountRole;import vn.edu.iuh.fit.enums.AssignStatus;import vn.edu.iuh.fit.exception.ResourceNotFoundException;import vn.edu.iuh.fit.repositories.*;import vn.edu.iuh.fit.service.TourGuideService;import org.springframework.data.domain.Page;import org.springframework.data.domain.PageRequest;import org.springframework.data.domain.Pageable;import java.time.LocalDateTime;import java.util.ArrayList;import java.util.Date;import java.util.List;import java.util.Optional;import java.util.stream.Collectors;@Service@RequiredArgsConstructor@Transactionalpublic class TourGuideServiceImpl extends AbstractCrudService<TourGuide, Long> implements TourGuideService {    @Autowired    private TourGuideRepository tourGuideRepository;    @Autowired    private TourGuideAssignmentRepository tourGuideAssignmentRepository;    @Override    protected JpaRepository<TourGuide, Long> getRepository() {        return tourGuideRepository;    }    private final TourGuideAssignmentRepository assignmentRepository;    @Autowired    private DepartureRepository departureRepository;    @Autowired    private SimpMessagingTemplate messagingTemplate;    public List<TourAssignmentDTO> getGuideAssignments(Long guideId, AssignStatus status) {        TourGuide guide = tourGuideRepository.findById(guideId)                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found"));        List<TourGuideAssignment> assignments;        if (status != null) {            assignments = assignmentRepository.findByTourGuideUserIdAndStatus(guideId, status);        } else {            assignments = assignmentRepository.findByTourGuideUserId(guideId);        }        return assignments.stream()                .map(this::convertToAssignmentDTO)                .collect(Collectors.toList());    }    public Page<TourAssignmentDTO> getAllAssignments(int page, int size) {        Pageable pageable = PageRequest.of(page, size);        Page<TourGuideAssignment> assignmentPage = assignmentRepository.findAll(pageable);        return assignmentPage.map(this::convertToAssignmentDTO);    }    @Transactional    public void updateAssignmentStatus(Long guideId, Long departureId, AssignStatus status) {        TourGuideAssignment assignment = assignmentRepository                .findByTourGuideUserIdAndDepartureDepartureId(guideId, departureId)                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công công việc với hướng dẫn viên có id: " + guideId + " and departureId: " + departureId));        if (assignment.getStatus() != AssignStatus.TODO) {            throw new IllegalStateException("Không thể cập nhật trạng thái của phân công công việc đã chấp nhận hoặc đã hủy");        }        assignment.setStatus(status);        assignment.setAssignmentDate(LocalDateTime.now());        TourGuideAssignment updatedAssignment = assignmentRepository.save(assignment);        TourAssignmentDTO updatedDTO = convertToAssignmentDTO(updatedAssignment);//        messagingTemplate.convertAndSend("/topic/assignments", updatedDTO);    }    public TourGuideKPIDTO getKPI(long userId) {        Optional<TourGuide> tourGuideOpt = tourGuideRepository.findById(userId);        if (!tourGuideOpt.isPresent()) {            return null;        }        TourGuide tourGuide = tourGuideOpt.get();        TourGuideKPIDTO kpi = new TourGuideKPIDTO();        kpi.setUserId(tourGuide.getUserId());        kpi.setFullName(tourGuide.getFullName());        // Tính toán KPI        int totalAssignedTours = tourGuideAssignmentRepository.countByTourGuide(tourGuide);        kpi.setTotalAssignedTours(totalAssignedTours);        int todoTours = tourGuideAssignmentRepository.countTodoToursByTourGuide(tourGuide);        kpi.setTodoTours(todoTours);        int completeTours = tourGuideAssignmentRepository.countCompletedToursByTourGuide(tourGuide);        kpi.setCompletedTours(completeTours);        int acceptTours = tourGuideAssignmentRepository.countAcceptToursByTourGuide(tourGuide);        kpi.setAcceptTours(acceptTours);        int rejectTours = tourGuideAssignmentRepository.countRejectToursByTourGuide(tourGuide);        kpi.setRejectTours(rejectTours);        float averageRating = tourGuideAssignmentRepository.findAverageRatingByTourGuide(tourGuide);        kpi.setAverageRating(averageRating);        // Tỷ lệ khách hàng hài lòng        int satisfactoryReviews = assignmentRepository.countSatisfactoryReviewsByTourGuide(tourGuide);        int totalReviews = assignmentRepository.countTotalReviewsByTourGuide(tourGuide);        float satisfactionRate = (totalReviews > 0) ? ((float) satisfactoryReviews / totalReviews) * 100 : 0;        kpi.setSatisfactionRate(satisfactionRate);        // Thời gian trung bình cho mỗi chuyến đi        float averageDuration = assignmentRepository.findAverageDurationByTourGuide(tourGuide);        kpi.setAverageDuration(averageDuration);        int totalDuration = assignmentRepository.countTotalDurationByTourGuide(tourGuide);        kpi.setTotalDuration(totalDuration);        // Tổng số khách hàng đã phục vụ        int uniqueCustomers = assignmentRepository.countUniqueCustomersByTourGuide(tourGuide);        kpi.setUniqueCustomers(uniqueCustomers);        // Tỷ lệ hủy chuyến đi        int canceledTours = assignmentRepository.countRejectToursByTourGuide(tourGuide);        float cancellationRate = (totalAssignedTours > 0) ? ((float) canceledTours / totalAssignedTours) * 100 : 0;        kpi.setRejectRate(cancellationRate);        // Tỷ lệ chấp nhận chuyến đi        float acceptRate = (totalAssignedTours > 0) ? ((float) acceptTours / totalAssignedTours) * 100 : 0;        kpi.setAcceptRate(acceptRate);        // Tỷ lệ hoàn thành chuyến đi        float completeRate = (totalAssignedTours > 0) ? ((float) completeTours / acceptTours) * 100 : 0;        kpi.setCompleteRate(completeRate);        // Số lượng tour đã hoàn thành theo loại hình tour        List<Object[]> completedToursByType = assignmentRepository.countCompletedToursByType(tourGuide);        kpi.setCompletedToursByType(completedToursByType);        // Thống kê phản hồi từ khách hàng        List<Object[]> reviews = assignmentRepository.findReviewsByTourGuide(tourGuide);        kpi.setCustomerFeedback(reviews);        return kpi;    }    private TourAssignmentDTO convertToAssignmentDTO(TourGuideAssignment assignment) {        Departure departure = assignment.getDeparture();        Tour tour = departure.getTour();        return TourAssignmentDTO.builder()                .departureId(departure.getDepartureId())                .tourName(tour.getTourName())                .startDate(departure.getStartDate())                .endDate(departure.getEndDate())                .status(assignment.getStatus())                .assignmentDate(assignment.getAssignmentDate())                .maxParticipants(departure.getMaxParticipants())                .availableSeats(departure.getAvailableSeats())                .guideName(assignment.getTourGuide().getFullName())                .guideId(assignment.getTourGuide().getUserId())                .build();    }    public List<TourGuide> getAllTourGuides() {        return tourGuideRepository.findAllByRole(AccountRole.TOURGUIDE);    }    public void updateTourGuide(TourGuideUpdateDTO updateDTO) {        TourGuide existingTourGuide = tourGuideRepository.findById(updateDTO.getUserId())                .orElseThrow(() -> new ResourceNotFoundException("Tour guide not found"));        existingTourGuide.setEmail(updateDTO.getEmail());        existingTourGuide.setFullName(updateDTO.getFullName());        existingTourGuide.setPhoneNumber(updateDTO.getPhoneNumber());        existingTourGuide.setExperienceYear(updateDTO.getExperienceYear());        existingTourGuide.getAddresses().clear();        List<AddressDTO> updatedAddresses = updateDTO.getAddresses();        if (updatedAddresses != null) {            for (AddressDTO updatedAddress : updatedAddresses) {                Address addressEntity = new Address();                addressEntity.setAddressId(updatedAddress.getAddressId());                addressEntity.setAddress(updatedAddress.getAddress());                addressEntity.setUser(existingTourGuide);                existingTourGuide.getAddresses().add(addressEntity);            }        }        tourGuideRepository.save(existingTourGuide);    }    @Transactional    public void updateTourAssignment(Long guideId, Long departureId, TourAssignmentDTO updateDTO) {        TourGuideAssignment assignment = tourGuideAssignmentRepository                .findByTourGuideUserIdAndDepartureDepartureId(guideId, departureId)                .orElseThrow(() -> new ResourceNotFoundException("Tour assignment not found for guideId: " + guideId + " and departureId: " + departureId));        if (updateDTO.getStatus() != null) {            assignment.setStatus(updateDTO.getStatus());        }        if (updateDTO.getDepartureId() != null) {            Departure newDeparture = departureRepository.findById(updateDTO.getDepartureId())                    .orElseThrow(() -> new ResourceNotFoundException("Departure not found for ID: " + updateDTO.getDepartureId()));            TourGuideAssignment newAssignment = new TourGuideAssignment();            newAssignment.setAssignmentDate(LocalDateTime.now());            newAssignment.setStatus(assignment.getStatus());            newAssignment.setDeparture(newDeparture);            newAssignment.setTourGuide(assignment.getTourGuide());            tourGuideAssignmentRepository.save(newAssignment);            tourGuideAssignmentRepository.delete(assignment);        } else {            assignment.setAssignmentDate(LocalDateTime.now());            tourGuideAssignmentRepository.save(assignment);        }    }    @Transactional    public List<TourGuideAssignment> assignTourGuide(TourGuideAssignmentRequestDTO requestDTO) {        Departure departure = departureRepository.findById(requestDTO.getDepartureId())                .orElseThrow(() -> new ResourceNotFoundException("Không có chuyến đi với ID: " + requestDTO.getDepartureId()));        List<TourGuideAssignment> assignments = new ArrayList<>();        List<String> messages = new ArrayList<>(); // Để lưu thông báo trùng lặp        for (Long guideId : requestDTO.getGuideIds()) {            TourGuide tourGuide = tourGuideRepository.findById(guideId)                    .orElseThrow(() -> new ResourceNotFoundException("Hướng dẫn viên không tồn tại với ID: " + guideId));            // Kiểm tra xem phân công đã tồn tại chưa            Optional<TourGuideAssignment> existingAssignment = tourGuideAssignmentRepository                    .findByTourGuideUserIdAndDepartureDepartureId(guideId, departure.getDepartureId());            if (existingAssignment.isPresent()) {                messages.add("Phân công đã tồn tại cho hướng dẫn viên ID " + guideId + " và Chuyến đi ID " + departure.getDepartureId());                continue;            }            TourGuideAssignment assignment = new TourGuideAssignment();            assignment.setDeparture(departure);            assignment.setTourGuide(tourGuide);            assignment.setStatus(AssignStatus.TODO);            assignment.setAssignmentDate(LocalDateTime.now());            assignments.add(tourGuideAssignmentRepository.save(assignment));        }        // Nếu có thông báo trùng lặp, ném một ngoại lệ hoặc trả về thông báo        if (!messages.isEmpty()) {            throw new IllegalStateException(String.join(", ", messages));        }        return assignments;    }    public void deleteTourGuideAssignment(Long guideId, Long departureId) {        TourGuideAssignment assignment = tourGuideAssignmentRepository                .findByTourGuideUserIdAndDepartureDepartureId(guideId, departureId)                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phân công công việc với hướng dẫn viên có id: " + guideId + " and departureId: " + departureId));        tourGuideAssignmentRepository.delete(assignment);    }}