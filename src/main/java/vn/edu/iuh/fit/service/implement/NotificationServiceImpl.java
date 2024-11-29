package vn.edu.iuh.fit.service.implement;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entity.Notification;
import vn.edu.iuh.fit.entity.User;
import vn.edu.iuh.fit.repositories.NotificationRepository;
import vn.edu.iuh.fit.service.NotificationService;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl extends AbstractCrudService<Notification, Long> implements NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Override
    protected JpaRepository<Notification, Long> getRepository() {
        return notificationRepository;
    }

    @Override
    public List<Notification> findAllByUserOrderByCreateDateDesc(User user) {
        return notificationRepository.findAllBySenderOrReceiverOrderByCreateDateDesc(user, user);
    }
}
