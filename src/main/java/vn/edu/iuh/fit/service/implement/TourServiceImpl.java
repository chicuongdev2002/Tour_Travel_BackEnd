package vn.edu.iuh.fit.service.implement;import com.amazonaws.auth.AWSStaticCredentialsProvider;import com.amazonaws.auth.BasicAWSCredentials;import com.amazonaws.services.s3.AmazonS3;import com.amazonaws.services.s3.AmazonS3ClientBuilder;import com.amazonaws.services.s3.model.CannedAccessControlList;import com.amazonaws.services.s3.model.PutObjectRequest;import jakarta.annotation.PostConstruct;import org.modelmapper.ModelMapper;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.beans.factory.annotation.Value;import org.springframework.cache.annotation.Cacheable;import org.springframework.data.domain.Page;import org.springframework.data.domain.PageRequest;import org.springframework.data.domain.Pageable;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;import org.springframework.util.StringUtils;import org.springframework.web.multipart.MultipartFile;import vn.edu.iuh.fit.dto.TourDetailDTO;import vn.edu.iuh.fit.dto.TourSummaryDTO;import vn.edu.iuh.fit.entity.Destination;import vn.edu.iuh.fit.entity.Image;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.exception.ResourceNotFoundException;import vn.edu.iuh.fit.repositories.TourRepository;import vn.edu.iuh.fit.service.AbstractCrudService;import vn.edu.iuh.fit.service.TourService;import java.io.File;import java.io.IOException;import java.math.BigDecimal;import java.util.HashSet;import java.util.List;import java.util.UUID;@Service@Transactionalpublic class TourServiceImpl extends AbstractCrudService<Tour, UUID> implements TourService {    @Autowired    private TourRepository tourRepository;    private final ModelMapper modelMapper;    @Value("${aws.s3.bucket}")    private String bucketName;    @Value("${aws.access.key}")    private String accessKey;    @Value("${aws.secret.key}")    private String secretKey;    @Value("${aws.region}")    private String region;    private AmazonS3 s3Client;    @Autowired    public TourServiceImpl(TourRepository tourRepository, ModelMapper modelMapper) {        this.tourRepository = tourRepository;        this.modelMapper = modelMapper;    }    @PostConstruct    public void initializeAmazonS3Client() {        if (StringUtils.isEmpty(accessKey) || StringUtils.isEmpty(secretKey)) {            throw new IllegalStateException("AWS credentials not properly configured");        }        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);        this.s3Client = AmazonS3ClientBuilder.standard()                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))                .withRegion(region)                .build();    }    @Override    protected JpaRepository<Tour, UUID> getRepository() {        return tourRepository;    }    @Override    public List<Tour> findToursByDestination(Destination destination) {        return null;    }    @Override    public List<Tour> findToursByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {        return null;    }    @Cacheable("tours")    public Page<TourSummaryDTO> getTours(int page, int size, BigDecimal minPrice, BigDecimal maxPrice) {        Pageable pageable = PageRequest.of(page, size);        Page<Tour> tours = tourRepository.findToursWithPriceRange(minPrice, maxPrice, pageable);        return tours.map(tour -> modelMapper.map(tour, TourSummaryDTO.class));    }    @Cacheable("tour")    public TourDetailDTO getTourById(UUID id) {        Tour tour = tourRepository.findById(id)                .orElseThrow(() -> new ResourceNotFoundException("Tour not found"));        return modelMapper.map(tour, TourDetailDTO.class);    }    public Tour convertDtoToEntity(TourDetailDTO tourDetailDTO) {        return modelMapper.map(tourDetailDTO, Tour.class);    }    @Override    public String uploadImageToAWS(File file, Tour tour) throws IOException {        String fileName = UUID.randomUUID() + "_" + file.getName();        try {            s3Client.putObject(new PutObjectRequest(bucketName, fileName, file));//                    .withCannedAcl(CannedAccessControlList.PublicRead);            String fileUrl = s3Client.getUrl(bucketName, fileName).toString();            Image image = new Image();            if (tour.getImages() == null) {                tour.setImages(new HashSet<>());            }            image.setImageUrl(fileUrl);            image.setTour(tour);            tour.getImages().add(image);            tourRepository.save(tour);            return fileUrl;        } catch (Exception e) {            throw new IOException("Error uploading image: " + e.getMessage(), e);        } finally {            // Dọn dẹp file tạm thời nếu cần            if (file.exists()) {                file.delete();            }        }    }    // Phương thức chuyển đổi MultipartFile thành File    @Override    public File convertMultiPartToFile(MultipartFile file) throws IOException {        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());        file.transferTo(convFile);        System.out.println("File được tạo: " + convFile.getAbsolutePath());        return convFile;    }}