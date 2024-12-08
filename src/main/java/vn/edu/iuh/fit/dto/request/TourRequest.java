package vn.edu.iuh.fit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.fit.entity.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TourRequest {
    private Tour tour;
    private List<Departure> departures;
    private List<TourDestination> tourDestinations;
    private List<TourPricing> tourPricing;
    private List<Image> images;
    private long userId;
}
