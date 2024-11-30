package vn.edu.iuh.fit.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import vn.edu.iuh.fit.dto.FavoriteTourDTO;import vn.edu.iuh.fit.dto.TourListDTO;import vn.edu.iuh.fit.entity.FavoriteTour;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.service.FavoriteTourService;import java.util.List;import java.util.Set;@RestController@RequestMapping("/api/favorite-tours")public class FavoriteTourController {    @Autowired    private FavoriteTourService favoriteTourService;    @PostMapping    public ResponseEntity<FavoriteTourDTO> addFavoriteTour(@RequestParam Long userId, @RequestParam Long tourId) {        try {            FavoriteTourDTO favoriteTourDTO = favoriteTourService.addFavoriteTour(userId, tourId);            return new ResponseEntity<>(favoriteTourDTO, HttpStatus.CREATED);        } catch (IllegalArgumentException e) {            return new ResponseEntity<>(HttpStatus.NOT_FOUND);        }    }    @DeleteMapping    public ResponseEntity<Void> removeFavoriteTour(@RequestParam Long userId, @RequestParam Long tourId) {        favoriteTourService.removeFavoriteTour(userId, tourId);        return ResponseEntity.noContent().build();    }    @GetMapping("/favorites/{userId}")    public ResponseEntity<List<TourListDTO>> getFavoriteTours(@PathVariable Long userId) {        try {            List<TourListDTO> favoriteTours = favoriteTourService.getFavoriteToursByUserId(userId);            return new ResponseEntity<>(favoriteTours, HttpStatus.OK);        } catch (IllegalArgumentException e) {            return new ResponseEntity<>(HttpStatus.NOT_FOUND);        }    }}