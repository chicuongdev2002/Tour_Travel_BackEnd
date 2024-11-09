package vn.edu.iuh.fit.service.implement;import org.springframework.beans.factory.annotation.Autowired;import org.springframework.data.jpa.repository.JpaRepository;import org.springframework.stereotype.Service;import vn.edu.iuh.fit.dto.*;import vn.edu.iuh.fit.entity.*;import vn.edu.iuh.fit.repositories.AddressRepository;import vn.edu.iuh.fit.repositories.UserRepository;import vn.edu.iuh.fit.repositories.TourGuideAssignmentRepository; // Import repositoryimport vn.edu.iuh.fit.service.UserService;import java.util.Arrays;import java.util.HashSet;import java.util.List;import java.util.Map;import java.util.Set;import java.util.stream.Collectors;import vn.edu.iuh.fit.enums.AccountRole;@Servicepublic class UserServiceImpl extends AbstractCrudService<User, Long> implements UserService {    @Autowired    private UserRepository userRepository;    @Autowired    private TourGuideAssignmentRepository tourGuideAssignmentRepository;    @Autowired    private AddressRepository addressRepository;    @Override    protected JpaRepository<User, Long> getRepository() {        return userRepository;    }    @Override    public User findByUsername(String username) {        // Tìm User theo username (cần phải triển khai)        return null;    }    @Override    public boolean existsByEmail(String email) {        return userRepository.findByEmail(email).isPresent();    }    public List<UserAccountInfoDTO> getAllUsers() {        List<User> users = userRepository.findByAccount_RoleInAndAccount_IsActiveTrue(Arrays.asList(AccountRole.CUSTOMER, AccountRole.CUSTOMERVIP));        return users.stream().map(user -> {            UserAccountInfoDTO dto = new UserAccountInfoDTO();            dto.setUserId(user.getUserId());            dto.setEmail(user.getEmail());            dto.setFullName(user.getFullName());            dto.setPhoneNumber(user.getPhoneNumber());            dto.setAddresses(user.getAddresses().stream().map(address -> new AddressDTO(                    address.getAddressId(),                    address.getAddress()            )).collect(Collectors.toList()));            dto.setAccountRole(user.getAccount().getRole());            return dto;        }).collect(Collectors.toList());    }    @Override    public UserInfoDTO getUserById(long id) {        User user = userRepository.findById(id).orElse(null);        if (user != null) {            return convertToDTO(user);        }        return null;    }    // Hàm cập nhật user    @Override    public boolean updateUser(Long userId, UserInfoDTO userInfoDTO) {        User user = userRepository.findById(userId).orElse(null);        if (user != null) {            user.setFullName(userInfoDTO.getFullName());            user.setEmail(userInfoDTO.getEmail());            user.setPhoneNumber(userInfoDTO.getPhoneNumber());            updateAddresses(user, userInfoDTO.getAddresses());            System.out.println("Before save - Number of addresses: " + user.getAddresses().size());            userRepository.save(user);            System.out.println("After save - Number of addresses: " + user.getAddresses().size());            return true;        }        return false;    }   // Hàm update Address   private void updateAddresses(User user, List<AddressDTO> addressDTOs) {       // Khởi tạo collection nếu null       if (user.getAddresses() == null) {           user.setAddresses(new HashSet<>());       }       // Tạo một Set mới để lưu các địa chỉ đã cập nhật       Set<Address> updatedAddresses = new HashSet<>();       for (AddressDTO addressDTO : addressDTOs) {           Address address;           if (addressDTO.getAddressId() == 0) {               // Trường hợp thêm mới               address = new Address();               address.setAddress(addressDTO.getAddress());               address.setUser(user);           } else {               // Trường hợp cập nhật               address = user.getAddresses().stream()                       .filter(a -> a.getAddressId() == addressDTO.getAddressId())                       .findFirst()                       .orElseGet(() -> {                           Address newAddr = new Address();                           newAddr.setUser(user);                           return newAddr;                       });               address.setAddress(addressDTO.getAddress());           }           updatedAddresses.add(address);       }       // Xóa tất cả địa chỉ cũ       user.getAddresses().clear();       // Thêm tất cả địa chỉ đã cập nhật       user.getAddresses().addAll(updatedAddresses);       // Debug logging       System.out.println("Number of addresses in DTO: " + addressDTOs.size());       System.out.println("Number of addresses in updated set: " + updatedAddresses.size());       System.out.println("Number of addresses in user object: " + user.getAddresses().size());   }    // Phương thức lấy danh sách TourGuide từ Departure    public List<TourGuideDTO> getTourGuidesByDeparture(Departure departure) {        List<TourGuideAssignment> assignments = tourGuideAssignmentRepository.findByDeparture_DepartureId(departure.getDepartureId());        return assignments.stream()                .map(assignment -> {                    TourGuide guide = assignment.getTourGuide();                    TourGuideDTO guideDTO = new TourGuideDTO();                    guideDTO.setGuideId(guide.getUserId());                    guideDTO.setFullName(guide.getFullName());                    guideDTO.setExperienceYear(guide.getExperienceYear());                    return guideDTO;                })                .collect(Collectors.toList());    }    private UserInfoDTO convertToDTO(User user) {        UserInfoDTO userDTO = new UserInfoDTO();        userDTO.setUserId(user.getUserId());        userDTO.setEmail(user.getEmail());        userDTO.setFullName(user.getFullName());        userDTO.setPhoneNumber(user.getPhoneNumber());        // Chuyển đổi địa chỉ        List<AddressDTO> addressDTOs = user.getAddresses().stream()                .map(address -> new AddressDTO(address.getAddressId(), address.getAddress()))                .collect(Collectors.toList());        userDTO.setAddresses(addressDTOs);        return userDTO;    }    private TourInfoDTO convertTourInfo(Tour tour) {        return new TourInfoDTO(                tour.getTourId(),                tour.getTourName(),                tour.getTourDescription(),                tour.getDuration(),                tour.getStartLocation(),                tour.getTourType()        );    }    private DepartureDTO convertDeparture(Departure departure) {        if (departure == null) {            return null;        }        DepartureDTO departureDTO = new DepartureDTO();        departureDTO.setDepartureId(departure.getDepartureId());        departureDTO.setStartDate(departure.getStartDate());        departureDTO.setEndDate(departure.getEndDate());        departureDTO.setAvailableSeats(departure.getAvailableSeats());        departureDTO.setMaxParticipants(departure.getMaxParticipants());        departureDTO.setTourGuides(getTourGuidesByDeparture(departure));        departureDTO.setTourPricing(convertTourPricing(departure.getTourPricing()));        return departureDTO;    }    private List<TourPricingDTO> convertTourPricing(List<TourPricing> tourPricingList) {        return tourPricingList.stream()                .map(pricing -> new TourPricingDTO(                        pricing.getPrice(),                        pricing.getParticipantType(),                        pricing.getModifiedDate()                ))                .collect(Collectors.toList());    }    private List<PaymentInfoDTO> convertPayments(Set<Payment> payments) {        return payments.stream()                .map(payment -> new PaymentInfoDTO(                        payment.getPaymentId(),                        payment.getAmount(),                        payment.getPaymentDate(),                        payment.getPaymentMethod()                ))                .collect(Collectors.toList());    }}