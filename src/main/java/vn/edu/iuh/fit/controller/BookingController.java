package vn.edu.iuh.fit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.Departure;
import vn.edu.iuh.fit.entity.User;
import vn.edu.iuh.fit.service.BookingService;
import vn.edu.iuh.fit.service.DepartureService;
import vn.edu.iuh.fit.service.UserService;

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

    @PostMapping("/createBooking")
    public ResponseEntity<Booking> createBooking(@RequestParam long userId,
                                            @RequestParam long departureId,
                                            @RequestParam String participants) {
        User user = userService.getById(userId);
        Departure departure = departureService.getById(departureId);
        Booking booking = Booking.builder()
                .user(user)
                .departure(departure)
                .bookingDate(LocalDateTime.now())
                .participants(participants)
                .isActive(true)
                .build();
        Booking newBooking = bookingService.create(booking);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
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
        Page<Booking> pageBooking = bookingService.getPageBooking(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(pageBooking, HttpStatus.OK);
    }
}