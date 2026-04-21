package com.camerashop.repository;

import com.camerashop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEntityIdAndType(Long entityId, String type);
    List<Review> findByUserUserId(String userId);
}
