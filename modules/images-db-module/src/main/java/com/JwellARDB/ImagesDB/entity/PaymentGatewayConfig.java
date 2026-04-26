package com.JwellARDB.ImagesDB.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_gateway_config")
public class PaymentGatewayConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String provider;

    @Column(name = "key_id", nullable = false)
    private String keyId;

    @Column(name = "secret_id", nullable = false)
    private String secretId;

    @Column(name = "updated_by_uid", nullable = false)
    private String updatedByUid;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
