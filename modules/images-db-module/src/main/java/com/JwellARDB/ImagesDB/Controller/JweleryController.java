package com.JwellARDB.ImagesDB.Controller;

import com.JwellARDB.ImagesDB.Repository.UserActivityRepository;
import com.JwellARDB.ImagesDB.entity.JwelryItem;
import com.JwellARDB.ImagesDB.entity.UserActivity;
import com.JwellARDB.ImagesDB.services.JweleryService;
import com.JwellARDB.ImagesDB.services.FirebaseService; // ✅ correct import
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/jewelry")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor   // 🔥 cleaner than manual constructor
public class JweleryController {

    private final JweleryService service;
    private final FirebaseService firebaseService;
    private final UserActivityRepository userActivityRepo;

    @GetMapping("/test")
    public String test(){
        return "working!!!";
    }
    @GetMapping("/{id}")
    public JwelryItemDTO getById(@PathVariable Long id) {

        JwelryItem item = service.getById(id);

        return convertToDTO(item);
    }

    @PostMapping("/{id}/try")
    public ResponseEntity<?> tryJewelry(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) throws Exception {

        String uid = firebaseService.verifyToken(token);

        service.incrementTry(id,uid);

        userActivityRepo.save(
                new UserActivity(
                        null,
                        uid,
                        id,
                        "TRY",
                        0.0,
                        LocalDateTime.now()
                )
        );

        return ResponseEntity.ok("Try recorded");
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchase(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) throws Exception {

        String uid = firebaseService.verifyToken(token);

        // increase jewellery purchase count
        service.incrementPurchase(id,uid);

        // get jewellery price
        JwelryItem item = service.getById(id);

        userActivityRepo.save(
                new UserActivity(
                        null,
                        uid,
                        id,
                        "PURCHASE",
                        item.getPrice(),
                        LocalDateTime.now()
                )
        );

        return ResponseEntity.ok("Purchase recorded");
    }

    @GetMapping("/all")
    public List<JwelryItemDTO> getAll() {
        return service.getAllJewelry()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/filter")
    public List<JwelryItemDTO> getFilteredJewelry(
            @RequestParam(required = false) String face,
            @RequestParam(required = false) String tone
    ) {
        return service.getJewelryByShapesAndTones(face, tone)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    // 🔥 CLICK (secure)
    @PostMapping("/{id}/click")
    public ResponseEntity<?> click(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id
    ) throws Exception {

        String uid = firebaseService.verifyToken(token);

        service.incrementClick(id,uid);

        userActivityRepo.save(
                new UserActivity(null, uid, id, "CLICK", 0.0, LocalDateTime.now())
        );

        return ResponseEntity.ok("Click recorded");
    }

    public JwelryItemDTO convertToDTO(JwelryItem item) {
        return new JwelryItemDTO(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getImageUrl(),
                item.getPrice()
        );
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JwelryItemDTO {
        private Long id;
        private String name;
        private String description;
        private String imageUrl;
        private Double price;
    }
}