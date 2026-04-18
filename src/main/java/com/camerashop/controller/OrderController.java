package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.OrderDTO;
import com.camerashop.entity.Order;
import com.camerashop.entity.User;
import com.camerashop.repository.OrderRepository;
import com.camerashop.repository.UserRepository;
import com.camerashop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            String shippingAddress = (String) body.get("shippingAddress");
            String paymentMethod = (String) body.getOrDefault("paymentMethod", "COD");
            Long shippingFee = body.get("shippingFee") != null ? ((Number) body.get("shippingFee")).longValue() : null;
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

            OrderDTO order = orderService.createOrder(userDetails.getUsername(), shippingAddress, paymentMethod, shippingFee, items);
            return ResponseEntity.ok(ApiResponse.success(order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getOrdersByUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<OrderDTO> orders = orderService.getOrdersByUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }
            OrderDTO orderDTO = orderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.success(orderDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update order status (Admin only)
     * PATCH /api/orders/{id}/status
     */
    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Status is required"));
            }

            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);
            OrderDTO order = orderService.updateOrderStatus(orderId, newStatus);

            return ResponseEntity.ok(ApiResponse.success(order));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid status: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update order status: " + e.getMessage()));
        }
    }
}
