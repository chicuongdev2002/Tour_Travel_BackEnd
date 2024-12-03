package vn.edu.iuh.fit.service;

import vn.edu.iuh.fit.entity.Notification;
import vn.edu.iuh.fit.entity.User;

import java.util.List;

public interface NotificationService extends CrudService<Notification, Long> {
    List<Notification> findAllByUserOrderByCreateDateDesc(User user);
    Notification findBySenderAndMessage(User sender, String message);
}
