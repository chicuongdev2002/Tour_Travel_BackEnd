package vn.edu.iuh.fit.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.config.MomoPaymentConfig;
import vn.edu.iuh.fit.config.MomoRequestBody;
import vn.edu.iuh.fit.service.APIService;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    @Autowired
    private APIService apiService;

    @PostMapping("/momo")
    public ResponseEntity<String> paymentMomo(@RequestParam long amount,
                                              @RequestParam String orderId) throws Exception {
        MomoPaymentConfig momoPaymentConfig = new MomoPaymentConfig();
        String rawSignature = "accessKey=" + momoPaymentConfig.getAccessKey()
                + "&amount=" + amount + "&extraData=" + momoPaymentConfig.getExtraData()
                + "&ipnUrl=" + momoPaymentConfig.getIpnUrl() + "&orderId=" + orderId
                + "&orderInfo=" + momoPaymentConfig.getOrderInfo()
                + "&partnerCode=" + momoPaymentConfig.getPartnerCode()
                + "&redirectUrl=" + momoPaymentConfig.getRedirectUrl()
                + "&requestId=" + orderId
                + "&requestType=" + momoPaymentConfig.getRequestType();
        String signature = momoPaymentConfig.calculateHMac(momoPaymentConfig.getSecretKey(), rawSignature);
        Map<String, Object> requestBodyMap = new HashMap<>();
        requestBodyMap.put("partnerCode", momoPaymentConfig.getPartnerCode());
        requestBodyMap.put("partnerName", "Test");
        requestBodyMap.put("storeId", "MomoTestStore");
        requestBodyMap.put("requestId", orderId);
        requestBodyMap.put("amount", amount);
        requestBodyMap.put("orderId", orderId);
        requestBodyMap.put("orderInfo", momoPaymentConfig.getOrderInfo());
        requestBodyMap.put("redirectUrl", momoPaymentConfig.getRedirectUrl());
        requestBodyMap.put("ipnUrl", momoPaymentConfig.getIpnUrl());
        requestBodyMap.put("lang", momoPaymentConfig.getLang());
        requestBodyMap.put("requestType", momoPaymentConfig.getRequestType());
        requestBodyMap.put("autoCapture", momoPaymentConfig.isAutoCapture());
        requestBodyMap.put("extraData", momoPaymentConfig.getExtraData());
        requestBodyMap.put("orderGroupId", momoPaymentConfig.getOrderGroupId());
        requestBodyMap.put("signature", signature);
        String url = "https://test-payment.momo.vn/v2/gateway/api/create";
        Gson gson = new Gson();
        String requestBody = gson.toJson(requestBodyMap);
        return ResponseEntity.ok(apiService.postData(url, requestBody));
    }

    @PostMapping("/momo/callback")
    public ResponseEntity<String> callBack(@RequestBody Object requestBody){
        System.out.println(requestBody);
        return null;
    }
}
