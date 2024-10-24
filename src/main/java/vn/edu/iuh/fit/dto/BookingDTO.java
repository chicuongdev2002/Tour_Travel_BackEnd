package vn.edu.iuh.fit.dto;import lombok.AllArgsConstructor;import lombok.Data;import lombok.NoArgsConstructor;import java.time.LocalDateTime;import java.util.List;@Data@AllArgsConstructor@NoArgsConstructorpublic class BookingDTO {    private long bookingId;    private LocalDateTime bookingDate;    private Integer numberOfParticipants;    private boolean isActive;    private TourInfoDTO tour;    private DepartureDTO departure;    private List<PaymentInfoDTO> payments;}