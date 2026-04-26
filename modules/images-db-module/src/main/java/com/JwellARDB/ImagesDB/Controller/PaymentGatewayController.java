package com.JwellARDB.ImagesDB.Controller;

import com.JwellARDB.ImagesDB.Repository.PaymentGatewayConfigRepository;
import com.JwellARDB.ImagesDB.entity.PaymentGatewayConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentGatewayController {

    private final PaymentGatewayConfigRepository paymentGatewayConfigRepository;

    @GetMapping("/razorpay/public")
    public ResponseEntity<?> getPublicRazorpayKey() {
        PaymentGatewayConfig config = paymentGatewayConfigRepository
                .findByProvider("razorpay")
                .orElse(null);

        if (config == null || config.getKeyId() == null || config.getKeyId().isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Razorpay key is not configured");
        }

        return ResponseEntity.ok(new PublicRazorpayKeyDTO(config.getKeyId()));
    }

    @Getter
    @AllArgsConstructor
    static class PublicRazorpayKeyDTO {
        private String keyId;
    }
}
