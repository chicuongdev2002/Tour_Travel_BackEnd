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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.dto.*;
import vn.edu.iuh.fit.entity.*;
import vn.edu.iuh.fit.enums.ParticipantType;
import vn.edu.iuh.fit.enums.TourType;
import vn.edu.iuh.fit.exception.ResourceNotFoundException;
import vn.edu.iuh.fit.repositories.ImageRepository;
import vn.edu.iuh.fit.repositories.TourGuideAssignmentRepository;
import vn.edu.iuh.fit.repositories.TourPricingRepository;
import vn.edu.iuh.fit.repositories.TourRepository;
import vn.edu.iuh.fit.service.TourService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
                .map(tour -> new TourSimpleDTO(tour.getTourId(), tour.getTourName()))
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
        logger.info("Fetching tour with ID: {}", id);

        // Lấy Tour từ repository
        Tour tour = tourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));

        // Khởi tạo các quan hệ để tránh lazy loading exception
        Hibernate.initialize(tour.getDepartures());
        Hibernate.initialize(tour.getImages()); // Khởi tạo hình ảnh
        Hibernate.initialize(tour.getReviews()); // Khởi tạo đánh giá

        logger.info("Number of Departures: {}", tour.getDepartures().size());
        logger.info("Number of Images: {}", tour.getImages().size());
        logger.info("Number of Reviews: {}", tour.getReviews().size());

        // Lấy danh sách TourPricing cho các Departure
        List<Long> departureIds = tour.getDepartures().stream()
                .map(Departure::getDepartureId)
                .collect(Collectors.toList());

        List<TourPricing> tourPricings = tourPricingRepository.findTourPricingByDepartureIds(departureIds);

        // Tạo bản đồ để ánh xạ giá tour theo departureId
        Map<Long, List<TourPricingDTO>> pricingMap = tourPricings.stream()
                .map(pricing -> {
                    TourPricingDTO dto = new TourPricingDTO();
                    dto.setPrice(pricing.getPrice());
                    dto.setParticipantType(pricing.getParticipantType());
                    dto.setModifiedDate(pricing.getModifiedDate());
                    return new AbstractMap.SimpleEntry<>(pricing.getDeparture().getDepartureId(), dto);
                })
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        // Ánh xạ destinations
        List<DestinationDTO> destinationDTOs = tour.getTourDestinations().stream()
                .sorted(Comparator.comparingInt(TourDestination::getSequenceOrder))
                .filter(td -> td.getSequenceOrder() > 0)
                .map(tourDestination -> {
                    Destination destination = tourDestination.getDestination();

                    List<Image> images = imageRepository.findByDestinationId(destination.getDestinationId());

                    logger.info("Destination {} has {} images",
                            destination.getDestinationId(),
                            images.size());

                    // 3. Chuyển đổi và trả về DestinationDTO
                    return new DestinationDTO(
                            destination.getDestinationId(),
                            destination.getName(),
                            destination.getDescription(),
                            destination.getProvince(),
                            tourDestination.getSequenceOrder(),
                            tourDestination.getDuration(),
                            images.stream()
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

                // Lấy danh sách TourPricing cho departure hiện tại
                List<TourPricingDTO> tourPricingDTOs = pricingMap.getOrDefault(departure.getDepartureId(), List.of());

                // Gán danh sách tourPricing vào departureDTO
                departureDTO.setTourPricing(tourPricingDTOs);
                // Lấy danh sách hướng dẫn viên cho departure hiện tại
                List<TourGuideAssignment> assignments = tourGuideAssignmentRepository.findByDeparture_DepartureId(departure.getDepartureId());
                List<TourGuideDTO> tourGuideDTOs = assignments.stream()
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
                logger.info("Mapped DepartureDTO: {}", departureDTO);
                departureDTOs.add(departureDTO);
            }
        }

        // Ánh xạ danh sách hình ảnh
        List<ImageDTO> imageDTOs = tour.getImages().stream()
                .map(image -> {
                    ImageDTO imageDTO = new ImageDTO();
                    imageDTO.setImageId(image.getImageId());
                    imageDTO.setImageUrl(image.getImageUrl());
                    return imageDTO;
                })
                .collect(Collectors.toList());
        // Ánh xạ danh sách đánh giá
        List<ReviewDTO> reviewDTOs = tour.getReviews().stream()
                .map(review -> {
                    ReviewDTO reviewDTO = new ReviewDTO();
                    reviewDTO.setReviewId(review.getReviewId());
                    reviewDTO.setRating(review.getRating());
                    reviewDTO.setComment(review.getComment());
                    reviewDTO.setReviewDate(review.getReviewDate());
                    reviewDTO.setUserName(review.getUser() != null ? review.getUser().getFullName() : "");
                    return reviewDTO;
                })
                .collect(Collectors.toList());
        // Gán danh sách departure, destination và images vào DTO
        TourDetailDTO tourDetailDTO = modelMapper.map(tour, TourDetailDTO.class);
        tourDetailDTO.setDepartures(departureDTOs);
        tourDetailDTO.setDestinations(destinationDTOs);
        tourDetailDTO.setImages(imageDTOs); // Gán danh sách hình ảnh
        tourDetailDTO.setReviews(reviewDTOs); // Gán danh sách đánh giá
        logger.info("Final TourDetailDTO: {}", tourDetailDTO);
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
    public TourInfoDTO convertToDTO(Tour tour) {
        return modelMapper.map(tour, TourInfoDTO.class);
    }
}
