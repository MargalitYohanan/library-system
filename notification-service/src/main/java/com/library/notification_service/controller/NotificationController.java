package com.library.notification_service.controller;



import com.library.notification_service.DTO.NotificationResponseDTO;
import com.library.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // תוקן: (1) הבקרה החזירה את ה-Entity הגולמי במקום DTO שטוח - זה גרם ל-
    // "[object Object]" בצד הלקוח כי content הוא אובייקט ולא מחרוזת.
    // (2) נוסף @RequestParam עבור "size" שה-Frontend שולח בפועל אך היה מתעלם ממנו.
    @GetMapping("/user/{userId}")
    public Page<NotificationResponseDTO> getUserNotifications(
            @PathVariable("userId") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return notificationService.getNotifications(userId, page, size);
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(@PathVariable("userId") UUID userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }


    // איפוס וסימון כל ההודעות כנקראו לפי UUID
    @PutMapping("/user/{userId}/read")
    public ResponseEntity<Void> markAllNotificationsAsRead(@PathVariable("userId") UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

}