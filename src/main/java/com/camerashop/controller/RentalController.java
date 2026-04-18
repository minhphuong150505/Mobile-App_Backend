package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.RentalDTO;
import com.camerashop.entity.Rental;
import com.camerashop.entity.User;
import com.camerashop.repository.RentalRepository;
import com.camerashop.repository.UserRepository;
import com.camerashop.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin(origins = "*")
public class RentalController {

    @Autowired
    private RentalService rentalService;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> createRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {
        try {
            String assetId = body.get("assetId");
            LocalDate startDate = LocalDate.parse(body.get("startDate"));
            LocalDate endDate = LocalDate.parse(body.get("endDate"));
            String shippingAddress = body.get("shippingAddress");
            String paymentMethod = body.get("paymentMethod");
            Long shippingFee = Long.parseLong(body.getOrDefault("shippingFee", "0"));

            RentalDTO rental = rentalService.createRental(
                userDetails.getUsername(), assetId, startDate, endDate,
                shippingAddress, paymentMethod, shippingFee
            );
            return ResponseEntity.ok(ApiResponse.success(rental));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getRentalsByUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<RentalDTO> rentals = rentalService.getRentalsByUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(rentals));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getRentalById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Rental rental = rentalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Rental not found"));
            if (!rental.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }
            RentalDTO rentalDTO = rentalService.getRentalById(id);
            return ResponseEntity.ok(ApiResponse.success(rentalDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Check if an asset is available for the given date range
     */
    @GetMapping("/check-availability")
    public ResponseEntity<ApiResponse> checkAssetAvailability(
            @RequestParam String assetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            boolean available = rentalService.isAssetAvailable(assetId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(Map.of(
                    "available", available,
                    "assetId", assetId,
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString()
            )));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Calculate rental price without creating a rental
     */
    @PostMapping("/calculate-price")
    public ResponseEntity<ApiResponse> calculateRentalPrice(@RequestBody Map<String, String> body) {
        try {
            String assetId = body.get("assetId");
            LocalDate startDate = LocalDate.parse(body.get("startDate"));
            LocalDate endDate = LocalDate.parse(body.get("endDate"));

            Map<String, Object> priceInfo = rentalService.calculateRentalPrice(assetId, startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success(priceInfo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Extend a rental period
     */
    @PostMapping("/{id}/extend")
    public ResponseEntity<ApiResponse> extendRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Rental rental = rentalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Rental not found"));
            if (!rental.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }
            LocalDate newEndDate = LocalDate.parse(body.get("newEndDate"));
            RentalDTO result = rentalService.extendRental(id, newEndDate);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Process rental return
     */
    @PostMapping("/{id}/return")
    public ResponseEntity<ApiResponse> returnRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Rental rental = rentalRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Rental not found"));
            if (!rental.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }
            LocalDate returnDate = LocalDate.parse(body.get("returnDate"));
            RentalDTO result = rentalService.returnRental(id, returnDate);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
