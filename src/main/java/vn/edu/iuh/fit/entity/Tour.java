package vn.edu.iuh.fit.entity;import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.util.Date;import java.util.Set;import java.util.UUID;@Entity@Table(name = "tours")@Getter@Setter@NoArgsConstructor@AllArgsConstructor@Datapublic class Tour {    @Id    @GeneratedValue()    private UUID tourId;    private String tourName;    @Column(length = 1000)    private String tourDescription;    private Integer duration;    private Date startLocation;    private Date endLocation;    private BigDecimal price;    private Integer maxParticipants;    //    @ManyToMany//    @JoinTable(//            name = "tour_destinations",//            joinColumns = @JoinColumn(name = "tour_id"),//            inverseJoinColumns = @JoinColumn(name = "destination_id")//    )//    private Set<Destination> destinations;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)    private Set<Departure> departures;    @OneToMany(mappedBy = "tour")    private Set<TourDestination> tourDestinations;    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)    private Set<Review> reviews;}