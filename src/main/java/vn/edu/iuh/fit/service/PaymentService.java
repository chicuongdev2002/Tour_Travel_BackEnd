package vn.edu.iuh.fit.service;

import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.Payment;

public interface PaymentService extends CrudService<Payment, Long> {
    Payment getPaymentByBooking(String bookingId);
}
