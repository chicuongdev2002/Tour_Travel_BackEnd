package vn.edu.iuh.fit.service.implement;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.http.HttpStatus;import org.springframework.http.ResponseEntity;import org.springframework.stereotype.Service;import vn.edu.iuh.fit.dto.DiscountDTO;import vn.edu.iuh.fit.dto.DiscountRequest;import vn.edu.iuh.fit.entity.Discount;import vn.edu.iuh.fit.entity.Tour;import vn.edu.iuh.fit.mailservice.EmailService;import vn.edu.iuh.fit.repositories.DiscountRepository;import vn.edu.iuh.fit.repositories.TourRepository;import vn.edu.iuh.fit.service.DiscountService;import java.time.LocalDateTime;import java.time.format.DateTimeFormatter;import java.util.List;import java.util.Optional;import java.util.UUID;import java.util.stream.Collectors;@Servicepublic class DiscountServiceImpl extends AbstractCrudService<Discount, Long> implements DiscountService {    @Autowired    private DiscountRepository discountRepository;    @Autowired    private TourRepository tourRepository;    @Override    protected JpaRepository<Discount, Long> getRepository() {        return discountRepository;    }    @Autowired    private EmailService emailService;    public ResponseEntity<String> sendDiscountCodesToEmails(List<String> emails, List<Long> discountIds) {        List<Discount> discounts = discountRepository.findAllById(discountIds);        if (discounts.isEmpty()) {            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không có mã giảm giá nào tồn tại.");        }        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");        // Lặp qua từng email        for (String email : emails) {            // Gửi từng mã giảm giá cho email            for (Discount discount : discounts) {                String discountCode = discount.getDiscountCode();                LocalDateTime startDate = discount.getStartDate();                LocalDateTime endDate = discount.getEndDate();                String tourName = discount.getTour().getTourName();                String imageUrl = discount.getTour().getImages().stream()                        .findFirst()                        .map(img -> img.getImageUrl())                        .orElse("default-image-url.jpg");                String startDateString = startDate.format(formatter);                String endDateString = endDate.format(formatter);                // Gửi email cho từng mã giảm giá                emailService.sendDiscountEmail(email, discountCode, tourName, imageUrl,startDateString, endDateString);            }        }        // Trả về phản hồi thành công        return ResponseEntity.status(HttpStatus.OK).body("Các mã khuyến mãi đã được gửi thành công đến các email.");    }    public List<DiscountDTO> getAllDiscounts() {        return discountRepository.findAll().stream()                .map(this::convertToDTO)                .collect(Collectors.toList());    }    @Override    public ResponseEntity<DiscountDTO> getDiscountById(Long id) {        Optional<Discount> discount = discountRepository.findById(id);        return discount.map(d -> ResponseEntity.ok(convertToDTO(d)))                .orElseGet(() -> ResponseEntity.notFound().build());    }    public DiscountDTO createDiscount(Long tourId, DiscountRequest request) {        Tour tour = tourRepository.findById(tourId)                .orElseThrow(() -> new RuntimeException("Tour not found"));        String generatedCode = generateDiscountCode(tour.getTourName(), request.getStartDate());        Discount discount = new Discount();        discount.setDiscountCode(generatedCode);        discount.setDiscountAmount(request.getDiscountAmount());        discount.setStartDate(request.getStartDate());        discount.setEndDate(request.getEndDate());        discount.setCountUse(request.getCountUse());        discount.setTour(tour);        discountRepository.save(discount);        return convertToDTO(discount);    }    private String generateDiscountCode(String tourName, LocalDateTime startDate) {        // Chỉ lấy các ký tự chữ cái đầu tiên trong tên tour và ngày tháng        String tourInitials = tourName.replaceAll("[^A-Za-z]", "").substring(0, 3).toUpperCase();        String datePart = startDate.format(DateTimeFormatter.ofPattern("yyMMddHHmm"));        return tourInitials + "-" + datePart + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();    }    @Override    public ResponseEntity<DiscountDTO> updateDiscount(Long id, Discount discountDetails) {        Optional<Discount> optionalDiscount = discountRepository.findById(id);        if (optionalDiscount.isPresent()) {            Discount discount = optionalDiscount.get();            discount.setDiscountCode(discountDetails.getDiscountCode());            discount.setDiscountAmount(discountDetails.getDiscountAmount());            discount.setStartDate(discountDetails.getStartDate());            discount.setEndDate(discountDetails.getEndDate());            discount.setCountUse(discountDetails.getCountUse());            return ResponseEntity.ok(convertToDTO(discountRepository.save(discount)));        } else {            return ResponseEntity.notFound().build();        }    }    @Override    public ResponseEntity<Void> deleteDiscount(Long id) {        if (discountRepository.existsById(id)) {            discountRepository.deleteById(id);            return ResponseEntity.ok().build();        } else {            return ResponseEntity.notFound().build();        }    }    private DiscountDTO convertToDTO(Discount discount) {        return new DiscountDTO(                discount.getId(),                discount.getDiscountCode(),                discount.getDiscountAmount(),                discount.getStartDate(),                discount.getEndDate(),                discount.getCountUse(),                discount.getTour() != null ? discount.getTour().getTourId() : null,                discount.getTour() != null ? discount.getTour().getTourName() : null        );    }}