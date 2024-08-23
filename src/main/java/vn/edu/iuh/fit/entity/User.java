package vn.edu.iuh.fit.entity;import jakarta.persistence.*;import lombok.*;import java.util.Set;import java.util.UUID;@Entity@Table(name = "users")@Getter@Setter@NoArgsConstructor@AllArgsConstructor@Datapublic class User {    @Id    @GeneratedValue()    private UUID userId;    private String username;    private String email;    private String password;    private String fullName;    private String phoneNumber;    @OneToMany(mappedBy = "user")    private Set<Booking> bookings;    @OneToMany(mappedBy = "user")    private Set<Review> reviews;    public Set<Review> getReviews() {        return reviews;    }    public void setReviews(Set<Review> reviews) {        this.reviews = reviews;    }}