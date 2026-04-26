package com.JwellARDB.ImagesDB.services;

import com.JwellARDB.ImagesDB.Repository.JewelryRepository;
import com.JwellARDB.ImagesDB.Repository.UserActivityRepository;
import com.JwellARDB.ImagesDB.entity.JwelryItem;
import com.JwellARDB.ImagesDB.entity.UserActivity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JweleryService {

    private final JewelryRepository jewelryRepo;

    private final UserActivityRepository userActivityRepo;

    public JweleryService(JewelryRepository jewelryRepo, UserActivityRepository userActivityRepo) {
        this.jewelryRepo = jewelryRepo;
        this.userActivityRepo = userActivityRepo;
    }

    public List<JwelryItem> getAllJewelry() {
        return jewelryRepo.findAll();
    }

    public List<JwelryItem> getJewelryByShapesAndTones(String face, String tone) {
        if ((face == null || face.isBlank()) &&
                (tone == null || tone.isBlank())) {
            return jewelryRepo.findAll();
        }
        return jewelryRepo.filter(face, tone);
    }

    // 🔥 Analytics Increment Methods

    @Transactional
    public void incrementClick(Long id, String uid) {

        JwelryItem item = jewelryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Jewelry not found"));

        Long current = item.getClickCount() != null ? item.getClickCount() : 0L;
        item.setClickCount(current + 1);

        jewelryRepo.save(item);

        // 🔥 STORE EVENT WITH TIMESTAMP
        userActivityRepo.save(
                new UserActivity(
                        null,
                        uid,
                        id,
                        "CLICK",
                        0.0,
                        LocalDateTime.now()
                )
        );
    }
    @Transactional
    public void incrementTry(Long id, String uid) {

        JwelryItem item = jewelryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Jewelry not found"));

        Long current = item.getTryCount() != null ? item.getTryCount() : 0L;
        item.setTryCount(current + 1);

        jewelryRepo.save(item);

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
    }

    @Transactional
    public void incrementPurchase(Long id, String uid) {

        JwelryItem item = jewelryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Jewelry not found"));

        Long current = item.getPurchaseCount() != null ? item.getPurchaseCount() : 0L;
        item.setPurchaseCount(current + 1);

        jewelryRepo.save(item);

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
    }

    // 🔥 Revenue Calculation

    public Double getTotalRevenue() {

        return jewelryRepo.findAll()
                .stream()
                .mapToDouble(j -> {

                    Double price = j.getPrice() != null ? j.getPrice() : 0.0;
                    Long purchase = j.getPurchaseCount() != null ? j.getPurchaseCount() : 0L;

                    return price * purchase;
                })
                .sum();
    }

        public JwelryItem save(JwelryItem item) {
                return jewelryRepo.save(item);
        }

        public void deleteById(Long id) {
                jewelryRepo.deleteById(id);
        }

    public JwelryItem getById(Long id) {
        return jewelryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
    }
}