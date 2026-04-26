package com.JwellARDB.ImagesDB.Repository;

import com.JwellARDB.ImagesDB.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrderIdOrderByIdAsc(Long orderId);
}
