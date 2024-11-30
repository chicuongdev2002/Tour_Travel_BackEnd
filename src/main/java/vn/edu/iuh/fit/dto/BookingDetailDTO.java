package vn.edu.iuh.fit.dto;import lombok.Data;import vn.edu.iuh.fit.enums.CheckInStatus;import java.time.LocalDateTime;import java.util.List;@Datapublic class BookingDetailDTO {    private String bookingId;    private LocalDateTime bookingDate;    private String participants;    private boolean isActive;    private String address;    private CheckInStatus checkinStatus;    private LocalDateTime checkinTime;    private TourInfoDTO tour;    private DepartureDTO departure;    private List<PaymentInfoDTO> payments;    private TourGuideDTO tourGuide;    private UserInfoDTO tourProvider;}