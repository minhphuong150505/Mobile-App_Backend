package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    private User user;

    @Column(name = "entity_id")
    private Long entityId; // productId or assetId

    private Integer rating;

    private String comment;

    private String type; // 'PRODUCT' | 'ASSET'
}
