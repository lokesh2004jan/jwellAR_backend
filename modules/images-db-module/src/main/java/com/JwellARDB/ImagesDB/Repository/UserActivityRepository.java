package com.JwellARDB.ImagesDB.Repository;

import com.JwellARDB.ImagesDB.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {

    List<UserActivity> findByUserId(String userId);
    List<UserActivity> findByCreatedAtAfter(LocalDateTime date);

    @Query("""
        SELECT ua.userId,
               SUM(CASE WHEN ua.eventType='CLICK' THEN 1 ELSE 0 END),
               SUM(CASE WHEN ua.eventType='TRY' THEN 1 ELSE 0 END),
               SUM(CASE WHEN ua.eventType='PURCHASE' THEN 1 ELSE 0 END),
               SUM(ua.amount)
        FROM UserActivity ua
        GROUP BY ua.userId
    """)
    List<Object[]> getUserSummary();
}