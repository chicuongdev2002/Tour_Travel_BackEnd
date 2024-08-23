package vn.edu.iuh.fit.entity;import jakarta.persistence.*;import jakarta.validation.constraints.DecimalMin;import lombok.*;import org.springframework.jdbc.support.CustomSQLErrorCodesTranslation;import vn.edu.iuh.fit.enums.CustomerType;import java.math.BigDecimal;@Entity@Table(name = "tour_pricing")@Getter@Setter@AllArgsConstructor@NoArgsConstructor@Datapublic class TourPricing {    @Id    @GeneratedValue(strategy = GenerationType.IDENTITY)    private Long tourPricingId;    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")    private BigDecimal price;    @Enumerated(EnumType.STRING)    @Column(nullable = false)    private CustomerType customerType;    @ManyToOne    @JoinColumn(name = "departure_id")    private Departure departure;}