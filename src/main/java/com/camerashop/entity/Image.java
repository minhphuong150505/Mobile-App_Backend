package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    private Long entityId; // productId or assetId

    @Column(length = 1000, nullable = false)
    private String url;

    @Column(nullable = false)
    private Boolean isPrimary;

    private String type; // 'PRODUCT' | 'ASSET'
}
