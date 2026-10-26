package com.library.smart_library_ai.service.clients;

import com.library.smart_library_ai.dto.UserDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.UUID;

@Service
public class UserClient {
    private final WebClient webClient;

    public UserClient(WebClient.Builder builder, @Value("${service.users.url}") String url) {
        this.webClient = builder.baseUrl(url).build();
    }

    // תוקן: userservice לא חשף בכלל endpoint כללי ל"כל המשתמשים" קודם.
    // /api/users/internal הוא endpoint חדש, פנימי בלבד (server-to-server, בלי JWT) - ראו userservice.
    public List<UserDto> getAllUsers() {
        return webClient.get()
                .uri("/api/users/internal")
                .retrieve()
                .bodyToFlux(UserDto.class)
                .collectList()
                .block();
    }

    // תוקן: userId עכשיו UUID (לא int), והנתיב תוקן ל-/api/users/internal/{id}
    // (endpoint פנימי חדש - ה-/api/users/{id} הרגיל מעולם לא היה קיים ב-UserController האמיתי).
    public UserDto getUserById(UUID userId) {
        try {
            return webClient.get()
                    .uri("/api/users/internal/{id}", userId)
                    .retrieve()
                    // אם השרת מחזיר שגיאה (כמו 404), זה יזרוק Exception
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (Exception e) {
            // טיפול במידה והשירות השני לא זמין או שהמשתמש לא נמצא
            throw new RuntimeException("Failed to fetch user from User Service: " + e.getMessage());
        }
    }
}
