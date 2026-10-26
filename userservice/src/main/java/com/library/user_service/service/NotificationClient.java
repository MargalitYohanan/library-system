package com.library.user_service.service;

import com.library.user_service.dto.UserNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * קליינט לקריאה לשירות ה-notification-service.
 * כשל בשליחת ההתראה לא אמור להפיל את תהליך ההרשמה עצמו — לכן try/catch.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${service.notifications.url}")
    private String notificationServiceUrl;

    public void sendWelcomeEmail(UserNotificationDto dto) {
        try {
            restTemplate.postForEntity(
                    notificationServiceUrl + "/api/notifications/system/welcome", dto, Void.class);
        } catch (RestClientException e) {
            log.warn("Failed to send welcome notification for user {}: {}", dto.getId(), e.getMessage());
        }
    }
}
