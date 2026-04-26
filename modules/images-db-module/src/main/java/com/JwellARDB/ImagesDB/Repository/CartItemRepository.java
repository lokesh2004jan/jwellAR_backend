package com.JwellARDB.ImagesDB.Repository;

import com.JwellARDB.ImagesDB.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdOrderByAddedAtDesc(String userId);

    Optional<CartItem> findByUserIdAndJewelleryId(String userId, Long jewelleryId);

    void deleteByUserIdAndJewelleryId(String userId, Long jewelleryId);

    void deleteByUserIdAndJewelleryIdIn(String userId, List<Long> jewelleryIds);
}
