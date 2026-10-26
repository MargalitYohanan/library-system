package com.library.smart_library_ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class UserDto {

    // userservice מחזיר את המזהה תחת השדה "id" (UUID), לא "userId".
    @JsonProperty("id")
    private UUID userId;

    // תוקן: הוחלף מ-"city" (לא היה קיים בשום מקום אמיתי במודל) ל-neighborhoodName -
    // שדה אמיתי שקיים ב-UserDto של userservice, באותו שם JSON בדיוק, לכן לא צריך מיפוי מיוחד.
    private String neighborhoodName;

    private List<LoanHistoryDto> loanHistoryDto;

    public UserDto() {}

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getNeighborhoodName() { return neighborhoodName; }
    public void setNeighborhoodName(String neighborhoodName) { this.neighborhoodName = neighborhoodName; }

    public List<LoanHistoryDto> getLoanHistoryDto() {
        return loanHistoryDto;
    }

    public void setLoanHistoryDto(List<LoanHistoryDto> loanHistoryDto) {
        this.loanHistoryDto = loanHistoryDto;
    }
}
