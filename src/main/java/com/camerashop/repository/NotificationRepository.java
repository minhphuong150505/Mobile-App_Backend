package com.camerashop.repository;

import com.camerashop.entity.Notification;
import com.camerashop.entity.Notification.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    /**
     * Get notifications for a user with pagination
     */
    Page<Notification> findByUserUserId(String userId, Pageable pageable);

    /**
     * Get unread notifications for a user
     */
    List<Notification> findByUserUserIdAndIsReadFalse(String userId);

    /**
     * Count unread notifications for a user
     */
    long countByUserUserIdAndIsReadFalse(String userId);

    /**
     * Get notifications by reference (order or rental)
     */
    List<Notification> findByReferenceIdAndReferenceType(String referenceId, Notification.ReferenceType referenceType);

    /**
     * Get notifications by type for a user
     */
    Page<Notification> findByUserUserIdAndType(String userId, NotificationType type, Pageable pageable);

    /**
     * Get action required notifications for a user
     */
    List<Notification> findByUserUserIdAndIsActionRequiredTrue(String userId);

    /**
     * Mark all notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.user.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") String userId, @Param("readAt") LocalDateTime readAt);

    /**
     * Delete expired notifications
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);

    /**
     * Delete old read notifications (older than 30 days)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Check if a notification already exists for a user + reference + type (for deduplication)
     */
    boolean existsByUserUserIdAndReferenceIdAndReferenceTypeAndType(
            String userId, String referenceId, Notification.ReferenceType referenceType, NotificationType type
    );

    /**
     * Get system/promotion notifications that haven't expired (limited)
     */
    @Query("SELECT n FROM Notification n WHERE n.type IN :types AND (n.expiresAt IS NULL OR n.expiresAt > :now) ORDER BY n.createdAt DESC")
    List<Notification> findByTypeInAndExpiresAtAfterOrExpiresAtIsNull(
            @Param("types") List<NotificationType> types,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );
}
