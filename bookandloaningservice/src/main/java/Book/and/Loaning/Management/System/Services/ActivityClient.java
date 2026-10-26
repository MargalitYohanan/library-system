package Book.and.Loaning.Management.System.Services;

import Book.and.Loaning.Management.System.DTO.ActivityRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityClient {

    private final RestTemplate restTemplate;

    @Value("${service.users.url}")
    private String userServiceUrl;

    public void logBookAdded(UUID ownerId, String bookTitle) {
        logActivity(ownerId, "BOOK_ADDED", "הוספת ספר חדש: " + bookTitle);
    }

    public void logBookUpdated(UUID ownerId, String bookTitle) {
        logActivity(ownerId, "BOOK_UPDATED", "עדכון פרטי ספר: " + bookTitle);
    }

    public void logBookRemoved(UUID ownerId, String bookTitle) {
        logActivity(ownerId, "BOOK_REMOVED", "מחיקת ספר: " + bookTitle);
    }

    public void logLoanRequestCreated(UUID borrowerId, String bookTitle) {
        logActivity(borrowerId, "LOAN_REQUEST_CREATED", "בקשת השאלה חדשה לספר: " + bookTitle);
    }

    public void logLoanRequestApproved(UUID ownerId, String bookTitle) {
        logActivity(ownerId, "LOAN_REQUEST_APPROVED", "אישור בקשת השאלה לספר: " + bookTitle);
    }

    public void logLoanRequestRejected(UUID ownerId, String bookTitle) {
        logActivity(ownerId, "LOAN_REQUEST_REJECTED", "דחיית בקשת השאלה לספר: " + bookTitle);
    }

    public void logLoanReturned(UUID borrowerId, String bookTitle) {
        logActivity(borrowerId, "LOAN_BOOK_RETURNED", "החזרת ספר: " + bookTitle);
    }

    private void logActivity(UUID userId, String actionType, String description) {
        try {
            ActivityRequestDTO dto = new ActivityRequestDTO(userId, actionType, description);
            restTemplate.postForEntity(userServiceUrl + "/api/activities", dto, Void.class);
        } catch (RestClientException e) {
            log.warn("Failed to log activity '{}' for user {}: {}", actionType, userId, e.getMessage());
        }
    }
}
