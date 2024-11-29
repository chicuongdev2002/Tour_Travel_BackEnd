package vn.edu.iuh.fit.dto;import lombok.AllArgsConstructor;import lombok.Getter;import lombok.NoArgsConstructor;import lombok.Setter;import java.time.LocalDateTime;@Getter@Setter@NoArgsConstructor@AllArgsConstructorpublic class CheckInResponeDTO {    private Long bookingId;    private UserDTO user;    private DepartureDTO departure;    private TourGuideDTO tourGuide;    @Getter    @Setter    @NoArgsConstructor    public static class UserDTO {        private Long userId;        private String fullName;        public UserDTO(Long userId, String fullName) {            this.userId = userId;            this.fullName = fullName;        }    }    @Getter    @Setter    @NoArgsConstructor    @AllArgsConstructor    public static class DepartureDTO {        private Long departureId;        private LocalDateTime startDate;        private LocalDateTime endDate;        private TourDTO tour;        public DepartureDTO(Long departureId, TourDTO tour) {            this.departureId = departureId;            this.tour = tour;        }    }    @Getter    @Setter    @NoArgsConstructor    public static class TourDTO {        private Long tourId;        private String tourName;        private String tourDescription;        private UserDTO user; // Chỉ cần thông tin user        public TourDTO(Long tourId, String tourName, String tourDescription, UserDTO user) {            this.tourId = tourId;            this.tourName = tourName;            this.tourDescription = tourDescription;            this.user = user;        }    }}