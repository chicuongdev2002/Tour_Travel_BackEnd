package vn.edu.iuh.fit.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourCardDTO {
    private long tourId;
    private String tourName;
    private String price;
    private String image;
    private Integer discount;
}
