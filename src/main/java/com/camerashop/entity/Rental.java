package com.camerashop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "rentals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String rentalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private LocalDate returnDate;

    @Column(nullable = false)
    private Long depositFee;

    @Column(nullable = false)
    private Long totalRentFee;

    private Long penaltyFee = 0L;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RentalStatus status = RentalStatus.PENDING;

    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private Long shippingFee = 0L;

    public enum RentalStatus {
        PENDING, ACTIVE, COMPLETED, CANCELLED
    }

    public enum PaymentMethod {
        COD, MoMo
    }
}
