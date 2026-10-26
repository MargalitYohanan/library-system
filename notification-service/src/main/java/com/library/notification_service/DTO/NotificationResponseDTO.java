package com.library.notification_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// תוקן: לפני כן הבקרה החזירה את ה-Entity (Notification) ישירות ללקוח.
// אצל Notification השדה "content" הוא אובייקט (Content: subject + contentNotification)
// ולא מחרוזת - זה גרם לצד ה-Frontend להדפיס "[object Object]" כי הוא ציפה למחרוזת.
// ה-DTO הזה "משטח" את הנתונים כך שהשדה contentNotification הוא מחרוזת ישירה.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {

    private Integer id;

    private String subject;

    private String contentNotification;

    private LocalDateTime sendDate;

    private boolean isRead;

    private String status;
}
