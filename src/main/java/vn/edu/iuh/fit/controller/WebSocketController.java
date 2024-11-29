package vn.edu.iuh.fit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.edu.iuh.fit.dto.CheckInResponeDTO;
import vn.edu.iuh.fit.dto.TourGuideDTO;
import vn.edu.iuh.fit.dto.request.NotifyRequest;
import vn.edu.iuh.fit.dto.respone.CheckInResponse;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.TourGuide;
import vn.edu.iuh.fit.entity.TourGuideAssignment;
import vn.edu.iuh.fit.enums.CheckInStatus;
import vn.edu.iuh.fit.repositories.BookingRepository;
import vn.edu.iuh.fit.repositories.TourGuideAssignmentRepository;
import vn.edu.iuh.fit.service.BookingService;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private TourGuideAssignmentRepository tourGuideAssignmentRepository;

    @MessageMapping("/notify")
    public void notify(@Payload NotifyRequest notify) {
        simpMessagingTemplate.convertAndSendToUser(notify.getReceiver() + "", "notify", notify);
    }


    @MessageMapping("/checkin")
    public void handleCheckIn(@Payload String qrContent) {
        try {
//            Long bookingId = Long.parseLong(qrContent.trim());
            Booking booking = bookingService.getById(qrContent);

            if (booking != null) {
                if (booking.getCheckinStatus() == CheckInStatus.CHECKED_IN) {
                    simpMessagingTemplate.convertAndSend(
                            "/checkin/response",
                            new CheckInResponse("INFO", "Đã checkin", null)
                    );
                } else {
                    booking.setCheckinStatus(CheckInStatus.CHECKED_IN);
                    booking.setCheckinTime(LocalDateTime.now());
                    bookingRepository.save(booking);

                    CheckInResponeDTO.UserDTO userDTO = new CheckInResponeDTO.UserDTO(
                            booking.getUser().getUserId(),
                            booking.getUser().getFullName()
                    );

                    CheckInResponeDTO.TourDTO tourDTO = new CheckInResponeDTO.TourDTO(
                            booking.getDeparture().getTour().getTourId(),
                            booking.getDeparture().getTour().getTourName(),
                            booking.getDeparture().getTour().getTourDescription(),
                            new CheckInResponeDTO.UserDTO(
                                    booking.getDeparture().getTour().getUser().getUserId(),
                                    booking.getDeparture().getTour().getUser().getFullName()
                            )
                    );

                    CheckInResponeDTO.DepartureDTO departureDTO = new CheckInResponeDTO.DepartureDTO(
                            booking.getDeparture().getDepartureId(),
                            booking.getDeparture().getStartDate(),
                            booking.getDeparture().getEndDate(),
                            tourDTO
                    );

                    List<TourGuideAssignment> assignments = tourGuideAssignmentRepository.findByDeparture_DepartureId(booking.getDeparture().getDepartureId());
                    TourGuideDTO tourGuideDTO = null;
                    if (!assignments.isEmpty()) {
                        TourGuide tourGuide = assignments.get(0).getTourGuide();
                        tourGuideDTO = new TourGuideDTO(
                                tourGuide.getUserId(),
                                tourGuide.getFullName(),
                                tourGuide.getExperienceYear()
                        );
                    }

                    CheckInResponeDTO responseDTO = new CheckInResponeDTO(
                            booking.getBookingId(),
                            userDTO,
                            departureDTO,
                            tourGuideDTO
                    );

                    // Send success response
                    simpMessagingTemplate.convertAndSend(
                            "/checkin/response",
                            new CheckInResponse("SUCCESS", "Check in thành công", responseDTO)
                    );
                }
            } else {
                // Booking not found
                simpMessagingTemplate.convertAndSend(
                        "/checkin/response",
                        new CheckInResponse("ERROR", "Không tồn tại booking", null)
                );
            }
        } catch (NumberFormatException e) {
            // Invalid QR code format
            simpMessagingTemplate.convertAndSend(
                    "/checkin/response",
                    new CheckInResponse("ERROR", "Lỗi mã QR", null)
            );
        } catch (Exception e) {
            // Error processing check-in
            simpMessagingTemplate.convertAndSend(
                    "/checkin/response",
                    new CheckInResponse("ERROR", "Đã xảy ra lỗi khi xử lý check-in", null)
            );
        }
    }
}
