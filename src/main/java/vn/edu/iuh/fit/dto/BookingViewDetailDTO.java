package vn.edu.iuh.fit.dto;

import lombok.Builder;
import lombok.Data;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.ExtendBooking;
import vn.edu.iuh.fit.enums.TourType;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingViewDetailDTO {
    private ExtendBooking extendBooking;
    private String participants;
    private String fullName;
    private boolean active;
}
