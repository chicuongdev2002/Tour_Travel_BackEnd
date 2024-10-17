package vn.edu.iuh.fit.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.domain.Page;import org.springframework.hateoas.PagedModel;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import vn.edu.iuh.fit.dto.TourDetailDTO;import vn.edu.iuh.fit.dto.TourSummaryDTO;import vn.edu.iuh.fit.entity.Image;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.service.TourService;import java.io.File;import java.io.IOException;import java.math.BigDecimal;import java.util.HashSet;import java.util.List;import java.util.UUID;@RestController@RequestMapping("/api/tours")public class TourController {    private TourService tourService;    @Autowired    public TourController(TourService tourService) {        this.tourService = tourService;    }    @PostMapping    public ResponseEntity<Tour> createTour(            @RequestParam("file") MultipartFile file,            @ModelAttribute Tour tour) {        try {            // Upload the image to S3            File tempFile = tourService.convertMultiPartToFile(file);            String fileUrl = tourService.uploadImageToAWS(tempFile, tour);            // Tạo một thực thể Image            Image image = new Image();            image.setImageUrl(fileUrl);            image.setTour(tour); // Liên kết ảnh với tour            // Khởi tạo tập hợp images nếu nó null            if (tour.getImages() == null) {                tour.setImages(new HashSet<>());            }            // Kiểm tra xem ảnh đã tồn tại chưa            boolean imageExists = tour.getImages().stream()                    .anyMatch(existingImage -> existingImage.getImageUrl().equals(fileUrl));            if (!imageExists) {                tour.getImages().add(image); // Chỉ thêm ảnh nếu chưa tồn tại            }            // Tạo tour mới            Tour createdTour = tourService.create(tour);            return new ResponseEntity<>(createdTour, HttpStatus.CREATED);        } catch (IOException e) {            // Trả về một đối tượng Tour rỗng với trạng thái INTERNAL_SERVER_ERROR            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)                    .body(new Tour()); // Trả về một Tour rỗng để phù hợp với kiểu mong đợi        }    }//    @GetMapping("/{id}")//    public ResponseEntity<Tour> getTourById(@PathVariable("id") UUID id) {//        Tour tour = tourService.getById(id);//        return tour != null//                ? new ResponseEntity<>(tour, HttpStatus.OK)//                : new ResponseEntity<>(HttpStatus.NOT_FOUND);//    }////@GetMapping("/search")//public ResponseEntity<Page<TourSummaryDTO>> searchTours(//        @RequestParam String keyword,//        @RequestParam(defaultValue = "0") int page,//        @RequestParam(defaultValue = "20") int size) {////    Page<TourSummaryDTO> tours = tourService.searchTours(keyword, page, size);//    return ResponseEntity.ok(tours);//}    @GetMapping("/all")    public ResponseEntity<List<Tour>> getAllTours() {        List<Tour> tours = tourService.getAll();        return new ResponseEntity<>(tours, HttpStatus.OK);    }//@GetMapping//public ResponseEntity<Page<TourSummaryDTO>> getTours(//        @RequestParam(defaultValue = "0") int page,//        @RequestParam(defaultValue = "10") int size,//        @RequestParam(required = false, defaultValue = "0") BigDecimal minPrice,//        @RequestParam(required = false, defaultValue = "10000") BigDecimal maxPrice) {//    Page<TourSummaryDTO> tours = tourService.getTours(page, size, minPrice, maxPrice);//    System.out.println(tours);//    return ResponseEntity.ok(tours);//}//@GetMapping//public ResponseEntity<Page<TourSummaryDTO>> getTours(//        @RequestParam(required = false) String keyword,//        @RequestParam(defaultValue = "0") int page,//        @RequestParam(defaultValue = "10") int size,//        @RequestParam(required = false, defaultValue = "0") BigDecimal minPrice,//        @RequestParam(required = false, defaultValue = "10000") BigDecimal maxPrice) {////    Page<TourSummaryDTO> tours = tourService.getTours(keyword, page, size, minPrice, maxPrice);//    System.out.println(tours);//    return ResponseEntity.ok(tours);//}@GetMappingpublic ResponseEntity<Page<TourSummaryDTO>> getTours(        @RequestParam(required = false) String keyword,        @RequestParam(defaultValue = "0") int page,        @RequestParam(defaultValue = "10") int size,        @RequestParam(required = false, defaultValue = "0") BigDecimal minPrice,        @RequestParam(required = false, defaultValue = "10000000") BigDecimal maxPrice,        @RequestParam(required = false) String tourType,        @RequestParam(required = false) String startLocation,        @RequestParam(required = false) String participantType) {    Page<TourSummaryDTO> tours = tourService.getTours(keyword, page, size, minPrice, maxPrice,            tourType, startLocation, participantType);    return ResponseEntity.ok(tours);}    @GetMapping("/{id}")    public ResponseEntity<TourDetailDTO> getTourById(@PathVariable long id) {        TourDetailDTO tour = tourService.getTourById(id);        System.out.println(tour);        return ResponseEntity.ok(tour);    }    @PutMapping("/{id}")    public ResponseEntity<Tour> updateTour(@PathVariable("id") long id, @RequestBody Tour tour) {        if (id != tour.getTourId()) {            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);        }        tourService.update(tour);        return new ResponseEntity<>(tour, HttpStatus.OK);    }    @DeleteMapping("/{id}")    public ResponseEntity<Void> deleteTour(@PathVariable("id") long id) {        tourService.delete(id);        return new ResponseEntity<>(HttpStatus.NO_CONTENT);    }    @PostMapping("/upload")    public ResponseEntity<String> uploadImage(            @RequestParam("file") MultipartFile file,            @RequestParam("tourId") long tourId) {        try {            TourDetailDTO tourDetail = tourService.getTourById(tourId);            Tour tour = tourService.convertDtoToEntity(tourDetail);            // Kiểm tra và khởi tạo tập hợp            if (tour.getImages() == null) {                tour.setImages(new HashSet<>());            }            File tempFile = tourService.convertMultiPartToFile(file);            String fileUrl = tourService.uploadImageToAWS(tempFile, tour);            return ResponseEntity.ok(fileUrl);        } catch (IOException e) {            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());        }    }}