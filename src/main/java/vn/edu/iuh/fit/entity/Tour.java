package vn.edu.iuh.fit.entity;import jakarta.persistence.*;import lombok.*;import vn.edu.iuh.fit.enums.TourType;import java.math.BigDecimal;import java.util.*;@Entity@Table(name = "tours")@Getter@Setter@NoArgsConstructor@AllArgsConstructor@Datapublic class Tour {    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    private long tourId;    private String tourName;    @Column(length = 1000)    private String tourDescription;    private Integer duration;    private String startLocation;//    private Integer maxParticipants;    @Enumerated(EnumType.STRING)    private TourType tourType;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    private Set<Departure> departures;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    private Set<TourDestination> tourDestinations;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    private Set<Review> reviews;    // Hình ảnh tour    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    private Set<Image> images;    //Discount    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)    private List<Discount> discounts;    //TourGuide    @Override    public boolean equals(Object o) {        if (this == o) return true;        if (!(o instanceof Tour tour)) return false;        return getTourId() == tour.getTourId();    }    @Override    public int hashCode() {        return Objects.hash(getTourId());    }}