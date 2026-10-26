package com.library.notification_service.repository;

import com.library.notification_service.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByStatus(String status);

    /**
     * שליפת הודעות שנכשלו, שלא עברו את מכסת הניסיונות (retryCount),
     * ושזמן השליחה האחרון שלהן היה בטווח ה-24 שעות האחרונות.
     */
    List<Notification> findByStatusAndRetryCountLessThanAndSendDateAfter(
            String status,
            Integer retryCount,
            LocalDateTime sendDate
    );
    Page<Notification> findByToUserUserIdOrderBySendDateDesc(
            UUID UserId,
            Pageable pageable
    );

    // תוקן: toUser הוא קשר @ManyToOne ל-UserNotification, שה-@Id שלו הוא Integer פנימי -
    // ה-UUID החיצוני יושב בשדה userId בתוך UserNotification. לכן חובה לנווט דרך toUser.userId,
    // ולא להשוות ישירות את toUser (האובייקט/הקשר) מול UUID.
    long countByToUser_UserIdAndIsReadFalse(UUID userId);

    // עדכון גורף ל-'true' עבור משתמש ספציפי לפי ה-UUID שלו
    // תוקן: n.toUser.userId במקום n.toUser (מאותה סיבה כמו למעלה)
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.toUser.userId = :userId AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") UUID userId);
}
