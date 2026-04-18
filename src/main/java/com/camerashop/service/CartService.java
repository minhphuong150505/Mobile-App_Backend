package com.camerashop.service;

import com.camerashop.dto.CartItemDTO;
import com.camerashop.entity.*;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private AssetImageRepository assetImageRepository;

    public List<CartItemDTO> getCartItems(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getUserId());
        return cartItems.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CartItemDTO addToCart(String email, String itemId, String type, Integer quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setQuantity(quantity);
        cartItem.setType(CartItem.CartItemType.valueOf(type.toUpperCase()));

        if ("PRODUCT".equalsIgnoreCase(type)) {
            Product product = productRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            cartItem.setProduct(product);
        } else {
            Asset asset = assetRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Asset not found"));
            cartItem.setAsset(asset);
        }

        cartItemRepository.save(cartItem);
        return toDTO(cartItem);
    }

    public void removeFromCart(String cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public void clearCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        cartItemRepository.deleteByUserId(user.getUserId());
    }

    public CartItemDTO updateQuantity(String cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return toDTO(cartItem);
    }

    private CartItemDTO toDTO(CartItem cartItem) {
        CartItemDTO dto = CartItemDTO.builder()
                .cartItemId(cartItem.getCartItemId())
                .quantity(cartItem.getQuantity())
                .type(cartItem.getType().name())
                .build();

        if (cartItem.getProduct() != null) {
            dto.setProductId(cartItem.getProduct().getProductId());
            dto.setProductName(cartItem.getProduct().getProductName());
            dto.setPrice(cartItem.getProduct().getPrice());

            ProductImage primaryImage = productImageRepository.findByProductIdAndIsPrimaryTrue(cartItem.getProduct().getProductId());
            if (primaryImage != null) {
                dto.setPrimaryImageUrl(primaryImage.getUrl());
            }
        } else if (cartItem.getAsset() != null) {
            dto.setAssetId(cartItem.getAsset().getAssetId());
            dto.setAssetName(cartItem.getAsset().getModelName());
            dto.setPrice(cartItem.getAsset().getDailyRate());

            AssetImage primaryImage = assetImageRepository.findByAssetIdAndIsPrimaryTrue(cartItem.getAsset().getAssetId());
            if (primaryImage != null) {
                dto.setPrimaryImageUrl(primaryImage.getUrl());
            }
        }

        return dto;
    }
}
