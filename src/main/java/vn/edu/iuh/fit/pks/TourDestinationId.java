package vn.edu.iuh.fit.pks;import lombok.*;import java.io.Serializable;import java.util.Objects;@AllArgsConstructor@NoArgsConstructor@Getter@Setterpublic class TourDestinationId implements Serializable {    private Long tour;    private Long destination;    @Override    public boolean equals(Object o) {        if (this == o) return true;        if (!(o instanceof TourDestinationId that)) return false;        return Objects.equals(getTour(), that.getTour()) && Objects.equals(getDestination(), that.getDestination());    }    @Override    public int hashCode() {        return Objects.hash(getTour(), getDestination());    }}