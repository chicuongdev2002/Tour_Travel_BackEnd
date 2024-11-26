package vn.edu.iuh.fit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dto.BookingDTO;
import vn.edu.iuh.fit.dto.BookingHasPrice;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.Departure;
import vn.edu.iuh.fit.entity.Payment;
import vn.edu.iuh.fit.entity.User;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.service.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin("*")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private DepartureService departureService;

    @Autowired
    private TourPricingService tourPricingService;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/createBooking")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<BookingDTO> createBooking(@RequestParam String bookingID,
                                                    @RequestParam long userId,
                                                    @RequestParam long departureId,
                                                    @RequestParam String participants,
                                                    @RequestParam String address,
                                                    @RequestParam(required = false) String paymentMethod) throws Exception {
        BookingDTO bookingDTO;
        try{
            User user = userService.getById(userId);
            Departure departure = departureService.getById(departureId);
            String[] arrParticipant = participants.split(",");
            int newAvailableSeats = departure.getAvailableSeats() - Integer.parseInt(arrParticipant[0]) - Integer.parseInt(arrParticipant[1]) - Integer.parseInt(arrParticipant[2]);
            if(newAvailableSeats < 0) {
                throw new Exception("Không đủ chỗ trống!");
            }
            departure.setAvailableSeats(newAvailableSeats);
            departureService.update(departure);
            Booking booking = Booking.builder()
                    .bookingId(bookingID)
                    .user(user)
                    .departure(departure)
                    .bookingDate(LocalDateTime.now())
                    .participants(participants)
                    .isActive(true)
                    .address(address)
                    .build();
            bookingDTO = bookingService.convertDTO(bookingService.create(booking));
            if(paymentMethod != null){
                Payment payment = Payment.builder()
                        .booking(booking)
                        .amount(tourPricingService.calculatePrice(booking))
                        .paymentMethod(PaymentMethod.CASH)
                        .build();
                paymentService.create(payment);
            }
        } catch (Exception e){
            if(e.getMessage().equals("Không đủ chỗ trống!"))
                throw new Exception(e.getMessage());
            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");
        }
        return ResponseEntity.ok(bookingDTO);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBooking() {
        List<Booking> bookings = bookingService.getAll();
        return new ResponseEntity<>(bookings, HttpStatus.CREATED);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Booking>> getPageBooking(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String sortBy,
                                                        @RequestParam(required = false) String sortDirection){
        Page<Booking> pageBooking = bookingService.getPageList(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(pageBooking, HttpStatus.OK);
    }

    @GetMapping("/page/has-price")
    public ResponseEntity<Page<BookingHasPrice>> getPageBookingHasPrice(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size,
                                                                        @RequestParam(required = false) String sortBy,
                                                                        @RequestParam(required = false) String sortDirection){
        Page<BookingHasPrice> pageBooking = bookingService.getPageList(page, size, sortBy, sortDirection)
                .map(b -> {
                    BookingHasPrice bookingHasPrice = new BookingHasPrice();
                    bookingHasPrice.setBooking(b);
                    bookingHasPrice.setPrice(tourPricingService.calculatePrice(b));
                    Payment payment = paymentService.getPaymentByBooking(b.getBookingId());
                    bookingHasPrice.setPaymentDate(payment.getPaymentDate());
                    bookingHasPrice.setPaymentMethod(payment.getPaymentMethod());
                    return bookingHasPrice;
                });
        return new ResponseEntity<>(pageBooking, HttpStatus.OK);
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateStatusBooking(@RequestParam String bookingId){
        Booking booking = bookingService.getById(bookingId);
        if(booking == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy đơn đặt tour");
        booking.setActive(!booking.isActive());
        bookingService.update(booking);
        return ResponseEntity.status(HttpStatus.OK).body("Update thành công");
    }
}