package vn.edu.iuh.fit.entity;import com.fasterxml.jackson.annotation.JsonIgnore;import jakarta.persistence.*;import lombok.*;import vn.edu.iuh.fit.enums.TourType;import java.math.BigDecimal;import java.util.*;@Entity@Table(name = "tours")@Getter@Setter@NoArgsConstructor@AllArgsConstructor@Builderpublic class Tour{    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    private long tourId;    private String tourName;    @Column(length = 1000)    private String tourDescription;    private Integer duration;    private String startLocation;    private boolean isActive;//    private Integer maxParticipants;    @Enumerated(EnumType.STRING)    private TourType tourType;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    @JsonIgnore    private Set<Departure> departures;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    @JsonIgnore    private List<TourDestination> tourDestinations;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    @JsonIgnore    private Set<Review> reviews;    // Hình ảnh tour    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    @JsonIgnore    private Set<Image> images;    //Discount    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    @JsonIgnore    private List<Discount> discounts;    //TourProvider    @ManyToOne    @JoinColumn(name = "user_id", nullable = false)    private User user;    @Override    public boolean equals(Object o) {        if (this == o) return true;        if (!(o instanceof Tour tour)) return false;        return getTourId() == tour.getTourId();    }    @Override    public int hashCode() {        return Objects.hash(getTourId());    }}