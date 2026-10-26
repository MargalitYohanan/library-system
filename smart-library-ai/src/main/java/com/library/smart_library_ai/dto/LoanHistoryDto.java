package com.library.smart_library_ai.dto;

import java.util.UUID;

public class LoanHistoryDto {
    private UUID bookId;
    private String bookCategory; // (בשביל לבדוק מה הוא אוהב)

    // תוקן: הוחלף מ-"branchCity" (לא היה קיים במודל האמיתי) ל-ownerId - מגיע עכשיו
    // מ-bookandloaningservice (ה-endpoint /api/loans/history/{id} החדש). "השכונה" של
    // ההשאלה נגזרת בשירות מ-שכונת הבעלים של הספר שהושאל.
    private UUID ownerId;

    public LoanHistoryDto() {}

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }
    public String getBookCategory() { return bookCategory; }
    public void setBookCategory(String bookCategory) { this.bookCategory = bookCategory; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
}
