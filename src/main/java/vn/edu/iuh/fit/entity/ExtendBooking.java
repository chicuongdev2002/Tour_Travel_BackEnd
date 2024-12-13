package vn.edu.iuh.fit.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.enums.TourType;

import java.time.LocalDateTime;

@Entity
@Table(name = "extend_bookings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExtendBooking {
    @Id
    private String bookingId;
    private long departureId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int maxParticipants;
    private long price;
    private String tourName;
    private String description;
    private int duration;
    private String startLocation;
    @Enumerated(EnumType.STRING)
    private TourType tourType;
    private String tourProviderName;
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;
    private String address;
    private String phoneNumber;
    private String email;
    private long tourId;
}
