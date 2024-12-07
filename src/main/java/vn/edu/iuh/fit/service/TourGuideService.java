package vn.edu.iuh.fit.service;import org.springframework.data.domain.Page;import vn.edu.iuh.fit.dto.*;import vn.edu.iuh.fit.dto.request.TourGuideAssignmentRequestDTO;import vn.edu.iuh.fit.entity.*;import vn.edu.iuh.fit.enums.AssignStatus;import java.time.LocalDateTime;import java.util.List;public interface TourGuideService extends CrudService<TourGuide, Long> {     List<TourAssignmentDTO> getGuideAssignments(Long guideId, AssignStatus status);    Page<TourAssignmentDTO> getAllAssignments(int page, int size);    void updateAssignmentStatus(Long guideId, Long departureId, AssignStatus status);    TourGuideKPIDTO getKPI(long userId);    List<TourGuide> getAllTourGuides();    void updateTourGuide(TourGuideUpdateDTO updateDTO);    void updateTourAssignment(Long guideId, Long departureId, TourAssignmentDTO updateDTO);    List<TourGuideAssignment> assignTourGuide(TourGuideAssignmentRequestDTO requestDTO);    void deleteTourGuideAssignment(Long guideId, Long departureId);TourGuideAssignmentStatisticsDTO getSystemAssignmentStatistics();    List<TourGuideWorkingHoursDTO> getTotalWorkingHoursByTourGuides(LocalDateTime startDate, LocalDateTime endDate);     List<TourScheduleDTO> getToursByGuide(Long tourGuideId);     void markAttendance(AttendanceDTO attendanceDTO);}