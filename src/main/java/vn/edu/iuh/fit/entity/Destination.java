package vn.edu.iuh.fit.entity;import com.fasterxml.jackson.annotation.JsonIgnore;import jakarta.persistence.*;import lombok.*;import java.util.Set;@Entity@Table(name = "destinations")@Getter@Setter@NoArgsConstructor@AllArgsConstructorpublic class Destination {    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    private long destinationId;    private String name;    @Column(length = 1000)    private String description;    private String province;    @OneToMany(mappedBy = "destination")    @JsonIgnore    private Set<TourDestination> tourDestinations;    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, fetch = FetchType.LAZY)    @JsonIgnore    private Set<Image> images;}