package com.JwellARDB.ImagesDB.Repository;

import com.JwellARDB.ImagesDB.entity.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUserIdOrderByAddedAtDesc(String userId);

    Optional<WatchlistItem> findByUserIdAndJewelleryId(String userId, Long jewelleryId);

    void deleteByUserIdAndJewelleryId(String userId, Long jewelleryId);
}
