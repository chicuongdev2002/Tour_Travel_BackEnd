package vn.edu.iuh.fit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.entity.Destination;
import vn.edu.iuh.fit.entity.Tour;
import vn.edu.iuh.fit.entity.TourDestination;
import vn.edu.iuh.fit.service.DestinationService;
import vn.edu.iuh.fit.service.TourDestinationService;
import vn.edu.iuh.fit.service.TourService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/destinations")
public class DestinationController {
    @Autowired
    DestinationService destinationService;

    @Autowired
    TourDestinationService tourDestinationService;

    @Autowired
    TourService tourService;

    @GetMapping
    public ResponseEntity<Object> getListByTourId(@RequestParam long tourId){
        Tour tour = tourService.getById(tourId);
        if(tour == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Destination not found!");
        List<Destination> destinations = new ArrayList<>();
        for(TourDestination tourDestination : tourDestinationService.findAllByTour(tour)){
            destinations.add(destinationService.getById(tourDestination.getDestination().getDestinationId()));
        }
        return ResponseEntity.ok(destinations);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Destination>> getPageBooking(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(required = false) String sortBy,
                                                        @RequestParam(required = false) String sortDirection){
        Page<Destination> pageDestination = destinationService.getPageDestination(page, size, sortBy, sortDirection);
        return new ResponseEntity<>(pageDestination, HttpStatus.OK);
    }
}
