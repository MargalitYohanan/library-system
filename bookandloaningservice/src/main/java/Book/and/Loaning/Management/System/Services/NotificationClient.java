package Book.and.Loaning.Management.System.Services;

import Book.and.Loaning.Management.System.DTO.UserToUserNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${service.notifications.url}")
    private String notificationServiceUrl;

    public void sendLoanRequestAlert(UserToUserNotificationDTO dto) {
        callMediationEndpoint("/api/notifications/mediation/loan-request", dto);
    }

    public void sendLoanApproval(UserToUserNotificationDTO dto) {
        callMediationEndpoint("/api/notifications/mediation/loan-approval", dto);
    }

    public void sendLoanExtensionRequest(UserToUserNotificationDTO dto) {
        callMediationEndpoint("/api/notifications/mediation/loan-extension", dto);
    }

    public void approveLoanExtension(UserToUserNotificationDTO dto) {
        callMediationEndpoint("/api/notifications/mediation/loan-extension/approve", dto);
    }

    private void callMediationEndpoint(String path, UserToUserNotificationDTO dto) {
        try {
            restTemplate.postForEntity(notificationServiceUrl + path, dto, Void.class);
        } catch (RestClientException e) {
            // לא זורקים הלאה בכוונה - אם ההתראה נכשלת, זה לא אמור למנוע השאלת ספר
            log.warn("Failed to send notification to {}: {}", path, e.getMessage());
        }
    }
}
