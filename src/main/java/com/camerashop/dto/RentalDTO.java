package com.camerashop.dto;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalDTO {
    private String rentalId;
    private String userId;
    private String assetId;
    private String assetName;
    private String assetBrand;
    private String primaryImageUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate returnDate;
    private Long depositFee;
    private Long totalRentFee;
    private Long penaltyFee;
    private String status;
    private String shippingAddress;
    private String paymentMethod;
    private Long shippingFee;
}
