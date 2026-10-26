package com.library.smart_library_ai.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookRecommendationDto {
    private String reasonForChoice;      // הנימוק הכללי שיוצג למעלה במסך
    private LocalDateTime generatedDate;  // תאריך הרענון השבועי
    private List<UUID> bookIds;        // רשימת 5 מזהי הספרים שה-React יפצל ויחפש

    // קונסטרקטור ריק
    public BookRecommendationDto() {}

    // קונסטרקטור מלא
    public BookRecommendationDto(String reasonForChoice, LocalDateTime generatedDate, List<UUID> bookIds) {
        this.reasonForChoice = reasonForChoice;
        this.generatedDate = generatedDate;
        this.bookIds = bookIds;
    }

    // Getters & Setters
    public String getReasonForChoice() { return reasonForChoice; }
    public void setReasonForChoice(String reasonForChoice) { this.reasonForChoice = reasonForChoice; }

    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }

    public List<UUID> getBookIds() { return bookIds; }
    public void setBookIds(List<UUID> bookIds) { this.bookIds = bookIds; }
    }
