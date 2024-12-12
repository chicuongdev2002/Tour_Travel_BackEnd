package vn.edu.iuh.fit.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.ExtendBooking;
import vn.edu.iuh.fit.entity.Payment;
import vn.edu.iuh.fit.repositories.ExtendBookingRepository;
import vn.edu.iuh.fit.repositories.PaymentRepository;
import vn.edu.iuh.fit.service.PaymentService;

import java.util.List;

@Service
public class PaymentServiceImpl extends AbstractCrudService<Payment, Long> implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ExtendBookingRepository extendBookingRepository;

    @Override
    protected JpaRepository<Payment, Long> getRepository() {
        return paymentRepository;
    }

    @Override
    public Payment getPaymentByBooking(String bookingId) {
        List<Payment> lst = paymentRepository.findByBooking_BookingId(bookingId);
        return lst.isEmpty() ? null: lst.get(0);
    }

    @Override
    public Payment create(Payment payment){
        ExtendBooking extendBooking = extendBookingRepository.findById(payment.getBooking().getBookingId()).orElse(null);
        if(extendBooking == null)
            return paymentRepository.save(payment);
        extendBooking.setPaymentMethod(payment.getPaymentMethod());
        extendBooking.setPaymentDate(payment.getPaymentDate());
        extendBookingRepository.save(extendBooking);
        return paymentRepository.save(payment);
    }
}
