package com.camerashop.service;

import com.camerashop.dto.RentalDTO;
import com.camerashop.entity.*;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RentalService {

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetImageRepository assetImageRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Check if an asset is available for the given date range
     */
    public boolean isAssetAvailable(String assetId, LocalDate startDate, LocalDate endDate) {
        // Check if asset exists and is available
        Asset asset = assetRepository.findById(assetId).orElse(null);
        if (asset == null || asset.getStatus() != Asset.AssetStatus.AVAILABLE) {
            return false;
        }

        // Check for overlapping rentals
        List<Rental> existingRentals = rentalRepository.findByAssetId(assetId);
        for (Rental rental : existingRentals) {
            // Skip cancelled or completed rentals
            if (rental.getStatus() == Rental.RentalStatus.CANCELLED ||
                rental.getStatus() == Rental.RentalStatus.COMPLETED) {
                continue;
            }

            // Check for date overlap
            boolean overlaps = !(endDate.isBefore(rental.getStartDate()) ||
                                startDate.isAfter(rental.getEndDate()));
            if (overlaps) {
                return false;
            }
        }

        return true;
    }

    @Transactional
    public RentalDTO createRental(String email, String assetId, LocalDate startDate, LocalDate endDate,
                                   String shippingAddress, String paymentMethod, Long shippingFee) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        if (asset.getStatus() != Asset.AssetStatus.AVAILABLE) {
            throw new RuntimeException("Asset is not available for rent");
        }

        // Check availability for the date range
        if (!isAssetAvailable(assetId, startDate, endDate)) {
            throw new RuntimeException("Asset is not available for the selected dates");
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new RuntimeException("End date must be after start date");
        }

        // Calculate fees
        long totalRentFee = asset.getDailyRate() * days;
        long depositFee = asset.getDailyRate() * 3; // 3 days deposit

        Rental.PaymentMethod paymentMethodEnum = Rental.PaymentMethod.valueOf(paymentMethod);

        Rental rental = Rental.builder()
                .user(user)
                .asset(asset)
                .startDate(startDate)
                .endDate(endDate)
                .depositFee(depositFee)
                .totalRentFee(totalRentFee)
                .penaltyFee(0L)
                .status(Rental.RentalStatus.PENDING)
                .shippingAddress(shippingAddress)
                .paymentMethod(paymentMethodEnum)
                .shippingFee(shippingFee)
                .build();

        rentalRepository.save(rental);

        return toDTO(rental);
    }

    /**
     * Extend a rental period
     */
    @Transactional
    public RentalDTO extendRental(String rentalId, LocalDate newEndDate) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (rental.getStatus() != Rental.RentalStatus.ACTIVE &&
            rental.getStatus() != Rental.RentalStatus.PENDING) {
            throw new RuntimeException("Cannot extend rental with status: " + rental.getStatus());
        }

        if (newEndDate.isBefore(rental.getEndDate())) {
            throw new RuntimeException("New end date must be after current end date");
        }

        long additionalDays = java.time.temporal.ChronoUnit.DAYS.between(rental.getEndDate(), newEndDate);
        long additionalFee = rental.getAsset().getDailyRate() * additionalDays;

        rental.setEndDate(newEndDate);
        rental.setTotalRentFee(rental.getTotalRentFee() + additionalFee);

        rentalRepository.save(rental);

        return toDTO(rental);
    }

    /**
     * Process rental return
     */
    @Transactional
    public RentalDTO returnRental(String rentalId, LocalDate returnDate) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));

        if (rental.getStatus() == Rental.RentalStatus.COMPLETED) {
            throw new RuntimeException("Rental already completed");
        }

        rental.setReturnDate(returnDate);

        // Calculate penalty if late
        if (returnDate.isAfter(rental.getEndDate())) {
            long lateDays = java.time.temporal.ChronoUnit.DAYS.between(rental.getEndDate(), returnDate);
            long penaltyRate = rental.getAsset().getDailyRate() * 2; // 2x daily rate for late penalty
            rental.setPenaltyFee(penaltyRate * lateDays);

            // Send overdue notification
            try {
                notificationService.notifyRentalOverdue(rental, lateDays);
            } catch (Exception e) {
                System.err.println("Failed to send overdue notification: " + e.getMessage());
            }
        }

        rental.setStatus(Rental.RentalStatus.COMPLETED);

        // Update asset status back to available
        Asset asset = rental.getAsset();
        asset.setStatus(Asset.AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        rentalRepository.save(rental);

        return toDTO(rental);
    }

    /**
     * Calculate rental price without creating a rental
     */
    public Map<String, Object> calculateRentalPrice(String assetId, LocalDate startDate, LocalDate endDate) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset not found"));

        long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (days <= 0) {
            throw new RuntimeException("End date must be after start date");
        }

        long totalRentFee = asset.getDailyRate() * days;
        long depositFee = asset.getDailyRate() * 3;

        return Map.of(
                "dailyRate", asset.getDailyRate(),
                "days", days,
                "totalRentFee", totalRentFee,
                "depositFee", depositFee,
                "total", totalRentFee + depositFee
        );
    }

    public List<RentalDTO> getRentalsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return rentalRepository.findByUserId(user.getUserId(), org.springframework.data.domain.PageRequest.of(0, 100))
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public RentalDTO getRentalById(String rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental not found"));
        return toDTO(rental);
    }

    private RentalDTO toDTO(Rental rental) {
        String primaryImageUrl = null;
        AssetImage img = assetImageRepository.findByAssetIdAndIsPrimaryTrue(rental.getAsset().getAssetId());
        if (img != null) {
            primaryImageUrl = img.getUrl();
        }

        return RentalDTO.builder()
                .rentalId(rental.getRentalId())
                .userId(rental.getUser().getUserId())
                .assetId(rental.getAsset().getAssetId())
                .assetName(rental.getAsset().getModelName())
                .assetBrand(rental.getAsset().getBrand())
                .primaryImageUrl(primaryImageUrl)
                .startDate(rental.getStartDate())
                .endDate(rental.getEndDate())
                .returnDate(rental.getReturnDate())
                .depositFee(rental.getDepositFee())
                .totalRentFee(rental.getTotalRentFee())
                .penaltyFee(rental.getPenaltyFee())
                .status(rental.getStatus().name())
                .shippingAddress(rental.getShippingAddress())
                .paymentMethod(rental.getPaymentMethod() != null ? rental.getPaymentMethod().name() : null)
                .shippingFee(rental.getShippingFee())
                .build();
    }
}
