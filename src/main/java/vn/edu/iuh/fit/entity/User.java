package vn.edu.iuh.fit.entity;import com.fasterxml.jackson.annotation.JsonIgnore;import com.fasterxml.jackson.annotation.JsonManagedReference;import jakarta.persistence.*;import lombok.*;import java.util.Set;import java.util.UUID;@Entity//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)//@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)@Inheritance(strategy = InheritanceType.JOINED)@Table(name = "users")@Getter@Setter@NoArgsConstructor@AllArgsConstructorpublic class User{    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    private long userId;    private String email;    private String fullName;    private String phoneNumber;    @OneToMany(mappedBy = "user")    @JsonIgnore    private Set<Booking> bookings;    @OneToMany(mappedBy = "user")    @JsonIgnore    private Set<Review> reviews;    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)    @JsonIgnore    private Account account;    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)    @JsonManagedReference    private Set<Address> addresses;}