package com.camerashop.controller;

import com.camerashop.dto.ApiResponse;
import com.camerashop.dto.CartItemDTO;
import com.camerashop.entity.CartItem;
import com.camerashop.entity.User;
import com.camerashop.repository.CartItemRepository;
import com.camerashop.repository.UserRepository;
import com.camerashop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse> getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        // Get user email, then find user by email to get userId
        // For now, we'll use a simpler approach - the email is the identifier
        List<CartItemDTO> cartItems = cartService.getCartItems(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(cartItems));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> body) {
        try {
            String itemId = (String) body.get("itemId");
            String type = (String) body.get("type");
            Integer quantity = body.get("quantity") != null ?
                ((Number) body.get("quantity")).intValue() : 1;

            CartItemDTO cartItem = cartService.addToCart(userDetails.getUsername(), itemId, type, quantity);
            return ResponseEntity.ok(ApiResponse.success(cartItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
            if (!cartItem.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }
            cartService.removeFromCart(id);
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/{id}/quantity")
    public ResponseEntity<ApiResponse> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @RequestBody Map<String, Integer> body) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            CartItem cartItem = cartItemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));
            if (!cartItem.getUser().getUserId().equals(user.getUserId())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Not authorized"));
            }
            CartItemDTO updated = cartService.updateQuantity(id, body.get("quantity"));
            return ResponseEntity.ok(ApiResponse.success(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
