package vn.edu.iuh.fit.entity;import com.fasterxml.jackson.annotation.JsonIgnore;import com.fasterxml.jackson.annotation.JsonManagedReference;import jakarta.persistence.*;import lombok.*;import java.util.*;@Entity//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)//@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)@Inheritance(strategy = InheritanceType.JOINED)@Table(name = "users")@Getter@Setter@NoArgsConstructor@AllArgsConstructor@Builderpublic class User{    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    private long userId;    private String email;    private String fullName;    private String phoneNumber;    @OneToMany(mappedBy = "user")    @JsonIgnore    private Set<Booking> bookings;    @OneToMany(mappedBy = "user")    @JsonIgnore    private Set<Review> reviews;    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)    @JsonIgnore    private Account account;    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)    private Set<Address> addresses = new HashSet<>();    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)    @JsonIgnore    private List<Notification> notifySent;    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)    @JsonIgnore    private List<Notification> notifyReceived;    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)    @JsonIgnore    private Set<FavoriteTour> favoriteTours = new HashSet<>();    @Override    public boolean equals(Object o) {        if (this == o) return true;        if (!(o instanceof User user)) return false;        return getUserId() == user.getUserId();    }    @Override    public int hashCode() {        return Objects.hash(getUserId());    }}