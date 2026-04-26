package com.JwellARDB.ImagesDB.Repository;

import com.JwellARDB.ImagesDB.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    List<OrderEntity> findByUserIdOrderByPlacedAtDesc(String userId);
}
