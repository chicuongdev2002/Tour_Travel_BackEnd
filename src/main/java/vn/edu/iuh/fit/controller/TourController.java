package vn.edu.iuh.fit.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.domain.Page;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import vn.edu.iuh.fit.dto.TourDetailDTO;import vn.edu.iuh.fit.dto.TourInfoDTO;import vn.edu.iuh.fit.dto.TourSummaryDTO;import vn.edu.iuh.fit.dto.request.TourRequest;import vn.edu.iuh.fit.entity.*;import vn.edu.iuh.fit.service.*;import java.io.File;import java.io.IOException;import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.*;@RestController@RequestMapping("/api/tours")@CrossOrigin("*")public class TourController {    @Autowired    private TourService tourService;    @Autowired    private DestinationService destinationService;    @Autowired    private TourDestinationService tourDestinationService;    @Autowired    private TourPricingService tourPricingService;    @Autowired    private DepartureService departureService;    @Autowired    private ImageService imageService;    @PostMapping    public ResponseEntity<Tour> createTour(            @RequestParam("file") MultipartFile file,            @ModelAttribute Tour tour) {        try {            // Upload the image to S3            File tempFile = tourService.convertMultiPartToFile(file);            String fileUrl = tourService.uploadImageToAWS(tempFile);            // Tạo một thực thể Image            Image image = new Image();            image.setImageUrl(fileUrl);            image.setTour(tour); // Liên kết ảnh với tour            // Khởi tạo tập hợp images nếu nó null            if (tour.getImages() == null) {                tour.setImages(new HashSet<>());            }            // Kiểm tra xem ảnh đã tồn tại chưa            boolean imageExists = tour.getImages().stream()                    .anyMatch(existingImage -> existingImage.getImageUrl().equals(fileUrl));            if (!imageExists) {                tour.getImages().add(image); // Chỉ thêm ảnh nếu chưa tồn tại            }            // Tạo tour mới            Tour createdTour = tourService.create(tour);            return new ResponseEntity<>(createdTour, HttpStatus.CREATED);        } catch (IOException e) {            // Trả về một đối tượng Tour rỗng với trạng thái INTERNAL_SERVER_ERROR            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)                    .body(new Tour()); // Trả về một Tour rỗng để phù hợp với kiểu mong đợi        }    }    @GetMapping("/all")    public ResponseEntity<List<Tour>> getAllTours() {        List<Tour> tours = tourService.getAll();        return new ResponseEntity<>(tours, HttpStatus.OK);    }    @GetMapping    public ResponseEntity<Page<TourSummaryDTO>> getTours(            @RequestParam(required = false) String keyword,            @RequestParam(defaultValue = "0") int page,            @RequestParam(defaultValue = "10") int size,            @RequestParam(required = false, defaultValue = "0") BigDecimal minPrice,            @RequestParam(required = false, defaultValue = "10000000") BigDecimal maxPrice,            @RequestParam(required = false) String tourType,            @RequestParam(required = false) String startLocation,            @RequestParam(required = false) String participantType) {        Page<TourSummaryDTO> tours = tourService.getTours(keyword, page, size, minPrice, maxPrice,                tourType, startLocation, participantType);        return ResponseEntity.ok(tours);    }    @GetMapping("/{id}")    public ResponseEntity<TourDetailDTO> getTourById(@PathVariable long id) {        TourDetailDTO tour = tourService.getTourById(id);        System.out.println(tour);        return ResponseEntity.ok(tour);    }    @PutMapping("/{id}")    public ResponseEntity<Tour> updateTour(@PathVariable("id") long id, @RequestBody Tour tour) {        if (id != tour.getTourId()) {            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);        }        tourService.update(tour);        return new ResponseEntity<>(tour, HttpStatus.OK);    }    @DeleteMapping("/{id}")    public ResponseEntity<Void> deleteTour(@PathVariable("id") long id) {        tourService.delete(id);        return new ResponseEntity<>(HttpStatus.NO_CONTENT);    }    @PostMapping("/upload")    public ResponseEntity<String> uploadImage(            @RequestParam("file") MultipartFile file)    {        try {            File tempFile = tourService.convertMultiPartToFile(file);            String fileUrl = tourService.uploadImageToAWS(tempFile);            return ResponseEntity.ok(fileUrl);        } catch (IOException e) {            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());        }  catch (Exception e) {            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());        }    }    @PutMapping("/delete/{tourId}")    public ResponseEntity<String> updateStatusTour(@PathVariable long tourId) throws Exception {        try {            Tour tour = tourService.getById(tourId);            tour.setActive(false);            tourService.update(tour);        } catch (Exception e){            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");        }        return ResponseEntity.ok("Xoá tour thành công!");    }    @PutMapping("/updateTour")    @Transactional(rollbackFor = Exception.class)    public ResponseEntity<String> updateTour(@RequestBody TourRequest tourRequest) throws Exception{        try {            List<Long> listId = new ArrayList<>();            tourRequest.getDepartures().forEach(d -> listId.add(d.getDepartureId()));            tourService.update(tourRequest.getTour());            Tour tour = tourService.getById(tourRequest.getTour().getTourId());            List<Departure> departureSet = departureService.getListDepartureByTour(tour);            for(Departure departure : tourRequest.getDepartures()){                if(departure.getDepartureId() < 0)                    departure.setDepartureId(0);                if(!departureSet.contains(departure))                    departure.setAvailableSeats(departure.getMaxParticipants());                else {                    departure.setAvailableSeats(departureService.getById(departure.getDepartureId()).getAvailableSeats());                    departure.setActive(true);                }                departure.setTour(tourRequest.getTour());                departure.setActive(true);                departureService.update(departure);            }            for(Departure departure : departureSet){                if(!tourRequest.getDepartures().contains(departure)){                    departure.setActive(false);                    departureService.update(departure);                }            }            for (TourPricing tourPricing : tourRequest.getTourPricing()){                if(tourPricing.getDeparture().getDepartureId() <= 0){                    int index = listId.indexOf(tourPricing.getDeparture().getDepartureId());                    Departure d = departureService.getById(tourRequest.getDepartures().get(index).getDepartureId());                    tourPricing.setDeparture(d);                }                tourPricing.setModifiedDate(LocalDateTime.now());                tourPricingService.create(tourPricing);            }            List<TourDestination> tourDestinations = tourDestinationService.findAllByTour(tour);            List<TourDestination> tourDestinationsRequest = tourRequest.getTourDestinations();            for(int i=0; i < tourDestinationsRequest.size(); i++){                TourDestination td = tourDestinationsRequest.get(i);                td.setTour(tourRequest.getTour());                td.setSequenceOrder(i+1);                tourDestinationService.update(td);            }//            List<Destination> destinations = new ArrayList<>();//            tourDestinationsRequest.forEach(d -> destinations.add(d.getDestination()));            for(TourDestination tourDestination : tourDestinations){                if(!tourDestinationsRequest.contains(tourDestination)){                    tourDestination.setSequenceOrder(0);                    tourDestinationService.update(tourDestination);                }            }            if(tourRequest.getImages() != null && !tourRequest.getImages().isEmpty()){                for (Image image : tourRequest.getImages()){                    if(image.getImageUrl() != null && !image.getImageUrl().isEmpty()){                        image.setTour(tour);                        imageService.create(image);                    }                }            }        } catch (Exception e){            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");        }        return ResponseEntity.ok("Update thành công!");    }    @PostMapping("/addTour")    @Transactional(rollbackFor = Exception.class)    public ResponseEntity<TourInfoDTO> addTour(@RequestBody TourRequest tourRequest) throws Exception{        Tour tourCreated = null;        try {            Tour tour = Tour.builder().tourName(tourRequest.getTour().getTourName())                    .tourDescription(tourRequest.getTour().getTourDescription())                    .tourType(tourRequest.getTour().getTourType())                    .startLocation(tourRequest.getTour().getStartLocation())                    .duration(tourRequest.getTour().getDuration()).build();            Departure departure = Departure.builder().startDate(tourRequest.getDepartures().get(0).getStartDate())                    .endDate(tourRequest.getDepartures().get(0).getEndDate())                    .maxParticipants(tourRequest.getDepartures().get(0).getMaxParticipants())                    .availableSeats(tourRequest.getDepartures().get(0).getMaxParticipants())                    .isActive(true).tour(tour).build();            departure.setTour(tour);            tour.setDepartures(Set.of(departure));            tourCreated = tourService.create(tour);            for (TourPricing tourPricing : tourRequest.getTourPricing()){                tourPricing.setModifiedDate(LocalDateTime.now());                tourPricing.setDeparture(departure);                tourPricingService.create(tourPricing);            }            for (int i=0; i < tourRequest.getTourDestinations().size(); i++){                Destination destination = destinationService.getById(tourRequest.getTourDestinations().get(i).getDestination().getDestinationId());                if(destination != null) {                    TourDestination tourDestination = TourDestination.builder()                            .tour(tourCreated).destination(destination).sequenceOrder(i+1)                            .duration(tourRequest.getTourDestinations().get(i).getDuration()).build();                    tourDestinationService.create(tourDestination);                }            }            if(tourRequest.getImages() != null && !tourRequest.getImages().isEmpty()){                for (Image image : tourRequest.getImages()){                    if(image.getImageUrl() != null && !image.getImageUrl().isEmpty()){                        image.setTour(tour);                        imageService.create(image);                    }                }            }        } catch (Exception e){            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");        }        return ResponseEntity.ok(tourService.convertToDTO(tourCreated));    }}