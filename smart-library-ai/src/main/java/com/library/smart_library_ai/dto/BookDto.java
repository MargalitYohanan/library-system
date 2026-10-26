package com.library.smart_library_ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class BookDto {

        // bookandloaningservice מחזיר את מזהה הספר תחת השדה "id" (UUID), לא "bookId".
        @JsonProperty("id")
        private UUID bookId;
        private String title;
        private String category;

        // תוקן: הוחלף מ-"city" (לא היה קיים במודל האמיתי) ל-ownerId - שדה אמיתי שקיים
        // ב-BookDTO של bookandloaningservice. "מיקום הספר" נגזר בשירות מ-שכונת הבעלים
        // (fetch נפרד מ-userservice), כי אין שדה מיקום ישיר על הספר עצמו.
        private UUID ownerId;

        // תוקן: לא היה קיים בכלל שדה "available" ב-DTO הזה. בלעדיו, אין דרך לסנן ספרים
        // לא-זמינים לפני שליחת הרשימה ל-AI, וזו הסיבה שההמלצות כללו ספר "לא זמין".
        private boolean available;

        public BookDto() {}

        public BookDto(UUID bookId, String title, String category, UUID ownerId, boolean available) {
            this.bookId = bookId;
            this.title = title;
            this.category = category;
            this.ownerId = ownerId;
            this.available = available;
        }

        public UUID getBookId() {
            return bookId;
        }

        public void setBookId(UUID bookId) {
            this.bookId = bookId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public UUID getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(UUID ownerId) {
            this.ownerId = ownerId;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }
    }
