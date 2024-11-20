package vn.edu.iuh.fit.config;

import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Data
@AllArgsConstructor
public class MomoPaymentConfig {
    private final String accessKey = "F8BBA842ECF85";
    private final String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    private final String orderInfo = "Thanh toán với MoMo";
    private final String partnerCode = "MOMO";
    private final String redirectUrl = "http://localhost:5173";
    private final String ipnUrl = "https://f571-101-99-24-11.ngrok-free.app/api/payment/momo/callback";
    private final String requestType = "payWithMethod";
    private final String extraData = "";
    private final String orderGroupId = "";
    private final boolean autoCapture = true;
    private final String lang = "vi";

    public String calculateHMac(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");

        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        return byteArrayToHex(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
    }

    public String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}