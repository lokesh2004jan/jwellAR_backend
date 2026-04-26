package com.JwellARDB.ImagesDB.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "jewellery")
public class JwelryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String name;
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    private Double price;

    @Column(columnDefinition = "json")
    private String faceShapes;

    @Column(columnDefinition = "json")
    private String skinTones;

    // 🔥 Analytics Fields
    @Column(name = "click_count")
    private Long clickCount = 0L;

    @Column(name = "try_count")
    private Long tryCount = 0L;

    @Column(name = "purchase_count")
    private Long purchaseCount = 0L;
}