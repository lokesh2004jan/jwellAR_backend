package com.JwellARDB.ImagesDB.Controller;

import com.JwellARDB.ImagesDB.Repository.PaymentGatewayConfigRepository;
import com.JwellARDB.ImagesDB.Repository.UserActivityRepository;
import com.JwellARDB.ImagesDB.entity.JwelryItem;
import com.JwellARDB.ImagesDB.entity.PaymentGatewayConfig;
import com.JwellARDB.ImagesDB.entity.UserActivity;
import com.JwellARDB.ImagesDB.services.FirebaseService;
import com.JwellARDB.ImagesDB.services.JweleryService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminController {

    private final JweleryService service;
    private final UserActivityRepository userActivityRepo;
    private final FirebaseService firebaseService;
    private final PaymentGatewayConfigRepository paymentGatewayConfigRepository;

    // 🔥 COMMON ADMIN CHECK METHOD
    private String checkAdmin(String token) throws Exception {

        String uid = firebaseService.verifyToken(token);

        // 🔥 TEMP: Hardcode admin UID (production: check DB)
        if (!uid.equals("Jj18zoKjReNvWSzgMQoacepcAHm2")) {
            throw new RuntimeException("Not authorized");
        }

        return uid;
    }
    @GetMapping("/analytics")
    public ResponseEntity<?> analytics(
            @RequestHeader("Authorization") String token,
            @RequestParam String period
    ) throws Exception {

        checkAdmin(token);

        LocalDateTime startDate;

        switch (period) {
            case "MONTH":
                startDate = LocalDateTime.now().minusMonths(1);
                break;
            case "SIX_MONTHS":
                startDate = LocalDateTime.now().minusMonths(6);
                break;
            case "YEAR":
                startDate = LocalDateTime.now().minusYears(1);
                break;
            default:
                startDate = LocalDateTime.now().minusMonths(1);
        }

        List<UserActivity> activities =
                userActivityRepo.findByCreatedAtAfter(startDate);

        return ResponseEntity.ok(activities);
    }
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        checkAdmin(token);

        List<JwelryItem> items = service.getAllJewelry();

        List<AdminProductDTO> dto = items.stream()
                .map(j -> new AdminProductDTO(
                        j.getId(),
                        j.getName(),
                        j.getPrice(),
                        j.getClickCount(),
                        j.getTryCount(),
                        j.getPurchaseCount()
                ))
                .toList();

        return ResponseEntity.ok(
                new AdminDashboardDTO(dto, service.getTotalRevenue())
        );
    }

        @GetMapping("/products")
        public ResponseEntity<?> getProducts(
            @RequestHeader("Authorization") String token
        ) throws Exception {

        checkAdmin(token);

        List<AdminProductManageDTO> dto = service.getAllJewelry().stream()
            .map(this::toManageDTO)
            .toList();

        return ResponseEntity.ok(dto);
        }

        @PostMapping("/products")
        public ResponseEntity<?> addProduct(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductUpsertDTO request
        ) throws Exception {

        checkAdmin(token);

        JwelryItem item = new JwelryItem();
        applyProductFields(item, request);

        item.setClickCount(0L);
        item.setTryCount(0L);
        item.setPurchaseCount(0L);

        JwelryItem saved = service.save(item);
        return ResponseEntity.ok(toManageDTO(saved));
        }

        @PutMapping("/products/{id}")
        public ResponseEntity<?> updateProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody ProductUpsertDTO request
        ) throws Exception {

        checkAdmin(token);

        JwelryItem item = service.getById(id);
        applyProductFields(item, request);

        JwelryItem updated = service.save(item);
        return ResponseEntity.ok(toManageDTO(updated));
        }

        @DeleteMapping("/products/{id}")
        public ResponseEntity<?> deleteProduct(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
        ) throws Exception {

        checkAdmin(token);
        service.deleteById(id);

        return ResponseEntity.ok("Product deleted");
        }

    @GetMapping("/payment/razorpay")
    public ResponseEntity<?> getRazorpaySettings(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        checkAdmin(token);

        PaymentGatewayConfig config = paymentGatewayConfigRepository
                .findByProvider("razorpay")
                .orElse(null);

        if (config == null) {
            return ResponseEntity.ok(new RazorpaySettingsDTO("", "", "", 0L));
        }

        return ResponseEntity.ok(toRazorpaySettingsDTO(config));
    }

    @PutMapping("/payment/razorpay")
    public ResponseEntity<?> saveRazorpaySettings(
            @RequestHeader("Authorization") String token,
            @RequestBody RazorpaySettingsUpsertRequest request
    ) throws Exception {

        String adminUid = checkAdmin(token);

        String keyId = request.getKeyId() == null ? "" : request.getKeyId().trim();
        String secretId = request.getSecretId() == null ? "" : request.getSecretId().trim();

        if (keyId.isBlank() || secretId.isBlank()) {
            return ResponseEntity.badRequest().body("Both keyId and secretId are required");
        }

        PaymentGatewayConfig config = paymentGatewayConfigRepository
                .findByProvider("razorpay")
                .orElse(new PaymentGatewayConfig());

        config.setProvider("razorpay");
        config.setKeyId(keyId);
        config.setSecretId(secretId);
        config.setUpdatedByUid(adminUid);
        config.setUpdatedAt(LocalDateTime.now());

        PaymentGatewayConfig saved = paymentGatewayConfigRepository.save(config);

        return ResponseEntity.ok(toRazorpaySettingsDTO(saved));
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsersSummary(
            @RequestHeader("Authorization") String token
    ) throws Exception {

        checkAdmin(token);

        List<Object[]> results = userActivityRepo.getUserSummary();

        List<UserSummaryDTO> dto = results.stream()
                .map(r -> new UserSummaryDTO(
                        (String) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/users/{uid}")
    public ResponseEntity<?> getUserDetails(
            @RequestHeader("Authorization") String token,
            @PathVariable String uid
    ) throws Exception {

        checkAdmin(token);

        return ResponseEntity.ok(userActivityRepo.findByUserId(uid));
    }

    @Getter
    @AllArgsConstructor
    static class AdminProductDTO {
        private Long id;
        private String name;
        private Double price;
        private Long clickCount;
        private Long tryCount;
        private Long purchaseCount;
    }

    @Getter
    @AllArgsConstructor
    static class AdminDashboardDTO {
        private List<AdminProductDTO> products;
        private Double totalRevenue;
    }

    @Getter
    @AllArgsConstructor
    static class AdminProductManageDTO {
        private Long id;
        private String category;
        private String name;
        private String description;
        private String imageUrl;
        private Double price;
        private String faceShapes;
        private String skinTones;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class ProductUpsertDTO {
        private String category;
        private String name;
        private String description;
        private String imageUrl;
        private Double price;
        private String faceShapes;
        private String skinTones;
    }

    @Getter
    @AllArgsConstructor
    static class UserSummaryDTO {
        private String userId;
        private Long totalEvents;
    }

    @Getter
    @AllArgsConstructor
    static class RazorpaySettingsDTO {
        private String keyId;
        private String secretId;
        private String updatedByUid;
        private Long updatedAtEpochMillis;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    static class RazorpaySettingsUpsertRequest {
        private String keyId;
        private String secretId;
    }

    private AdminProductManageDTO toManageDTO(JwelryItem item) {
        return new AdminProductManageDTO(
                item.getId(),
                item.getCategory(),
                item.getName(),
                item.getDescription(),
                item.getImageUrl(),
                item.getPrice(),
                item.getFaceShapes(),
                item.getSkinTones()
        );
    }

    private void applyProductFields(JwelryItem item, ProductUpsertDTO request) {
        item.setCategory(request.getCategory());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setImageUrl(request.getImageUrl());
        item.setPrice(request.getPrice());
        item.setFaceShapes(normalizeJsonArray(request.getFaceShapes()));
        item.setSkinTones(normalizeJsonArray(request.getSkinTones()));
    }

    private String normalizeJsonArray(String value) {
        if (value == null || value.isBlank()) {
            return "[]";
        }
        return value;
    }

    private RazorpaySettingsDTO toRazorpaySettingsDTO(PaymentGatewayConfig config) {
        Long epochMillis = config.getUpdatedAt() == null
                ? 0L
                : config.getUpdatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return new RazorpaySettingsDTO(
                config.getKeyId(),
                config.getSecretId(),
                config.getUpdatedByUid(),
                epochMillis
        );
    }

    public enum PeriodFilter {
        MONTH,
        SIX_MONTHS,
        YEAR
    }
}