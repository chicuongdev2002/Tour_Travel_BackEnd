package vn.edu.iuh.fit.controller;

import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.dto.BookingDTO;
import vn.edu.iuh.fit.dto.BookingDetailDTO;
import vn.edu.iuh.fit.dto.BookingViewDetailDTO;
import vn.edu.iuh.fit.dto.respone.ItineraryResponse;
import vn.edu.iuh.fit.dto.BookingHasPrice;
import vn.edu.iuh.fit.entity.*;
import vn.edu.iuh.fit.enums.CheckInStatus;
import vn.edu.iuh.fit.mailservice.EmailService;
import vn.edu.iuh.fit.repositories.BookingRepository;
import vn.edu.iuh.fit.repositories.ExtendBookingRepository;
import vn.edu.iuh.fit.repositories.NotificationRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.service.BookingService;
import vn.edu.iuh.fit.service.DepartureService;
import vn.edu.iuh.fit.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.service.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private EmailService emailService;


    @Autowired
    private TourPricingService tourPricingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private ExtendBookingRepository extendBookingRepository;
    @Autowired
    private TourService tourService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;


    @PostMapping("/createBooking")
    @Transactional(rollbackFor = Exception.class)
    public synchronized ResponseEntity<BookingDTO> createBooking(@RequestParam String bookingID,
                                                                 @RequestParam long userId,
                                                                 @RequestParam long departureId,
                                                                 @RequestParam String participants,
                                                                 @RequestParam String address,
                                                                 @RequestParam(required = false) String paymentMethod) throws Exception {
        BookingDTO bookingDTO;
        try{
            User user = userService.getById(userId);
            Departure departure = departureService.getById(departureId);
            Tour tour = tourService.getById(departure.getTour().getTourId());
            User tourProvider = userService.getById(tour.getUser().getUserId());
            tour.setUser(tourProvider);
            departure.setTour(tour);
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
                    .checkinStatus(CheckInStatus.NOT_CHECKED_IN)
                    .checkinTime(LocalDateTime.now())
                    .build();
            bookingDTO = bookingService.convertDTO(bookingService.create(booking));

            if(paymentMethod != null){
                Payment payment = Payment.builder()
                        .booking(booking)
                        .amount(tourPricingService.calculatePrice(booking))
                        .paymentMethod(PaymentMethod.CASH)
                        .build();
                paymentService.create(payment);
                byte[] qrCodeBase64 = generateQRCode(booking.getBookingId());
                System.out.println("QR Code Base64: " + qrCodeBase64);
                emailService.sendBookingConfirmationEmail(user.getEmail(), bookingDTO, qrCodeBase64);
            }
        } catch (Exception e){
            if(e.getMessage().equals("Không đủ chỗ trống!"))
                throw new Exception(e.getMessage());
            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");
        }
        return ResponseEntity.ok(bookingDTO);
    }

    public byte[] generateQRCode(String bookingId) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(String.valueOf(bookingId), BarcodeFormat.QR_CODE, 200, 200);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] qrCodeBytes = baos.toByteArray();
        return qrCodeBytes;
//        return Base64.getEncoder().encodeToString(qrCodeBytes);
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
    public ResponseEntity<Page<BookingViewDetailDTO>> getPageBookingHasPrice(@RequestParam(defaultValue = "0") int page,
                                                                             @RequestParam(defaultValue = "10") int size,
                                                                             @RequestParam(required = false) String sortBy,
                                                                             @RequestParam(required = false) String sortDirection,
                                                                             @RequestParam(required = false) long userId){
        Page<Booking> lstBooking = bookingService.getPageList(page, size, sortBy, sortDirection);
        List<Booking> lst;
        if(userId == 0)
            lst = lstBooking.getContent();
        else
            lst = lstBooking.getContent().stream().filter(b -> b.getUser().getUserId() == userId).toList();
        Page<BookingViewDetailDTO> pageBooking = new PageImpl<>(
                lst.stream().map(b -> {
                    ExtendBooking extendBooking = extendBookingRepository.findById(b.getBookingId()).orElse(null);
                    if(extendBooking == null)
                        return null;
                    User user = userService.getById(b.getUser().getUserId());
                    return BookingViewDetailDTO.builder()
                            .extendBooking(extendBooking)
                            .participants(b.getParticipants())
                            .active(b.isActive())
                            .fullName(user.getFullName())
                            .build();
                }).toList());
        return new ResponseEntity<>(pageBooking, HttpStatus.OK);
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<String> updateStatusBooking(@RequestParam String bookingId){
        Booking booking = bookingService.getById(bookingId);
        if(booking == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy đơn đặt tour");
        if(booking.isActive()){
            Notification notification = Notification.builder()
                    .sender(booking.getUser())
                    .receiver(User.builder().userId(21).build())
                    .createDate(LocalDateTime.now())
                    .messages("$$##Cancel_Booking##$$"+bookingId)
                    .build();
            notificationService.create(notification);
        }
        else{
            Notification n = notificationService.findBySenderAndMessage(booking.getUser(), "$$##Cancel_Booking##$$"+bookingId);
            notificationService.delete(n.getId());
        }
        booking.setActive(!booking.isActive());
        bookingService.update(booking);
        ExtendBooking extendBooking = extendBookingRepository.findById(bookingId).orElse(null);
        return ResponseEntity.status(HttpStatus.OK).body("Update thành công");
    }
    //lây danh sách booking theo bookingId
    @GetMapping("/getBookingById")  //api/bookings/getBookingById?bookingId=1
    public ResponseEntity<Booking> getBookingById(@RequestParam String bookingId){
        Booking booking = bookingService.getById(bookingId);
        if(booking == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        return ResponseEntity.status(HttpStatus.OK).body(booking);
    }
    @GetMapping("/itinerary/{userId}")
    public ItineraryResponse getUserItinerary(@PathVariable long userId) {
        return bookingService.getItineraryByUserId(userId);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingDetailDTO>> getBookingsByUserId(@PathVariable long userId) {
        List<BookingDetailDTO> bookings = bookingService.getBookingsByUserId(userId);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    @DeleteMapping("/cancelBooking/{id}")
    @Transactional
    public ResponseEntity<String> cancelBooking(@PathVariable String id, @RequestParam long userId){
        Payment payment = paymentService.getPaymentByBooking(id);
        User user = userService.getById(userId);
        Notification notification = notificationService.findBySenderAndMessage(user, "$$##Cancel_Booking##$$"+id);
        paymentService.delete(payment.getPaymentId());
        bookingService.delete(id);
        ExtendBooking eb = extendBookingRepository.findById(id).orElse(null);
        if (eb != null)
            extendBookingRepository.delete(eb);
        notificationService.delete(notification.getId());
        return ResponseEntity.ok("Complete");
    }

    @GetMapping("/getBooking/{id}")
    public ResponseEntity<BookingViewDetailDTO> getBooking(@PathVariable String id){
        ExtendBooking eb = extendBookingRepository.findById(id).orElse(null);
        if(eb == null)
            return ResponseEntity.ok(null);
        Booking booking = bookingRepository.findById(eb.getBookingId()).orElse(null);
        if(booking == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        User user = userRepository.findById(booking.getUser().getUserId()).orElse(null);
        if(user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        BookingViewDetailDTO bookingViewDetailDTO = BookingViewDetailDTO.builder()
                .extendBooking(eb)
                .participants(booking.getParticipants())
                .fullName(user.getFullName())
                .active(booking.isActive())
                .build();
        return ResponseEntity.ok(bookingViewDetailDTO);
    }

    @GetMapping("/getByTourId")
    public ResponseEntity<List<BookingViewDetailDTO>> getExtendByTourId(@RequestParam long tourId){
        List<ExtendBooking> lst = extendBookingRepository.findAllByTourId(tourId);
        List<BookingViewDetailDTO> response = new ArrayList<>();
        if(lst == null || lst.isEmpty())
            return ResponseEntity.ok(null);
        lst.forEach(e -> {
            BookingViewDetailDTO bookingViewDetailDTO = getBooking(e.getBookingId()).getBody();
            response.add(bookingViewDetailDTO);
        });
        return ResponseEntity.ok(response);
    }
}