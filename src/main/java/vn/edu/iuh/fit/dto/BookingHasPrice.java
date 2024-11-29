package vn.edu.iuh.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.enums.PaymentMethod;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingHasPrice {
    private Booking booking;
    private long price;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
}
