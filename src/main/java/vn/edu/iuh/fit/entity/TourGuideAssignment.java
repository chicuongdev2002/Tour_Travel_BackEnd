package vn.edu.iuh.fit.entity;import jakarta.persistence.*;import lombok.AllArgsConstructor;import lombok.Getter;import lombok.NoArgsConstructor;import lombok.Setter;import vn.edu.iuh.fit.enums.AssignStatus;import vn.edu.iuh.fit.pks.TourGuideAssignmentId;import java.io.Serializable;import java.time.LocalDateTime;@Entity@Table(name = "tour_guide_assignments")@Getter@Setter@NoArgsConstructor@AllArgsConstructor@IdClass(TourGuideAssignmentId.class)public class TourGuideAssignment implements Serializable {    private LocalDateTime assignmentDate;    @Enumerated(EnumType.STRING)    private AssignStatus status;    @Id    @ManyToOne    @JoinColumn(name = "departure_Id", insertable = false, updatable = false)    private Departure departure;    @Id    @ManyToOne    @JoinColumn(name = "guide_Id", insertable = false, updatable = false)    private TourGuide tourGuide;}