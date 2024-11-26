package vn.edu.iuh.fit.controller;

import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.config.MomoPaymentConfig;
import vn.edu.iuh.fit.dto.BookingDTO;
import vn.edu.iuh.fit.dto.request.BookingRequest;
import vn.edu.iuh.fit.entity.Booking;
import vn.edu.iuh.fit.entity.Payment;
import vn.edu.iuh.fit.enums.PaymentMethod;
import vn.edu.iuh.fit.service.APIService;
import vn.edu.iuh.fit.service.PaymentService;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin("*")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private APIService apiService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private BookingController bookingController;

    @PostMapping("/momo")
    @Transactional(rollbackOn = Exception.class)
    public ResponseEntity<String> paymentMomo(@RequestParam long amount,
                                              @RequestParam String orderId,
                                              @RequestParam long userId,
                                              @RequestParam long departureId,
                                              @RequestParam String address,
                                              @RequestParam String participants
                                              ) throws Exception {
        ResponseEntity<BookingDTO> booking = bookingController.createBooking(orderId, userId, departureId, participants, address, null);
        if(booking.getBody() == null)
            throw new Exception();
        MomoPaymentConfig momoPaymentConfig = new MomoPaymentConfig();
        momoPaymentConfig.setExtraData(userId + "#" + departureId);
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

    @MessageMapping("/callBackPayment")
    @PostMapping("/momo/callback")
    public void callBack(@RequestBody LinkedHashMap<String, Object> requestBody) {
        BookingRequest extraData = BookingRequest.builder()
                .bookingId(requestBody.get("orderId").toString())
                .userId(Long.parseLong(requestBody.get("extraData").toString().split("#")[0]))
                .departureId(Long.parseLong(requestBody.get("extraData").toString().split("#")[1]))
                .build();
        Payment payment = Payment.builder()
                .amount(Long.parseLong(requestBody.get("amount") + ""))
                .paymentDate(LocalDateTime.now())
                .paymentMethod(PaymentMethod.BANKING)
                .booking(Booking.builder().bookingId(requestBody.get("orderId").toString()).build())
                .build();
        paymentService.create(payment);
        simpMessagingTemplate.convertAndSendToUser(extraData.getUserId() + "", "callBackPayment", extraData);
    }

    @GetMapping("/booking")
    public ResponseEntity<Payment> getPaymentByBooking(@RequestParam String bookingId){
        return ResponseEntity.ok(paymentService.getPaymentByBooking(bookingId));
    }
}
