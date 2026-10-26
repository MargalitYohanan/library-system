package com.library.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * מבנה זהה ל-UserNotificationDTO ב-notification-service.
 * חייב להישאר תואם (שדות + שמות) לשני הצדדים כי זה גוף בקשת JSON בין שירותים.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
}
