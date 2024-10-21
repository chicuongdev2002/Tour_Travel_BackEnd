package vn.edu.iuh.fit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.Departure;
import vn.edu.iuh.fit.entity.Tour;
import vn.edu.iuh.fit.entity.User;
import vn.edu.iuh.fit.service.BookingService;
import vn.edu.iuh.fit.service.DepartureService;
import vn.edu.iuh.fit.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
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
                                            @RequestParam int numberOfParticipants) {
        User user = userService.getById(userId);
        Departure departure = departureService.getById(departureId);
        Booking booking = Booking.builder()
                .user(user)
                .departure(departure)
                .bookingDate(LocalDateTime.now())
                .numberOfParticipants(numberOfParticipants)
                .isActive(true)
                .build();
        Booking newBooking = bookingService.createBooking(booking);
        return new ResponseEntity<>(newBooking, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBooking() {
        List<Booking> bookings = bookingService.getAll();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }
}
