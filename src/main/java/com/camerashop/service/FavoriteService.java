package com.camerashop.service;

import com.camerashop.dto.FavoriteDTO;
import com.camerashop.entity.*;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

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

    public List<FavoriteDTO> getFavorites(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Favorite> favorites = favoriteRepository.findByUserId(user.getUserId());
        return favorites.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public FavoriteDTO toggleFavorite(String email, String itemId, String type) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getUserId();

        if ("PRODUCT".equalsIgnoreCase(type)) {
            if (favoriteRepository.existsByUserIdAndProductId(userId, itemId)) {
                favoriteRepository.deleteByUserIdAndProductId(userId, itemId);
                return null; // Removed
            } else {
                Product product = productRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Product not found"));

                Favorite favorite = Favorite.builder()
                        .user(user)
                        .product(product)
                        .type(Favorite.FavoriteType.PRODUCT)
                        .build();
                favoriteRepository.save(favorite);
                return toDTO(favorite);
            }
        } else {
            if (favoriteRepository.existsByUserIdAndAssetId(userId, itemId)) {
                favoriteRepository.deleteByUserIdAndAssetId(userId, itemId);
                return null; // Removed
            } else {
                Asset asset = assetRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Asset not found"));

                Favorite favorite = Favorite.builder()
                        .user(user)
                        .asset(asset)
                        .type(Favorite.FavoriteType.ASSET)
                        .build();
                favoriteRepository.save(favorite);
                return toDTO(favorite);
            }
        }
    }

    public boolean isFavorite(String email, String itemId, String type) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String userId = user.getUserId();
        if ("PRODUCT".equalsIgnoreCase(type)) {
            return favoriteRepository.existsByUserIdAndProductId(userId, itemId);
        } else {
            return favoriteRepository.existsByUserIdAndAssetId(userId, itemId);
        }
    }

    private FavoriteDTO toDTO(Favorite favorite) {
        FavoriteDTO dto = FavoriteDTO.builder()
                .favoriteId(favorite.getFavoriteId())
                .type(favorite.getType().name())
                .build();

        if (favorite.getProduct() != null) {
            dto.setProductId(favorite.getProduct().getProductId());
            dto.setProductName(favorite.getProduct().getProductName());
            dto.setPrice(favorite.getProduct().getPrice());

            ProductImage primaryImage = productImageRepository.findByProductIdAndIsPrimaryTrue(favorite.getProduct().getProductId());
            if (primaryImage != null) {
                dto.setPrimaryImageUrl(primaryImage.getUrl());
            }
        } else if (favorite.getAsset() != null) {
            dto.setAssetId(favorite.getAsset().getAssetId());
            dto.setAssetName(favorite.getAsset().getModelName());
            dto.setPrice(favorite.getAsset().getDailyRate());

            AssetImage primaryImage = assetImageRepository.findByAssetIdAndIsPrimaryTrue(favorite.getAsset().getAssetId());
            if (primaryImage != null) {
                dto.setPrimaryImageUrl(primaryImage.getUrl());
            }
        }

        return dto;
    }
}
