package vn.edu.iuh.fit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dto.Message;
import vn.edu.iuh.fit.dto.NotificationDTO;
import vn.edu.iuh.fit.entity.Notification;
import vn.edu.iuh.fit.entity.User;
import vn.edu.iuh.fit.service.NotificationService;
import vn.edu.iuh.fit.service.UserService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
//@CrossOrigin(
//        origins = "https://two2-webtour.onrender.com",
//        allowedHeaders = "*",
//        allowCredentials = "true"
//)
public class NotifyController {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Page<Notification>> getList(@RequestParam(defaultValue = "0", required = false) int page,
                                                      @RequestParam(defaultValue = "10", required = false) int size,
                                                      @RequestParam(defaultValue = "createDate", required = false) String sortBy,
                                                      @RequestParam(defaultValue = "desc", required = false) String sortDirection){
        Page<Notification> notifyList = notificationService.getPageList(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(notifyList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<NotificationDTO>> getNotifyByUserId(@PathVariable long id,
                                                                @RequestParam(defaultValue = "0", required = false) int page,
                                                                @RequestParam(defaultValue = "10", required = false) int size){
        User user = userService.getById(id);
        List<NotificationDTO> result = new ArrayList<>();
        List<Notification> lst = notificationService.findAllByUserOrderByCreateDateDesc(user);
        int count = 0;
        for (Notification notification : lst){
            if(count++ == (page+1)*size*10)
                break;
            NotificationDTO notificationDTO = NotificationDTO.builder().user(
                    notification.getSender().getUserId() == id? notification.getReceiver() : notification.getSender()
            ).build();
            int index = result.indexOf(notificationDTO);
            Message message = Message.builder()
                    .isMe(notification.getSender().getUserId() == id)
                    .content(notification.getMessages())
                    .createDate(notification.getCreateDate())
                    .build();
            if(index == -1){
                List<Message> messages = new ArrayList<>();
                messages.add(message);
                notificationDTO.setMessages(messages);
                result.add(notificationDTO);
            } else {
                if(result.get(index).getMessages().size() < 10){
                    List<Message> messages = result.get(index).getMessages();
                    messages.add(message);
                    result.get(index).setMessages(messages);
                }
            }
        }
        if(page*size + size <= result.size())
            return ResponseEntity.ok(result.subList(page*size, page*size + size));
        return ResponseEntity.ok(result.subList(page*size, result.size()));
    }

    @PostMapping(consumes = {"application/json"})
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) throws Exception {
        Notification notify;
        try{
            User sender = userService.getById(notification.getSender().getUserId());
            User receiver = userService.getById(notification.getReceiver().getUserId());
            notification.setCreateDate(LocalDateTime.now(ZoneId.of("Asia/Bangkok")));
            notification.setSender(sender);
            notification.setReceiver(receiver);
            notify = notificationService.create(notification);
        } catch (Exception e){
            throw new Exception("Đã xảy ra lỗi khi tạo thông báo!");
        }
        return ResponseEntity.ok(notify);
    }
}