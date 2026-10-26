package com.library.smart_library_ai.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "BookRecommendations")
public class BookRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recommendationId;

       @Column(columnDefinition = "uuid")
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String reasonForChoice;

    private LocalDateTime generatedDate;

      @ElementCollection
    @CollectionTable(
            name = "BookRecommendationItems",
            joinColumns = @JoinColumn(name = "recommendationId")
    )
    @Column(name = "bookId", columnDefinition = "uuid")
    private List<UUID> bookIds;

    public BookRecommendation() {}

    public int getRecommendationId() {
        return recommendationId;
    }

    public void setRecommendationId(int recommendationId) {
        this.recommendationId = recommendationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getReasonForChoice() {
        return reasonForChoice;
    }

    public void setReasonForChoice(String reasonForChoice) {
        this.reasonForChoice = reasonForChoice;
    }

    public LocalDateTime getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(LocalDateTime generatedDate) {
        this.generatedDate = generatedDate;
    }

    public List<UUID> getBookIds() {
        return bookIds;
    }

    public void setBookIds(List<UUID> bookIds) {
        this.bookIds = bookIds;
    }
}
