package vn.edu.iuh.fit.service.implement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entity.Payment;
import vn.edu.iuh.fit.repositories.PaymentRepository;
import vn.edu.iuh.fit.service.PaymentService;

@Service
public class PaymentServiceImpl extends AbstractCrudService<Payment, Long> implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    protected JpaRepository<Payment, Long> getRepository() {
        return paymentRepository;
    }
}
