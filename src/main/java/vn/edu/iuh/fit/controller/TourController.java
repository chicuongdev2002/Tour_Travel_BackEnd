package vn.edu.iuh.fit.controller;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.domain.Page;import org.springframework.data.domain.PageRequest;import org.springframework.data.domain.Pageable;import org.springframework.data.web.PageableDefault;import org.springframework.format.annotation.DateTimeFormat;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.transaction.annotation.Transactional;import org.springframework.web.bind.annotation.*;import org.springframework.web.multipart.MultipartFile;import vn.edu.iuh.fit.dto.*;import vn.edu.iuh.fit.dto.respone.RevenueStatisticsResponse;import vn.edu.iuh.fit.dto.respone.TourResponseDTO;import vn.edu.iuh.fit.entity.Destination;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.enums.ParticipantType;import vn.edu.iuh.fit.enums.TourType;import vn.edu.iuh.fit.exception.ResourceNotFoundException;import vn.edu.iuh.fit.service.DestinationService;import vn.edu.iuh.fit.service.TourDestinationService;import vn.edu.iuh.fit.service.TourService;import vn.edu.iuh.fit.dto.request.TourRequest;import vn.edu.iuh.fit.entity.*;import vn.edu.iuh.fit.enums.TourType;import vn.edu.iuh.fit.service.*;import java.io.File;import java.io.IOException;import java.math.BigDecimal;import java.time.LocalDate;import java.util.HashSet;import java.util.List;import vn.edu.iuh.fit.dto.request.TourRequest;import vn.edu.iuh.fit.entity.*;import java.time.LocalDateTime;import java.util.*;@RestController@RequestMapping("/api/tours")@CrossOrigin("*")public class TourController {    @Autowired    private TourService tourService;    @Autowired    private DestinationService destinationService;    @Autowired    private TourDestinationService tourDestinationService;    @Autowired    private TourPricingService tourPricingService;    @Autowired    private DepartureService departureService;    @Autowired    private ImageService imageService;    @GetMapping("/all")    public ResponseEntity<List<Tour>> getAllTours() {        List<Tour> tours = tourService.getAll();        return new ResponseEntity<>(tours, HttpStatus.OK);    }    @GetMapping    public ResponseEntity<Page<TourSummaryDTO>> getTours(            @RequestParam(required = false) String keyword,            @RequestParam(defaultValue = "0") int page,            @RequestParam(defaultValue = "10") int size,            @RequestParam(required = false, defaultValue = "0") BigDecimal minPrice,            @RequestParam(required = false, defaultValue = "10000000") BigDecimal maxPrice,            @RequestParam(required = false) String tourType,            @RequestParam(required = false) String startLocation,            @RequestParam(required = false) String participantType) {        Page<TourSummaryDTO> tours = tourService.getTours(keyword, page, size, minPrice, maxPrice,                tourType, startLocation, participantType);        return ResponseEntity.ok(tours);    }    @GetMapping("/test")    public Page<TourListDTO> getTours(            @RequestParam(defaultValue = "0") int page,            @RequestParam(defaultValue = "10") int size,            @RequestParam(required = false) String keyword,            @RequestParam(required = false) String startLocation,            @RequestParam(required = false) String tourType,            @RequestParam(required = false) String participantType,            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,            @RequestParam(required = false) BigDecimal minPrice,            @RequestParam(required = false) BigDecimal maxPrice,            @RequestParam(required = false) Long userId) {        return tourService.getToursTest(page, size, keyword, startLocation, tourType, participantType, startDate, endDate, minPrice, maxPrice, userId);    }    @GetMapping("/{id}")    public ResponseEntity<TourDetailDTO> getTourById(@PathVariable long id) {        TourDetailDTO tour = tourService.getTourById(id);        System.out.println(tour);        return ResponseEntity.ok(tour);    }    @PutMapping("/{id}")    public ResponseEntity<Tour> updateTour(@PathVariable("id") long id, @RequestBody Tour tour) {        if (id != tour.getTourId()) {            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);        }        tourService.update(tour);        return new ResponseEntity<>(tour, HttpStatus.OK);    }    @DeleteMapping("/{id}")    public ResponseEntity<Void> deleteTour(@PathVariable("id") long id) {        tourService.delete(id);        return new ResponseEntity<>(HttpStatus.NO_CONTENT);    }    @PostMapping("/upload")    public ResponseEntity<String> uploadImage(            @RequestParam("file") MultipartFile file) {        try {            File tempFile = tourService.convertMultiPartToFile(file);            String fileUrl = tourService.uploadImageToAWS(tempFile);            return ResponseEntity.ok(fileUrl);        } catch (IOException e) {            return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());        } catch (Exception e) {            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred: " + e.getMessage());        }    }    @PutMapping("/delete/{tourId}")    public ResponseEntity<String> updateStatusTour(@PathVariable long tourId) throws Exception {        try {            Tour tour = tourService.getById(tourId);            tour.setTourType(TourType.DELETE);            tourService.update(tour);        } catch (Exception e) {            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");        }        return ResponseEntity.ok("Xoá tour thành công!");    }    @PutMapping("/updateTour")    @Transactional(rollbackFor = Exception.class)    public ResponseEntity<String> updateTour(@RequestBody TourRequest tourRequest) throws Exception{        try {            List<Long> listId = new ArrayList<>();            tourRequest.getDepartures().forEach(d -> listId.add(d.getDepartureId()));            Tour newTour = tourRequest.getTour();            newTour.setActive(true);            tourService.update(newTour);            Tour tour = tourService.getById(tourRequest.getTour().getTourId());            List<Departure> departureSet = departureService.getListDepartureByTour(tour);            for(Departure departure : tourRequest.getDepartures()){                if(departure.getDepartureId() < 0)                    departure.setDepartureId(0);                if(!departureSet.contains(departure))                    departure.setAvailableSeats(departure.getMaxParticipants());                else {                    departure.setAvailableSeats(departureService.getById(departure.getDepartureId()).getAvailableSeats());                    departure.setActive(true);                }                departure.setTour(tourRequest.getTour());                departure.setActive(true);                departureService.update(departure);            }            for(Departure departure : departureSet){                if(!tourRequest.getDepartures().contains(departure)){                    departure.setActive(false);                    departureService.update(departure);                }            }            for (TourPricing tourPricing : tourRequest.getTourPricing()){                if(tourPricing.getDeparture().getDepartureId() <= 0){                    int index = listId.indexOf(tourPricing.getDeparture().getDepartureId());                    Departure d = departureService.getById(tourRequest.getDepartures().get(index).getDepartureId());                    tourPricing.setDeparture(d);                }                tourPricing.setModifiedDate(LocalDateTime.now());                tourPricingService.create(tourPricing);            }            List<TourDestination> tourDestinations = tourDestinationService.findAllByTour(tour);            List<TourDestination> tourDestinationsRequest = tourRequest.getTourDestinations();            for(int i=0; i < tourDestinationsRequest.size(); i++){                TourDestination td = tourDestinationsRequest.get(i);                td.setTour(tourRequest.getTour());                td.setSequenceOrder(i+1);                tourDestinationService.update(td);            }//            List<Destination> destinations = new ArrayList<>();//            tourDestinationsRequest.forEach(d -> destinations.add(d.getDestination()));            for(TourDestination tourDestination : tourDestinations){                if(!tourDestinationsRequest.contains(tourDestination)){                    tourDestination.setSequenceOrder(0);                    tourDestinationService.update(tourDestination);                }            }            List<Image> lstImg = imageService.findAllByTouur(tour);            if(tourRequest.getImages() == null || tourRequest.getImages().isEmpty() || tourRequest.getImages().get(0).getImageUrl() == null){                for(Image img : lstImg){                    imageService.delete(img.getImageId());                }            } else {                for (Image image : lstImg){                    if(!tourRequest.getImages().contains(image))                        imageService.delete(image.getImageId());                }                for (Image image : tourRequest.getImages()){                    if(!lstImg.contains(image)){                        image.setTour(tour);                        imageService.create(image);                    }                }            }        } catch (Exception e){            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");        }        return ResponseEntity.ok("Update thành công!");    }    @PostMapping("/addTour")    @Transactional(rollbackFor = Exception.class)    public ResponseEntity<TourInfoDTO> addTour(@RequestBody TourRequest tourRequest) throws Exception {        Tour tourCreated = null;        try {            Tour tour = Tour.builder().tourName(tourRequest.getTour().getTourName())                    .tourDescription(tourRequest.getTour().getTourDescription())                    .tourType(tourRequest.getTour().getTourType())                    .startLocation(tourRequest.getTour().getStartLocation())                    .user(tourRequest.getTour().getUser())                    .createdDate(LocalDateTime.now())                    .duration(tourRequest.getTour().getDuration())                    .isActive(false)                    .user(User.builder().userId(tourRequest.getUserId()).build()).build();            Departure departure = Departure.builder().startDate(tourRequest.getDepartures().get(0).getStartDate())                    .endDate(tourRequest.getDepartures().get(0).getEndDate())                    .maxParticipants(tourRequest.getDepartures().get(0).getMaxParticipants())                    .availableSeats(tourRequest.getDepartures().get(0).getMaxParticipants())                    .isActive(true).tour(tour).build();            departure.setTour(tour);            tour.setDepartures(Set.of(departure));            tourCreated = tourService.create(tour);            for (TourPricing tourPricing : tourRequest.getTourPricing()) {                tourPricing.setModifiedDate(LocalDateTime.now());                tourPricing.setDeparture(departure);                tourPricingService.create(tourPricing);            }            for (int i = 0; i < tourRequest.getTourDestinations().size(); i++) {                Destination destination = destinationService.getById(tourRequest.getTourDestinations().get(i).getDestination().getDestinationId());                if (destination != null) {                    TourDestination tourDestination = TourDestination.builder()                            .tour(tourCreated).destination(destination).sequenceOrder(i + 1)                            .duration(tourRequest.getTourDestinations().get(i).getDuration()).build();                    tourDestinationService.create(tourDestination);                }            }            if (tourRequest.getImages() != null && !tourRequest.getImages().isEmpty()) {                for (Image image : tourRequest.getImages()) {                    if (image.getImageUrl() != null && !image.getImageUrl().isEmpty()) {                        image.setTour(tour);                        imageService.create(image);                    }                }            }        } catch (Exception e) {            throw new Exception("Đã xảy ra lỗi trong quá trình xử lý thông tin!");        }        return ResponseEntity.ok(tourService.convertToDTO(tourCreated));    }    @GetMapping("/list-tours")    public ResponseEntity<Page<TourResponseDTO>> getListTour(            @RequestParam(defaultValue = "0") int page,            @RequestParam(defaultValue = "10") int size) {        Pageable pageable = PageRequest.of(page, size);        Page<TourResponseDTO> tourPage = tourService.getListTour(pageable);        return ResponseEntity.ok(tourPage);    }    @GetMapping("/provider/{userId}")    public ResponseEntity<Page<TourResponseDTO>> getListTourByUserId(            @PathVariable Long userId,            @RequestParam(defaultValue = "0") int page,            @RequestParam(defaultValue = "10") int size) {        Pageable pageable = PageRequest.of(page, size);        Page<TourResponseDTO> tourPage = tourService.getListTourByUserId(userId, pageable);        return ResponseEntity.ok(tourPage);    }    @PostMapping("/approve/{id}")    public ResponseEntity<String> approveTour(@PathVariable Long id) {        try {            tourService.approveTour(id);            return ResponseEntity.ok("Phê duyệt thành công");        } catch (ResourceNotFoundException e) {            return ResponseEntity.notFound().build();        } catch (Exception e) {            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());        }    }    @PostMapping("/delete/{id}")    public ResponseEntity<String> deleteTour(@PathVariable Long id) {        try {            tourService.deleteTour(id);            return ResponseEntity.ok("Xóa thành công");        } catch (ResourceNotFoundException e) {            return ResponseEntity.notFound().build();        } catch (Exception e) {            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());        }    }    @GetMapping("/simple")    public ResponseEntity<List<TourSimpleDTO>> getTourSimple() {        List<TourSimpleDTO> tours = tourService.getAllTours();        return ResponseEntity.ok(tours);    }    @GetMapping("/allWithDeparture")    public List<TourWithDeparturesDTO> getAllToursAndDeparture() {        return tourService.getAllToursAndDeparture();    }    @GetMapping("/count-by-user")    public List<TourCountDTO> getTourCountByUser() {        return tourService.getTourStatisticsByUser();    }    @GetMapping("/count-by-user/{userId}")    public ResponseEntity<TourCountDTO> getTourStatisticsByUserId(@PathVariable Long userId) {        TourCountDTO tourStatistics = tourService.getTourStatisticsByUserId(userId);        if (tourStatistics == null) {            return ResponseEntity.notFound().build();        }        return ResponseEntity.ok(tourStatistics);    }    @GetMapping("/revenue-statistics")    public List<RevenueStatisticsDTO> getRevenueStatistics(            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {        return tourService.getRevenueStatistics(startDate, endDate);    }    @GetMapping("/revenue-statistics-tour-and-ticket")    public ResponseEntity<RevenueStatisticsResponse> getRevenueStatisticsTourAndTicket(            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {        RevenueStatisticsResponse statistics = tourService.getRevenueStatisticsTourAndTicket(startDate, endDate);        return ResponseEntity.ok(statistics);    }    @GetMapping("/monthly-revenue")    public ResponseEntity<List<MonthlyRevenueDTO>> getMonthlyRevenueStatistics(@RequestParam int year) {        List<MonthlyRevenueDTO> statistics = tourService.getMonthlyRevenueStatistics(year);        return ResponseEntity.ok(statistics);    }    @GetMapping("/tourType")    public ResponseEntity<Map<String, String>> getTourTypes(){        Map<String, String> enumMap = new LinkedHashMap<>();        for (TourType item : TourType.values()) {            enumMap.put(item.name(), item.getDescription());        }        return ResponseEntity.ok(enumMap);    }    @GetMapping("/page")    public ResponseEntity<Page<Tour>> getTourPage(            @RequestParam(defaultValue = "0", required = false) int page,            @RequestParam(defaultValue = "10", required = false) int size,            @RequestParam(defaultValue = "tourName", required = false) String sortBy,            @RequestParam(defaultValue = "asc", required = false) String sortDirection) {        Page<Tour> tours = tourService.getPageList(page, size, sortBy, sortDirection);        return ResponseEntity.ok(tours);    }}