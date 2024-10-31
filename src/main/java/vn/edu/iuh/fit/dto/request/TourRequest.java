package vn.edu.iuh.fit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.fit.entity.Departure;
import vn.edu.iuh.fit.entity.Tour;
import vn.edu.iuh.fit.entity.TourDestination;
import vn.edu.iuh.fit.enums.TourType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourRequest {
    private Tour tour;
    private Departure departure;
    private List<TourDestination> tourDestinations;
}
