package com.camerashop.service;

import com.camerashop.dto.NotificationDTO;
import com.camerashop.entity.Notification;
import com.camerashop.entity.Order;
import com.camerashop.entity.Rental;
import com.camerashop.entity.User;
import com.camerashop.repository.NotificationRepository;
import com.camerashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new notification for a user
     */
    @Transactional
    public NotificationDTO.NotificationResponse createNotification(
            String userId,
            String title,
            String message,
            Notification.NotificationType type,
            String referenceId,
            Notification.ReferenceType referenceType,
            Boolean isActionRequired,
            String actionUrl
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID().toString())
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .isActionRequired(isActionRequired != null ? isActionRequired : false)
                .actionUrl(actionUrl)
                .build();

        // Set expiry for certain notification types
        if (type == Notification.NotificationType.PROMOTION) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(7));
        } else if (type == Notification.NotificationType.RENTAL_REMINDER) {
            notification.setExpiresAt(LocalDateTime.now().plusDays(1));
        }

        notificationRepository.save(notification);

        return toResponse(notification);
    }

    /**
     * Create notification for order status change
     */
    @Transactional
    public void notifyOrderStatusChange(Order order, String oldStatus, String newStatus) {
        String title = getOrderStatusTitle(newStatus);
        String message = getOrderStatusMessage(order, oldStatus, newStatus);

        createNotification(
                order.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.ORDER_UPDATE,
                order.getOrderId(),
                Notification.ReferenceType.ORDER,
                requiresAction(newStatus),
                null
        );
    }

    /**
     * Create notification for rental return reminder
     */
    @Transactional
    public void notifyRentalReturnReminder(Rental rental, long daysUntilReturn) {
        String title = "Rental Return Reminder";
        String message = String.format(
                "Your rental of %s is due in %d day(s). Please return it by %s to avoid late fees.",
                rental.getAsset().getModelName(),
                daysUntilReturn,
                rental.getEndDate()
        );

        createNotification(
                rental.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.RENTAL_REMINDER,
                rental.getRentalId(),
                Notification.ReferenceType.RENTAL,
                true,
                null
        );
    }

    /**
     * Create notification for rental overdue
     */
    @Transactional
    public void notifyRentalOverdue(Rental rental, long daysOverdue) {
        String title = "Rental Overdue!";
        String message = String.format(
                "Your rental of %s is %d day(s) overdue. Please return it immediately to avoid additional charges.",
                rental.getAsset().getModelName(),
                daysOverdue
        );

        createNotification(
                rental.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.RENTAL_OVERDUE,
                rental.getRentalId(),
                Notification.ReferenceType.RENTAL,
                true,
                null
        );
    }

    /**
     * Create notification for payment success
     */
    @Transactional
    public void notifyPaymentSuccess(Order order, double amount) {
        String title = "Payment Successful";
        String message = String.format(
                "Your payment of ₫%,d for order %s has been confirmed.",
                (long) amount,
                order.getOrderId()
        );

        createNotification(
                order.getUser().getUserId(),
                title,
                message,
                Notification.NotificationType.PAYMENT_SUCCESS,
                order.getOrderId(),
                Notification.ReferenceType.ORDER,
                false,
                null
        );
    }

    /**
     * Get notifications for a user with pagination
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO.NotificationResponse> getUserNotifications(
            String email,
            int page,
            int size
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications = notificationRepository.findByUserUserId(user.getUserId(), pageRequest);

        return notifications.map(this::toResponse);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO.NotificationResponse> getUnreadNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Notification> notifications = notificationRepository.findByUserUserIdAndIsReadFalse(user.getUserId());
        return notifications.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.countByUserUserIdAndIsReadFalse(user.getUserId());
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public NotificationDTO.NotificationResponse markAsRead(String notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Notification does not belong to user");
        }

        notification.markAsRead();
        notificationRepository.save(notification);

        return toResponse(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return notificationRepository.markAllAsRead(user.getUserId(), LocalDateTime.now());
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(String notificationId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (!notification.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Notification does not belong to user");
        }

        notificationRepository.delete(notification);
    }

    /**
     * Scheduled job: Send rental return reminders 2 days before due date
     */
    @Scheduled(cron = "0 0 9 * * ?") // Every day at 9 AM
    @Transactional
    public void sendRentalReminders() {
        LocalDateTime twoDaysFromNow = LocalDateTime.now().plusDays(2);

        // This would need a RentalRepository method to find rentals due in 2 days
        // For now, this is a placeholder for the scheduled job
        System.out.println("Running scheduled rental reminder job...");
    }

    /**
     * Scheduled job: Clean up old notifications
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2 AM
    @Transactional
    public void cleanupOldNotifications() {
        // Delete expired notifications
        int deletedExpired = notificationRepository.deleteExpired(LocalDateTime.now());

        // Delete read notifications older than 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deletedOld = notificationRepository.deleteOldReadNotifications(thirtyDaysAgo);

        System.out.println("Cleanup: deleted " + deletedExpired + " expired and " + deletedOld + " old read notifications");
    }

    /**
     * Create notification for a user by email (resolves email to userId)
     */
    @Transactional
    public NotificationDTO.NotificationResponse createNotificationForUser(
            String email,
            String title,
            String message,
            Notification.NotificationType type,
            String referenceId,
            Notification.ReferenceType referenceType,
            Boolean isActionRequired,
            String actionUrl
    ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return createNotification(
                user.getUserId(),
                title,
                message,
                type,
                referenceId,
                referenceType,
                isActionRequired,
                actionUrl
        );
    }

    /**
     * Get system/broadcast notifications (no auth required, limited to 10 most recent)
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO.NotificationResponse> getSystemNotifications() {
        List<Notification> notifications = notificationRepository.findByTypeInAndExpiresAtAfterOrExpiresAtIsNull(
                Arrays.asList(Notification.NotificationType.SYSTEM, Notification.NotificationType.PROMOTION),
                LocalDateTime.now(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        // Deduplicate by title to avoid showing same broadcast for every user
        Map<String, Notification> uniqueByTitle = new java.util.LinkedHashMap<>();
        for (Notification n : notifications) {
            uniqueByTitle.putIfAbsent(n.getTitle(), n);
        }
        return uniqueByTitle.values().stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Check if an overdue notification already exists for a rental
     */
    @Transactional(readOnly = true)
    public boolean hasOverdueNotification(String rentalId) {
        List<Notification> existing = notificationRepository.findByReferenceIdAndReferenceType(
                rentalId, Notification.ReferenceType.RENTAL
        );
        return existing.stream().anyMatch(n -> n.getType() == Notification.NotificationType.RENTAL_OVERDUE);
    }

    /**
     * Create a welcome notification for a new user
     */
    @Transactional
    public void createWelcomeNotification(String userId) {
        createNotification(
                userId,
                "Welcome to Camera Shop!",
                "Thank you for joining Camera Shop! Browse our collection of premium cameras, lenses, and equipment. Feel free to explore and find the perfect gear for your needs.",
                Notification.NotificationType.SYSTEM,
                null,
                null,
                false,
                null
        );
    }

    // Helper methods

    private NotificationDTO.NotificationResponse toResponse(Notification notification) {
        return NotificationDTO.NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .userId(notification.getUser().getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType() != null ? notification.getReferenceType().name() : null)
                .isRead(notification.getIsRead())
                .isActionRequired(notification.getIsActionRequired())
                .actionUrl(notification.getActionUrl())
                .deepLinkUrl(notification.getDeepLinkUrl())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    private String getOrderStatusTitle(String status) {
        switch (status) {
            case "PENDING": return "Order Placed";
            case "CONFIRMED": return "Order Confirmed";
            case "PROCESSING": return "Order Processing";
            case "SHIPPED": return "Order Shipped";
            case "DELIVERED": return "Order Delivered";
            case "CANCELLED": return "Order Cancelled";
            default: return "Order Status Update";
        }
    }

    private String getOrderStatusMessage(Order order, String oldStatus, String newStatus) {
        switch (newStatus) {
            case "PENDING":
                return String.format("Your order %s has been placed and is awaiting confirmation.", order.getOrderId());
            case "CONFIRMED":
                return String.format("Your order %s has been confirmed. We're preparing it for shipment.", order.getOrderId());
            case "PROCESSING":
                return String.format("Your order %s is being prepared for shipment.", order.getOrderId());
            case "SHIPPED":
                return String.format("Your order %s has been shipped and is on its way!", order.getOrderId());
            case "DELIVERED":
                return String.format("Your order %s has been delivered. Enjoy your purchase!", order.getOrderId());
            case "CANCELLED":
                return String.format("Your order %s has been cancelled.", order.getOrderId());
            default:
                return String.format("Order %s status updated to %s.", order.getOrderId(), newStatus);
        }
    }

    private boolean requiresAction(String status) {
        return "CANCELLED".equals(status) || "PENDING".equals(status);
    }
}
