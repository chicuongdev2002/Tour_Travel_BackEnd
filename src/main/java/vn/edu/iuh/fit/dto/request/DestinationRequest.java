package vn.edu.iuh.fit.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DestinationRequest {
    private String name;
    private String description;
    private String province;
    private String image;
}
