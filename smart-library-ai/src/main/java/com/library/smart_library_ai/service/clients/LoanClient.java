package com.library.smart_library_ai.service.clients;

import com.library.smart_library_ai.dto.LoanHistoryDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.UUID;

@Service
public class LoanClient {
    private final WebClient webClient;

    public LoanClient(WebClient.Builder builder, @Value("${service.loans.url}") String url) {
        this.webClient = builder.baseUrl(url).build();
    }

    // תוקן: userId עכשיו UUID, והנתיב תוקן ל-/api/loans/history/{id} -
    // endpoint חדש שנוסף ב-LoanController (הישן, /api/loans/user/{id}, מעולם לא היה קיים).
    public List<LoanHistoryDto> getLoanHistory(UUID userId) {
        return webClient.get()
                .uri("/api/loans/history/{id}", userId)
                .retrieve()
                .bodyToFlux(LoanHistoryDto.class)
                .collectList()
                .block();
    }
}
