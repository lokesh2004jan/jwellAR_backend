package com.JwellARDB.ImagesDB.Repository;

import com.JwellARDB.ImagesDB.entity.JwelryItem;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface JewelryRepository extends JpaRepository<JwelryItem, Long> {

    @Query(value = """
        SELECT * FROM jewellery
        WHERE (:face IS NULL OR JSON_CONTAINS(face_shapes, CONCAT('"', :face, '"')))
        AND (:tone IS NULL OR JSON_CONTAINS(skin_tones, CONCAT('"', :tone, '"')))
        """,
            nativeQuery = true)
    List<JwelryItem> filter(
            @Param("face") String face,
            @Param("tone") String tone
    );


    // 🔥 Increment Counters

    @Modifying
    @Transactional
    @Query("UPDATE JwelryItem j SET j.clickCount = j.clickCount + 1 WHERE j.id = :id")
    void incrementClick(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE JwelryItem j SET j.tryCount = j.tryCount + 1 WHERE j.id = :id")
    void incrementTry(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE JwelryItem j SET j.purchaseCount = j.purchaseCount + 1 WHERE j.id = :id")
    void incrementPurchase(@Param("id") Long id);
}