package vn.edu.iuh.fit.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MomoRequestBody {
    private String partnerCode;
    private String partnerName;
    private String  storeId;
    private String requestId;
    private long amount;
    private String orderId;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String lang;
    private String requestType;
    private boolean autoCapture;
    private String extraData;
    private String orderGroupId;
    private String signature;
}
