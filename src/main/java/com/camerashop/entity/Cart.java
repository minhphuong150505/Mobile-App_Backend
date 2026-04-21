package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "Carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    private Long userId;
    private Long productId;
    private Integer quantity;
}
