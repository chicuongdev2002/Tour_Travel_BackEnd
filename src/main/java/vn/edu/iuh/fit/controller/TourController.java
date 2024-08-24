package vn.edu.iuh.fit.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.service.TourService;import java.util.List;@RestController@RequestMapping("/api/tours")public class TourController {    @Autowired    private TourService tourService;    @PostMapping    public ResponseEntity<Tour> createTour(@RequestBody Tour tour) {        Tour createdTour = tourService.createTour(tour);        return new ResponseEntity<>(createdTour, HttpStatus.CREATED);    }    @GetMapping("/{id}")    public ResponseEntity<Tour> getTourById(@PathVariable("id") Long id) {        Tour tour = tourService.getTourById(id);        return new ResponseEntity<>(tour, HttpStatus.OK);    }    @GetMapping    public ResponseEntity<List<Tour>> getAllTours() {        List<Tour> tours = tourService.getAllTours();        return new ResponseEntity<>(tours, HttpStatus.OK);    }    @PutMapping("/{id}")    public ResponseEntity<Tour> updateTour(@PathVariable("id") Long id, @RequestBody Tour tour) {        // Ensure the ID in the path matches the ID in the body        if (!id.equals(tour.getTourId())) {            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);        }        Tour updatedTour = tourService.updateTour(tour);        return new ResponseEntity<>(updatedTour, HttpStatus.OK);    }    @DeleteMapping("/{id}")    public ResponseEntity<Void> deleteTour(@PathVariable("id") Long id) {        tourService.deleteTour(id);        return new ResponseEntity<>(HttpStatus.NO_CONTENT);    }}