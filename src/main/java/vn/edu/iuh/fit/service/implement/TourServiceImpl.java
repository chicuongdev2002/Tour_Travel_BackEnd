package vn.edu.iuh.fit.service.implement;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectRequest;
import jakarta.annotation.PostConstruct;
import org.hibernate.Hibernate;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dto.*;
import vn.edu.iuh.fit.dto.respone.RevenueStatisticsResponse;
import vn.edu.iuh.fit.dto.respone.TourResponseDTO;
import vn.edu.iuh.fit.entity.*;
import vn.edu.iuh.fit.enums.AccountRole;
import vn.edu.iuh.fit.enums.ParticipantType;
import vn.edu.iuh.fit.enums.TourType;
import vn.edu.iuh.fit.exception.ResourceNotFoundException;
import vn.edu.iuh.fit.mailservice.EmailService;
import vn.edu.iuh.fit.repositories.*;
import vn.edu.iuh.fit.repositories.DepartureRepository;
import vn.edu.iuh.fit.repositories.TourPricingRepository;
import vn.edu.iuh.fit.repositories.TourRepository;
import vn.edu.iuh.fit.service.DiscountService;
import vn.edu.iuh.fit.service.ImageService;
import vn.edu.iuh.fit.service.TourPricingService;
import vn.edu.iuh.fit.service.TourService;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TourServiceImpl extends AbstractCrudService<Tour, Long> implements TourService {
    private static final Logger logger = LoggerFactory.getLogger(TourServiceImpl.class);
    @Autowired
    private TourRepository tourRepository;
    @Autowired
    private TourPricingRepository tourPricingRepository;
    @Autowired

    private TourGuideAssignmentRepository tourGuideAssignmentRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private DepartureRepository departureRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private  UserRepository userRepository;
    @Autowired
    private FavoriteTourRepository favoriteTourRepository;


    private TourPricingService tourPricingService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private DiscountService discountService;
    @Autowired
    private EmailService emailService;
    private final ModelMapper modelMapper;
    //    @Autowired
//    private PagedResourcesAssembler<Tour> pagedResourcesAssembler;
    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.access.key}")
    private String accessKey;

    @Value("${aws.secret.key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    private AmazonS3 s3Client;


    @Autowired
    public TourServiceImpl(TourRepository tourRepository, ModelMapper modelMapper) {
        this.tourRepository = tourRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<TourSimpleDTO> getAllTours() {
        List<Tour> tours = tourRepository.findAll();
        return tours.stream()
                .map(tour -> new TourSimpleDTO(tour.getTourId(), tour.getTourName(),tour.getTourType()))
                .collect(Collectors.toList());
    }

    @PostConstruct
    public void initializeAmazonS3Client() {
        if (StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(secretKey)) {
            throw new IllegalStateException("AWS credentials not properly configured");
        }
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(region)
                .build();
    }

    @Override
    protected JpaRepository<Tour, Long> getRepository() {
        return tourRepository;
    }

    @Override
    public List<Tour> findToursByDestination(Destination destination) {
        return null;
    }

    @Override
    public Page<TourSummaryDTO> getTours(String keyword, int page, int size, BigDecimal minPrice, BigDecimal maxPrice,
                                         String tourTypeStr, String startLocation, String participantTypeStr) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results;

        TourType tourType = null;
        ParticipantType participantType = null;

        if (tourTypeStr != null && !tourTypeStr.isEmpty()) {
            try {
                tourType = TourType.valueOf(tourTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid tour type: " + tourTypeStr);
            }
        }

        if (participantTypeStr != null && !participantTypeStr.isEmpty()) {
            try {
                participantType = ParticipantType.valueOf(participantTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid participant type: " + participantTypeStr);
            }
        }

        if (keyword != null && !keyword.isEmpty()) {
            // Tìm kiếm với từ khóa
            results = tourRepository.findTourByKeyword(keyword, pageable);
        } else if (tourType != null || startLocation != null || participantType != null) {
            // Tìm kiếm theo bộ lộc
            results = tourRepository.searchTours(minPrice, maxPrice, tourType, startLocation, participantType, pageable);
        } else {
            // Lấy danh sách mặc định
            results = tourRepository.findToursWithPriceRange(minPrice, maxPrice, pageable);
        }

        return results.map(result -> {
            TourSummaryDTO dto = new TourSummaryDTO();
            Tour tour = (Tour) result[0];
            TourPricing tourPricing = (TourPricing) result[1];
            LocalDateTime startDate = (LocalDateTime) result[2];
            Integer availableSeats = (Integer) result[3];
            Integer maxParticipants = (Integer) result[4];
            String imageUrlsString = (String) result[5];
            // Chuyển đổi chuỗi ảnh thành list
            List<String> imageUrls = imageUrlsString != null ?
                    Arrays.asList(imageUrlsString.split(",")) :
                    Collections.emptyList();

            // Chỉ lấy ảnh đầu tiên làm ảnh đại diện
            String mainImageUrl = imageUrls.isEmpty() ? null : imageUrls.get(0);
            dto.setTourId(tour.getTourId());
            dto.setTourName(tour.getTourName());
            dto.setTourDescription(tour.getTourDescription());
            dto.setDuration(tour.getDuration());
            dto.setStartLocation(tour.getStartLocation());
            dto.setPrice(tourPricing.getPrice());
            dto.setStartDate(startDate);
            dto.setMaxParticipants(maxParticipants);
            dto.setAvailableSeats(availableSeats);
            dto.setImageUrl(mainImageUrl);
            return dto;
        });
    }

    public Page<TourListDTO> getToursTest(int page, int size, String keyword, String startLocation,
                                          String tourTypeStr, String participantTypeStr,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          BigDecimal minPrice, BigDecimal maxPrice,
                                          Long userId) {
        List<Tour> allTours = tourRepository.findActiveToursForCardListing(TourType.DELETE);
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);

        final TourType finalTourType = tourTypeStr != null ? TourType.valueOf(tourTypeStr.toUpperCase()) : null;
        final ParticipantType finalParticipantType = participantTypeStr != null ? ParticipantType.valueOf(participantTypeStr.toUpperCase()) : null;
        final String finalKeyword = keyword;
        Map<Long, List<TourPricing>> pricingByDepartureId = loadLatestPricingMap(allTours);

        List<Tour> filteredTours = allTours.stream()
                .filter(tour -> tour.isActive() &&
                        tour.getTourType() != TourType.DELETE &&
                        tour.getDepartures() != null && !tour.getDepartures().isEmpty() &&
                        tour.getDepartures().stream()
                                .anyMatch(departure -> departure.isActive() &&
                                        departure.getAvailableSeats() > 0 &&
                                        departure.getStartDate() != null &&  // Kiểm tra không null
                                        departure.getStartDate().isAfter(threeDaysFromNow))
                )
                .filter(tour -> (finalKeyword == null || tour.getTourName().toLowerCase().contains(finalKeyword.toLowerCase())) &&
                        (startLocation == null || tour.getStartLocation().equalsIgnoreCase(startLocation)) &&
                        (finalTourType == null || tour.getTourType() == finalTourType) &&
                        (finalParticipantType == null || matchesParticipantType(tour, finalParticipantType, pricingByDepartureId)) &&
                        (startDate == null || tour.getDepartures().stream()
                                .anyMatch(departure -> departure.getStartDate() != null && departure.getStartDate().isAfter(startDate))) &&  // Kiểm tra không null
                        (endDate == null || tour.getDepartures().stream()
                                .anyMatch(departure -> departure.getStartDate() != null && departure.getStartDate().isBefore(endDate))) &&  // Kiểm tra không null
                        (minPrice == null || getLowestPriceForTour(tour, pricingByDepartureId).compareTo(minPrice) >= 0) &&
                        (maxPrice == null || getLowestPriceForTour(tour, pricingByDepartureId).compareTo(maxPrice) <= 0)
                )
                .collect(Collectors.toList());

        final Set<Long> favoriteTourIds = loadFavoriteTourIds(userId);
        filteredTours.sort((t1, t2) -> {
            boolean t1IsFavorite = favoriteTourIds.contains(t1.getTourId());
            boolean t2IsFavorite = favoriteTourIds.contains(t2.getTourId());
            if (t1IsFavorite && !t2IsFavorite) {
                return -1;  // t1 trước t2
            } else if (!t1IsFavorite && t2IsFavorite) {
                return 1;   // t2 trước t1
            } else {
                return t2.getCreatedDate().compareTo(t1.getCreatedDate());
            }
        });

        long totalFilteredElements = filteredTours.size();
        int start = Math.min(page * size, (int) totalFilteredElements);
        int end = Math.min(start + size, (int) totalFilteredElements);

        List<TourListDTO> paginatedTours = filteredTours.subList(start, end).stream()
                .map(tour -> toTourListDTO(tour, pricingByDepartureId, favoriteTourIds.contains(tour.getTourId()), threeDaysFromNow))
                .collect(Collectors.toList());

        return new PageImpl<>(paginatedTours, PageRequest.of(page, size), totalFilteredElements);
    }

    private boolean matchesParticipantType(Tour tour, ParticipantType participantType) {
        if (participantType == null) {
            return true;
        }
        return tour.getDepartures().stream()
                .flatMap(departure -> departure.getTourPricing().stream())
                .anyMatch(pricing -> pricing.getParticipantType() == participantType);
    }

    private boolean matchesParticipantType(Tour tour, ParticipantType participantType, Map<Long, List<TourPricing>> pricingByDepartureId) {
        if (participantType == null) {
            return true;
        }
        return tour.getDepartures().stream()
                .flatMap(departure -> pricingByDepartureId.getOrDefault(departure.getDepartureId(), List.of()).stream())
                .anyMatch(pricing -> pricing.getParticipantType() == participantType);
    }

    private Set<Long> loadFavoriteTourIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        return userRepository.findById(userId)
                .map(favoriteTourRepository::findByUser)
                .orElse(List.of())
                .stream()
                .map(favoriteTour -> favoriteTour.getTour().getTourId())
                .collect(Collectors.toSet());
    }

    private Map<Long, List<TourPricing>> loadLatestPricingMap(Collection<Tour> tours) {
        List<Departure> departures = tours.stream()
                .filter(Objects::nonNull)
                .flatMap(tour -> tour.getDepartures().stream())
                .toList();
        if (departures.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, LocalDateTime> departureStartDates = departures.stream()
                .collect(Collectors.toMap(Departure::getDepartureId, Departure::getStartDate, (left, right) -> left));

        List<Long> departureIds = departures.stream()
                .map(Departure::getDepartureId)
                .toList();

        List<TourPricing> sortedPricings = tourPricingRepository.findTourPricingByDepartureIds(departureIds).stream()
                .sorted(Comparator.comparing(TourPricing::getModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                .toList();

        Map<Long, EnumMap<ParticipantType, TourPricing>> latestByDeparture = new HashMap<>();
        for (TourPricing pricing : sortedPricings) {
            Long departureId = pricing.getDeparture().getDepartureId();
            LocalDateTime startDate = departureStartDates.get(departureId);
            if (startDate != null && pricing.getModifiedDate() != null && pricing.getModifiedDate().isAfter(startDate)) {
                continue;
            }
            latestByDeparture
                    .computeIfAbsent(departureId, ignored -> new EnumMap<>(ParticipantType.class))
                    .putIfAbsent(pricing.getParticipantType(), pricing);
        }

        Map<Long, List<TourPricing>> pricingByDepartureId = new HashMap<>();
        for (Departure departure : departures) {
            EnumMap<ParticipantType, TourPricing> departurePricings = latestByDeparture.getOrDefault(
                    departure.getDepartureId(),
                    new EnumMap<>(ParticipantType.class)
            );
            List<TourPricing> latestPricings = Arrays.stream(ParticipantType.values())
                    .map(participantType -> departurePricings.getOrDefault(
                            participantType,
                            TourPricing.builder()
                                    .departure(departure)
                                    .participantType(participantType)
                                    .price(BigDecimal.ZERO)
                                    .modifiedDate(departure.getStartDate() != null ? departure.getStartDate().minusSeconds(1) : null)
                                    .build()
                    ))
                    .toList();
            pricingByDepartureId.put(departure.getDepartureId(), latestPricings);
        }
        return pricingByDepartureId;
    }

    private BigDecimal getLowestPriceForTour(Tour tour, Map<Long, List<TourPricing>> pricingByDepartureId) {
        return tour.getDepartures().stream()
                .flatMap(departure -> pricingByDepartureId.getOrDefault(departure.getDepartureId(), List.of()).stream())
                .map(TourPricing::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private Discount findActiveDiscount(Tour tour, LocalDateTime now) {
        return tour.getDiscounts().stream()
                .filter(discount -> discount.getStartDate() != null && discount.getEndDate() != null)
                .filter(discount -> discount.getStartDate().isBefore(now) &&
                        discount.getEndDate().isAfter(now) &&
                        (discount.getCountUse() == null || discount.getCountUse() > 0))
                .findFirst()
                .orElse(null);
    }

    private TourListDTO toTourListDTO(Tour tour, Map<Long, List<TourPricing>> pricingByDepartureId, boolean isFavorite, LocalDateTime minStartDate) {
        BigDecimal originalPrice = getLowestPriceForTour(tour, pricingByDepartureId);
        BigDecimal displayPrice = originalPrice;
        Discount activeDiscount = findActiveDiscount(tour, LocalDateTime.now());
        if (activeDiscount != null) {
            BigDecimal discountPercentage = BigDecimal.valueOf(activeDiscount.getDiscountAmount());
            BigDecimal discountAmount = originalPrice.multiply(discountPercentage.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            displayPrice = originalPrice.subtract(discountAmount);
            if (displayPrice.compareTo(BigDecimal.ZERO) < 0) {
                displayPrice = BigDecimal.ZERO;
            }
        }

        return TourListDTO.builder()
                .tourId(tour.getTourId())
                .tourName(tour.getTourName())
                .tourType(tour.getTourType())
                .tourDescription(tour.getTourDescription())
                .price(displayPrice)
                .originalPrice(originalPrice)
                .duration(tour.getDuration())
                .maxParticipants(tour.getDepartures().stream()
                        .findFirst()
                        .map(Departure::getMaxParticipants)
                        .orElse(null))
                .startLocation(tour.getStartLocation())
                .startDate(tour.getDepartures().stream()
                        .filter(departure -> departure.getStartDate() != null && departure.getStartDate().isAfter(minStartDate))
                        .map(Departure::getStartDate)
                        .min(LocalDateTime::compareTo)
                        .orElse(null))
                .availableSeats(tour.getDepartures().stream()
                        .map(Departure::getAvailableSeats)
                        .filter(Objects::nonNull)
                        .reduce(0, Integer::sum))
                .imageUrl(tour.getImages().isEmpty() ? null : tour.getImages().iterator().next().getImageUrl())
                .isFavorite(isFavorite)
                .build();
    }

    private List<TourPricingDTO> toTourPricingDTOs(Departure departure, Map<Long, List<TourPricing>> pricingByDepartureId) {
        return pricingByDepartureId.getOrDefault(departure.getDepartureId(), List.of()).stream()
                .map(pricing -> {
                    TourPricingDTO dto = new TourPricingDTO();
                    dto.setPrice(pricing.getPrice());
                    dto.setParticipantType(pricing.getParticipantType());
                    dto.setModifiedDate(pricing.getModifiedDate());
                    return dto;
                })
                .toList();
    }

    private Map<Long, List<TourGuideAssignment>> loadAssignmentsByDepartureId(Collection<Departure> departures) {
        List<Long> departureIds = departures.stream()
                .map(Departure::getDepartureId)
                .toList();
        if (departureIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return tourGuideAssignmentRepository.findByDeparture_DepartureIdIn(departureIds).stream()
                .collect(Collectors.groupingBy(assignment -> assignment.getDeparture().getDepartureId()));
    }

    private Page<TourResponseDTO> mapToursPageToResponse(Page<Tour> toursPage) {
        if (toursPage.isEmpty()) {
            return Page.empty(toursPage.getPageable());
        }

        List<Long> orderedTourIds = toursPage.getContent().stream()
                .map(Tour::getTourId)
                .toList();
        List<Tour> hydratedTours = tourRepository.findAllWithListingRelationsByTourIdIn(orderedTourIds);
        Map<Long, Tour> hydratedTourById = hydratedTours.stream()
                .collect(Collectors.toMap(Tour::getTourId, tour -> tour, (left, right) -> left));

        Map<Long, List<TourPricing>> pricingByDepartureId = loadLatestPricingMap(hydratedTours);
        Map<Long, List<Image>> destinationImagesById = loadDestinationImagesById(hydratedTours);

        List<TourResponseDTO> responses = orderedTourIds.stream()
                .map(hydratedTourById::get)
                .filter(Objects::nonNull)
                .map(tour -> toTourResponseDTO(tour, pricingByDepartureId, destinationImagesById))
                .toList();

        return new PageImpl<>(responses, toursPage.getPageable(), toursPage.getTotalElements());
    }

    private Map<Long, List<Image>> loadDestinationImagesById(Collection<Tour> tours) {
        List<Long> destinationIds = tours.stream()
                .flatMap(tour -> tour.getTourDestinations().stream())
                .map(TourDestination::getDestination)
                .filter(Objects::nonNull)
                .map(Destination::getDestinationId)
                .distinct()
                .toList();
        if (destinationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return imageRepository.findByDestination_DestinationIdIn(destinationIds).stream()
                .filter(image -> image.getDestination() != null)
                .collect(Collectors.groupingBy(image -> image.getDestination().getDestinationId()));
    }

    private TourResponseDTO toTourResponseDTO(Tour tour, Map<Long, List<TourPricing>> pricingByDepartureId, Map<Long, List<Image>> destinationImagesById) {
        List<TourResponseDTO.DepartureResponseDTO> departures = tour.getDepartures().stream()
                .map(departure -> new TourResponseDTO.DepartureResponseDTO(
                        departure.getDepartureId(),
                        departure.getStartDate(),
                        departure.getEndDate(),
                        departure.getAvailableSeats(),
                        departure.getMaxParticipants(),
                        departure.isActive(),
                        pricingByDepartureId.getOrDefault(departure.getDepartureId(), List.of()).stream()
                                .map(pricing -> new TourResponseDTO.DepartureResponseDTO.TourPricingResponseDTO(
                                        pricing.getPrice(),
                                        pricing.getParticipantType().name(),
                                        pricing.getModifiedDate()
                                ))
                                .toList()
                ))
                .toList();

        List<TourResponseDTO.DestinationDTO> destinations = tour.getTourDestinations().stream()
                .sorted(Comparator.comparingInt(TourDestination::getSequenceOrder))
                .filter(td -> td.getSequenceOrder() > 0)
                .map(tourDestination -> {
                    Destination destination = tourDestination.getDestination();
                    List<Image> images = destinationImagesById.getOrDefault(destination.getDestinationId(), List.of());
                    return new TourResponseDTO.DestinationDTO(
                            destination.getDestinationId(),
                            destination.getName(),
                            destination.getDescription(),
                            destination.getProvince(),
                            tourDestination.getSequenceOrder(),
                            tourDestination.getDuration(),
                            images.stream()
                                    .map(image -> new TourResponseDTO.DestinationDTO.ImageDTO(
                                            image.getImageId(),
                                            image.getImageUrl()
                                    ))
                                    .toList()
                    );
                })
                .toList();

        List<ImageDTO> images = tour.getImages().stream()
                .map(image -> {
                    ImageDTO imageDTO = new ImageDTO();
                    imageDTO.setImageId(image.getImageId());
                    imageDTO.setImageUrl(image.getImageUrl());
                    return imageDTO;
                })
                .toList();

        return new TourResponseDTO(
                tour.getTourId(),
                tour.getTourName(),
                tour.getTourDescription(),
                tour.getDuration(),
                tour.getStartLocation(),
                tour.getTourType(),
                tour.isActive(),
                tour.getUser().getFullName(),
                destinations,
                departures,
                images
        );
    }
    @PostConstruct
    public void initializeModelMapper() {
        modelMapper.addConverter(new Converter<UUID, Long>() {
            public Long convert(MappingContext<UUID, Long> context) {
                return context.getSource() != null ? Long.valueOf(context.getSource().toString()) : null;
            }
        });
    }


    //    @Cacheable("tour")
    public TourDetailDTO getTourById(long id) {
        Tour tour = tourRepository.findTourWithAllDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        Map<Long, List<TourPricing>> pricingByDepartureId = loadLatestPricingMap(List.of(tour));
        Map<Long, List<TourGuideAssignment>> assignmentsByDepartureId = loadAssignmentsByDepartureId(tour.getDepartures());

        List<DestinationDTO> destinationDTOs = tour.getTourDestinations().stream()
                .sorted(Comparator.comparingInt(TourDestination::getSequenceOrder))
                .filter(td -> td.getSequenceOrder() > 0)
                .map(tourDestination -> {
                    Destination destination = tourDestination.getDestination();
                    return new DestinationDTO(
                            destination.getDestinationId(),
                            destination.getName(),
                            destination.getDescription(),
                            destination.getProvince(),
                            tourDestination.getSequenceOrder(),
                            tourDestination.getDuration(),
                            destination.getImages().stream()
                                    .map(image -> new ImageDTO(
                                            image.getImageId(),
                                            image.getImageUrl()
                                    ))
                                    .collect(Collectors.toList())
                    );
                })
                .collect(Collectors.toList());

        // Ánh xạ departures
        List<DepartureDTO> departureDTOs = new ArrayList<>();
        for (Departure departure : tour.getDepartures()) {
            if (departure.isActive()) {
                DepartureDTO departureDTO = new DepartureDTO();
                departureDTO.setDepartureId(departure.getDepartureId());
                departureDTO.setStartDate(departure.getStartDate());
                departureDTO.setEndDate(departure.getEndDate());
                departureDTO.setAvailableSeats(departure.getAvailableSeats());
                departureDTO.setMaxParticipants(departure.getMaxParticipants());

                departureDTO.setTourPricing(toTourPricingDTOs(departure, pricingByDepartureId));
                List<TourGuideDTO> tourGuideDTOs = assignmentsByDepartureId
                        .getOrDefault(departure.getDepartureId(), List.of())
                        .stream()
                        .map(assignment -> {
                            TourGuide guide = assignment.getTourGuide();
                            TourGuideDTO guideDTO = new TourGuideDTO();
                            guideDTO.setGuideId(guide.getUserId());
                            guideDTO.setFullName(guide.getFullName());
                            guideDTO.setExperienceYear(guide.getExperienceYear());
                            return guideDTO;
                        })
                        .collect(Collectors.toList());
                departureDTO.setTourGuides(tourGuideDTOs);
                departureDTOs.add(departureDTO);
            }
        }

        List<ImageDTO> imageDTOs = tour.getImages().stream()
                .map(image -> {
                    ImageDTO imageDTO = new ImageDTO();
                    imageDTO.setImageId(image.getImageId());
                    imageDTO.setImageUrl(image.getImageUrl());
                    return imageDTO;
                })
                .collect(Collectors.toList());
        List<ReviewDTO> reviewDTOs = tour.getReviews().stream()
                .sorted(Comparator.comparing(Review::getReviewDate).reversed())
                .map(review -> {
                    ReviewDTO reviewDTO = new ReviewDTO();
                    reviewDTO.setReviewId(review.getReviewId());
                    reviewDTO.setRating(review.getRating());
                    reviewDTO.setComment(review.getComment());
                    reviewDTO.setReviewDate(review.getReviewDate());
                    reviewDTO.setUserId(review.getUser() != null ? review.getUser().getUserId() : null);
                    reviewDTO.setUserName(review.getUser() != null ? review.getUser().getFullName() : "");
                    reviewDTO.setActive(review.isActive());
                    return reviewDTO;
                })
                .collect(Collectors.toList());
        TourDetailDTO tourDetailDTO = modelMapper.map(tour, TourDetailDTO.class);
        tourDetailDTO.setDepartures(departureDTOs);
        tourDetailDTO.setDestinations(destinationDTOs);
        tourDetailDTO.setImages(imageDTOs);
        tourDetailDTO.setReviews(reviewDTOs);
        return tourDetailDTO;
    }


    public Tour convertDtoToEntity(TourDetailDTO tourDetailDTO) {
        return modelMapper.map(tourDetailDTO, Tour.class);
    }


    @Override
    public String uploadImageToAWS(File file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getName();
        try {
            // Tải lên hình ảnh lên AWS S3
            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));
            String fileUrl = s3Client.getUrl(bucketName, fileName).toString();
            return fileUrl;
        } catch (Exception e) {
            throw new IOException("Error uploading image: " + e.getMessage(), e);
        } finally {
            // Dọn dẹp file tạm thời nếu cần
            if (file.exists()) {
                file.delete();
            }
        }
    }

    // Phương thức chuyển đổi MultipartFile thành File
    @Override
    public File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        file.transferTo(convFile);
        System.out.println("File được tạo: " + convFile.getAbsolutePath());
        return convFile;
    }

    @Override
    public List<TourWithDeparturesDTO> getAllToursAndDeparture() {
        List<Tour> tours = tourRepository.findAll();
        LocalDate currentDate = LocalDate.now();
        LocalDateTime currentDateTime = currentDate.atStartOfDay();
        return tours.stream().map(tour -> {
            List<DepartureByTourDTO> departureDTOs = tour.getDepartures().stream()
                    .filter(departure -> departure.getStartDate().isAfter(currentDateTime))
                    .map(departure -> {
                        DepartureByTourDTO departureDTO = new DepartureByTourDTO();
                        departureDTO.setDepartureId(departure.getDepartureId());
                        departureDTO.setStartDate(departure.getStartDate());
                        departureDTO.setEndDate(departure.getEndDate());
                        departureDTO.setAvailableSeats(departure.getAvailableSeats());
                        departureDTO.setMaxParticipants(departure.getMaxParticipants());
                        return departureDTO;
                    }).collect(Collectors.toList());

            return new TourWithDeparturesDTO(
                    tour.getTourId(),
                    tour.getTourName(),
                    tour.getTourType().name(),
                    departureDTOs
            );
        }).collect(Collectors.toList());
    }
    @Override
    public Page<TourResponseDTO> getListTour(Pageable pageable) {
        Pageable sortedByActive = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "isActive"));
        Page<Tour> toursPage = tourRepository.findAll(sortedByActive);
        return mapToursPageToResponse(toursPage);
    }
    @Override
    public Page<TourResponseDTO> getListTourByUserId(Long userId, Pageable pageable) {
        Pageable sortedByStartDate = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "departures.startDate"));

        Page<Tour> toursPage = tourRepository.findByUser_UserId(userId, sortedByStartDate);
        return mapToursPageToResponse(toursPage);
    }
    @Override
    public TourInfoDTO convertToDTO(Tour tour) {
        return modelMapper.map(tour, TourInfoDTO.class);
    }
    @Override
    public void approveTour(Long id) {
        logger.info("Approving tour with ID: {}", id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        tour.setActive(true);
        tourRepository.save(tour);
        logger.info("Tour with ID: {} has been approved and set to active.", id);
        emailService.sendTourApprovalNotification(tour.getUser().getEmail(), tour);
        List<AccountRole> roles = List.of(AccountRole.CUSTOMER,AccountRole.CUSTOMERVIP);
        List<User> customers = userRepository.findByAccount_RoleInAndAccount_IsActiveTrue(roles);
        for (User customer : customers) {
            emailService.sendNewTourNotification(customer.getEmail(), tour);
        }
//        emailService.sendTourApprovalNotification("chicuong11042002@gmail.com",tour);
//        emailService.sendNewTourNotification("chicuong11042002@gmail.com",tour);
    }

    @Override
    public void deleteTour(Long id) {
        logger.info("Delete tour with ID: {}", id);
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));
        tour.setTourType(TourType.DELETE);
        logger.info("Tour",tour);
        tourRepository.save(tour);
        logger.info("Tour with ID: {} has been Delete", id);
    }
    @Override
    public List<TourCountDTO> getTourStatisticsByUser() {
        List<TourCountDTO> tourCounts = tourRepository.findAll().stream()
                .collect(Collectors.groupingBy(tour -> tour.getUser().getFullName()))
                .entrySet().stream()
                .map(entry -> {
                    String userName = entry.getKey();
                    List<Tour> tours = entry.getValue();
                    Long userId = tours.get(0).getUser().getUserId();
                    Long tourCount = (long) tours.size();
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    Map<String, BigDecimal> ticketRevenue = new HashMap<>();

                    // Tạo danh sách TourSimpleDTO
                    List<TourSimpleDTO> tourSimpleDTOs = new ArrayList<>();

                    for (Tour tour : tours) {
                        // Thêm thông tin tour vào danh sách TourSimpleDTO
                        tourSimpleDTOs.add(new TourSimpleDTO(tour.getTourId(), tour.getTourName(), tour.getTourType())); // Sử dụng các thuộc tính từ Tour

                        List<Departure> departures = departureRepository.findAllByTour(tour);
                        for (Departure departure : departures) {
                            List<Booking> bookings = bookingRepository.findByDeparture(departure);
                            for (Booking booking : bookings) {
                                String[] participants = booking.getParticipants().split(",");
                                List<TourPricing> tourPricings = tourPricingRepository.findByDeparture(departure);
                                if (participants.length == 3) {
                                    for (int i = 0; i < participants.length; i++) {
                                        int quantity = Integer.parseInt(participants[i]);
                                        if (i < tourPricings.size()) {
                                            BigDecimal price = tourPricings.get(i).getPrice();
                                            BigDecimal revenue = price.multiply(BigDecimal.valueOf(quantity));
                                            totalPrice = totalPrice.add(revenue);
                                            ticketRevenue.merge(tourPricings.get(i).getParticipantType().name(),
                                                    revenue,
                                                    BigDecimal::add);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return new TourCountDTO(userId, userName, tourCount, tourSimpleDTOs, totalPrice, ticketRevenue);
                })
                .collect(Collectors.toList());

        return tourCounts;
    }

                    @Override
                    public String getPriceTourCard(Tour tour) {
                        List<Departure> lst = departureRepository.findAllByTourAndIsActive(tour, true);
                        Set<String> set = new HashSet<>();
                        for(Departure departure : lst)
                            set.add(tourPricingService.getPriceByDeparture(departure.getDepartureId()));
                        if(set.isEmpty()) return "0,0,0";
                        if(set.size() == 1) return set.toArray()[0].toString().trim().replace(" ", ",");
                        return Collections.min(set).trim().replace(" ", ",") + "-" + Collections.max(set).trim().replace(" ", ",");
                    }
    @Override
    public List<TourCardDTO> convertTourToTourCardDTO(List<Tour> tours) {
        List<TourCardDTO> tourCardDTOs = new ArrayList<>();
        for (Tour tour : tours) {
            TourCardDTO tourCardDTO = TourCardDTO.builder()
                    .tourId(tour.getTourId())
                    .tourName(tour.getTourName())
                    .price(getPriceTourCard(tour))
                    .image(imageService.findAllByTouur(tour).isEmpty() ? null : imageService.findAllByTouur(tour).get(0).getImageUrl())
                    .discount(discountService.getDiscountByTour(tour) == null? null : discountService.getDiscountByTour(tour).getDiscountAmount().intValue())
                    .build();
            tourCardDTOs.add(tourCardDTO);
        }
        return tourCardDTOs;
    }

//    @Override
//    public List<Tour> searchTours(String keyword) {
//        return tourRepository.findByTourNameContainingIgnoreCaseOrStartLocationContainingIgnoreCase(keyword, keyword);
//    }



    @Override
    public TourCountDTO getTourStatisticsByUserId(Long userId) {
        // Fetch the user by userId
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        List<Tour> tours = tourRepository.findByUser(user);

        if (tours.isEmpty()) {
            return null;
        }

        Long tourCount = (long) tours.size();
        BigDecimal totalPrice = BigDecimal.ZERO;
        Map<String, BigDecimal> ticketRevenue = new HashMap<>();
        List<TourSimpleDTO> tourSimpleDTOs = tours.stream()
                .map(tour -> new TourSimpleDTO(tour.getTourId(), tour.getTourName(), tour.getTourType()))
                .collect(Collectors.toList());

        for (Tour tour : tours) {
            List<Departure> departures = departureRepository.findAllByTour(tour);
            for (Departure departure : departures) {
                List<Booking> bookings = bookingRepository.findByDeparture(departure);
                for (Booking booking : bookings) {
                    String[] participants = booking.getParticipants().split(",");
                    List<TourPricing> tourPricings = tourPricingRepository.findByDeparture(departure);

                    // Ensure we have the correct number of participants
                    if (participants.length == 3) { // Assuming 3 types: Children, Adults, Elderly
                        for (int i = 0; i < participants.length; i++) {
                            int quantity = Integer.parseInt(participants[i]);
                            if (i < tourPricings.size()) {
                                BigDecimal price = tourPricings.get(i).getPrice();
                                BigDecimal revenue = price.multiply(BigDecimal.valueOf(quantity));
                                totalPrice = totalPrice.add(revenue);
                                ticketRevenue.merge(tourPricings.get(i).getParticipantType().name(),
                                        revenue,
                                        BigDecimal::add);
                            }
                        }
                    } else {
                    }
                }
            }
        }
        return new TourCountDTO(user.getUserId(), user.getFullName(), tourCount, tourSimpleDTOs, totalPrice, ticketRevenue);
    }
    public List<RevenueStatisticsDTO> getRevenueStatistics(LocalDate startDate, LocalDate endDate) {
        List<RevenueStatisticsDTO> revenueStatistics = new ArrayList<>();

        // Lấy tất cả các tour
        List<Tour> allTours = tourRepository.findAll();

        // Lặp qua tất cả các tour để tính doanh thu
        for (Tour tour : allTours) {
            List<Departure> departures = departureRepository.findAllByTour(tour);
            for (Departure departure : departures) {
                List<Booking> bookings = bookingRepository.findByDeparture(departure);
                for (Booking booking : bookings) {
                    String[] participants = booking.getParticipants().split(",");
                    List<TourPricing> tourPricings = tourPricingRepository.findByDeparture(departure);
                    LocalDate bookingDate = booking.getBookingDate().toLocalDate();

                    // Kiểm tra xem ngày đặt có nằm trong khoảng thời gian không
                    if ((bookingDate.isEqual(startDate) || bookingDate.isAfter(startDate)) &&
                            (bookingDate.isEqual(endDate) || bookingDate.isBefore(endDate))) {

                        // Tính toán doanh thu cho các participants
                        for (int i = 0; i < participants.length; i++) {
                            int quantity = Integer.parseInt(participants[i]);
                            if (i < tourPricings.size()) {
                                BigDecimal price = tourPricings.get(i).getPrice();
                                BigDecimal revenue = price.multiply(BigDecimal.valueOf(quantity));

                                // Tìm doanh thu theo ngày
                                revenueStatistics.stream()
                                        .filter(stat -> stat.getDate().isEqual(bookingDate))
                                        .findFirst()
                                        .ifPresentOrElse(
                                                stat -> stat.setTotalRevenue(stat.getTotalRevenue().add(revenue)),
                                                () -> revenueStatistics.add(new RevenueStatisticsDTO(bookingDate, revenue))
                                        );
                            }
                        }
                    }
                }
            }
        }

        return revenueStatistics;
    }
    public RevenueStatisticsResponse getRevenueStatisticsTourAndTicket(LocalDate startDate, LocalDate endDate) {
        RevenueStatisticsResponse response = new RevenueStatisticsResponse();
        List<TourRevenueDTO> tourRevenueList = new ArrayList<>();
        Map<String, BigDecimal> ticketRevenueMap = new HashMap<>();

        // Lấy tất cả các tour
        List<Tour> allTours = tourRepository.findAll();

        // Lặp qua tất cả các tour để tính doanh thu
        for (Tour tour : allTours) {
            List<Departure> departures = departureRepository.findAllByTour(tour);
            for (Departure departure : departures) {
                List<Booking> bookings = bookingRepository.findByDeparture(departure);
                for (Booking booking : bookings) {
                    String[] participants = booking.getParticipants().split(",");
                    List<TourPricing> tourPricings = tourPricingRepository.findByDeparture(departure);
                    LocalDate bookingDate = booking.getBookingDate().toLocalDate();

                    // Kiểm tra xem ngày đặt có nằm trong khoảng thời gian không
                    if ((bookingDate.isEqual(startDate) || bookingDate.isAfter(startDate)) &&
                            (bookingDate.isEqual(endDate) || bookingDate.isBefore(endDate))) {

                        // Tính toán doanh thu cho các participants
                        for (int i = 0; i < participants.length; i++) {
                            int quantity = Integer.parseInt(participants[i]);
                            if (i < tourPricings.size()) {
                                BigDecimal price = tourPricings.get(i).getPrice();
                                BigDecimal revenue = price.multiply(BigDecimal.valueOf(quantity));

                                // Thêm doanh thu vào danh sách tour
                                tourRevenueList.stream()
                                        .filter(tourRevenue -> tourRevenue.getTourName().equals(tour.getTourName()))
                                        .findFirst()
                                        .ifPresentOrElse(
                                                tourRevenue -> tourRevenue.setRevenue(tourRevenue.getRevenue().add(revenue)),
                                                () -> tourRevenueList.add(new TourRevenueDTO(tour.getTourName(), revenue))
                                        );

                                // Thêm doanh thu vào bản đồ loại vé
                                String ticketType = tourPricings.get(i).getParticipantType().name();
                                ticketRevenueMap.merge(ticketType, revenue, BigDecimal::add);
                            }
                        }
                    }
                }
            }
        }

        response.setTours(tourRevenueList);
        response.setTicketRevenue(ticketRevenueMap);

        return response;
    }
    public List<MonthlyRevenueDTO> getMonthlyRevenueStatistics(int year) {
        List<MonthlyRevenueDTO> monthlyRevenues = new ArrayList<>();
        Map<Integer, BigDecimal> revenueMap = new HashMap<>();

        // Lấy tất cả các booking trong năm đã cho
        List<Booking> bookings = bookingRepository.findAllByYear(year);

        // Tính doanh thu cho từng tháng
        for (Booking booking : bookings) {
            LocalDate bookingDate = booking.getBookingDate().toLocalDate();
            int month = bookingDate.getMonthValue(); // Lấy giá trị tháng dưới dạng số
            BigDecimal revenue = calculateRevenueForBooking(booking); // Tính doanh thu cho booking

            // Cộng doanh thu vào tháng tương ứng
            revenueMap.merge(month, revenue, BigDecimal::add);
        }

        // Chuyển đổi map thành list và tính tỷ lệ tăng trưởng
        BigDecimal previousMonthRevenue = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> entry : revenueMap.entrySet()) {
            MonthlyRevenueDTO dto = new MonthlyRevenueDTO();
            dto.setMonth(entry.getKey()); // Gán tháng dưới dạng số
            dto.setRevenue(entry.getValue());

            // Tính tỷ lệ tăng trưởng
            if (previousMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growth = entry.getValue().subtract(previousMonthRevenue);
                BigDecimal growthRate = growth.divide(previousMonthRevenue, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                dto.setGrowthRate(growthRate.doubleValue()); // Đảm bảo là giá trị double
            } else {
                dto.setGrowthRate(0); // Tháng đầu tiên không có tỷ lệ tăng trưởng
            }

            monthlyRevenues.add(dto);
            previousMonthRevenue = entry.getValue(); // Cập nhật doanh thu của tháng hiện tại
        }

        return monthlyRevenues;
    }
    private BigDecimal calculateRevenueForBooking(Booking booking) {
        BigDecimal totalRevenue = BigDecimal.ZERO;
        String[] participants = booking.getParticipants().split(",");
        List<TourPricing> tourPricings = tourPricingRepository.findByDeparture(departureRepository.findById(booking.getDeparture().getDepartureId()).orElseThrow());
        for (int i = 0; i < participants.length; i++) {
            int quantity = Integer.parseInt(participants[i]);
            if (i < tourPricings.size()) {
                BigDecimal price = tourPricings.get(i).getPrice();
                totalRevenue = totalRevenue.add(price.multiply(BigDecimal.valueOf(quantity)));
            }
        }
        return totalRevenue;
    }
}
