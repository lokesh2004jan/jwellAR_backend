package com.JwellARDB.ImagesDB.Repository;

import com.JwellARDB.ImagesDB.entity.SavedAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedAddressRepository extends JpaRepository<SavedAddress, Long> {
    List<SavedAddress> findByUserIdOrderByUpdatedAtDesc(String userId);

    Optional<SavedAddress> findByIdAndUserId(Long id, String userId);

    long countByUserId(String userId);
}
