package com.library.smart_library_ai.controller;

import com.library.smart_library_ai.dto.BookRecommendationDto;
import com.library.smart_library_ai.service.BookRecommendationService;
import com.library.smart_library_ai.service.clients.BookClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final BookRecommendationService service;

    public RecommendationController(BookRecommendationService service) {
        this.service = service;
    }

    // תוקן: הנתיב היה "/start" בלי {userId} בכלל, למרות שהמתודה דרשה @PathVariable userId -
    // זה היה קורס בזמן ריצה (IllegalStateException) בכל קריאה בפועל.
    @GetMapping("/start/{userId}")
    public String start(@PathVariable UUID userId) {
        return "working";
    }

    // 1. getWeeklyUpdates – מחזיר את ההמלצה השבועית לפי userId
    @GetMapping("/{userId}")
    public ResponseEntity<BookRecommendationDto> getUserRecommendations(@PathVariable UUID userId) {
        try {
            BookRecommendationDto result = service.getRecommendationFromDB(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 2. add – יצירת Recommendation בסיסי למשתמש חדש
    // תוקן: לפני כן, אם ל-service.add() כבר הייתה רשומה קיימת הוא זרק RuntimeException
    // שלא נתפס כאן בכלל - Spring היה מחזיר 500 Internal Server Error גולמי לדפדפן
    // (זה מה שנראה בקונסול: כל בקשת POST ל-/add/{userId} עבור משתמש עם רשומה קיימת קרסה).
    // כעת אנחנו תופסים את זה ומחזירים 409 Conflict עם הודעה ברורה, וכל כשל אחר מוחזר כ-502
    // (שגיאת שירות תלוי, למשל ה-AI) במקום 500 סתום.
    @PostMapping("/add/{userId}")
    public ResponseEntity<?> add(@PathVariable UUID userId) {
        try {
            boolean created = service.add(userId);
            return created
                    ? ResponseEntity.ok(true)
                    : ResponseEntity.status(502).body("יצירת ההמלצה נכשלה - שירות ה-AI לא החזיר תוצאה תקינה.");
        } catch (BookRecommendationService.RecommendationAlreadyExistsException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // 3. delete – מחיקת Recommendation של משתמש (כולל כל הדאטה)
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> delete(@PathVariable UUID userId) {
        service.delete(userId);
        return ResponseEntity.ok().build();
    }

}
