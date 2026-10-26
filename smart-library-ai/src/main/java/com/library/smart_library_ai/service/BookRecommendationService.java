package com.library.smart_library_ai.service;

import com.library.smart_library_ai.dto.BookDto;
import com.library.smart_library_ai.dto.BookRecommendationDto;
import com.library.smart_library_ai.dto.LoanHistoryDto;
import com.library.smart_library_ai.dto.UserDto;
import com.library.smart_library_ai.entity.BookRecommendation;
import com.library.smart_library_ai.repository.BookRecommendationRepository;
import com.library.smart_library_ai.service.clients.GroqAiService;
import com.library.smart_library_ai.service.clients.LoanClient;
import com.library.smart_library_ai.service.clients.UserClient;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class BookRecommendationService {
    private final GroqAiService aiService;
    private final UserClient userClient;
    private final LoanClient loanClient;
    private final BookRecommendationRepository repository;
    private final BookCacheService bookCacheService;
    public BookRecommendationService(BookRecommendationRepository repository,BookCacheService bookCacheService,UserClient userClient,LoanClient loanClient, GroqAiService aiService) {
        this.userClient = userClient;
        this.loanClient = loanClient;
        this.repository = repository;
        this.aiService=aiService;
        this.bookCacheService=bookCacheService;
    }


    // ----------------------------1-----------------------------------
    @Scheduled(cron = "0 0 3 * * SUN")
    public void processWeeklyRecommendations() {
        // 1. משכנו את כל המשתמשים
        List<UserDto> allUsers = userClient.getAllUsers();
        List<BookDto> books = bookCacheService.getBooks();
        // שכונה של כל משתמש (=בעל ספר) לפי מזהה - נבנה פעם אחת ומשמש גם לפתרון "מיקום ספר" (שכונת הבעלים)
        Map<UUID, String> neighborhoodByUserId = buildNeighborhoodMap(allUsers);
        for (UserDto user : allUsers) {
            try {
                // 2. ה-Enrichment: שליפת ההיסטוריה הספציפית לכל משתמש והוספה ל-DTO
                List<LoanHistoryDto> history = loanClient.getLoanHistory(user.getUserId());
                user.setLoanHistoryDto(history); // כעת המידע נמצא בתוך האובייקט!

                // 3. ממשיכים לעיבוד כרגיל
                getRecommendationsForUser(user, neighborhoodByUserId);
            } catch (Exception e) {
                System.err.println("Error for user " + user.getUserId() + ": " + e.getMessage());
            }
        }
    }

    // בונה מיפוי userId -> שם שכונה, מתוך רשימת כל המשתמשים (מגיע מ-userservice).
    // משמש גם לשכונת המשתמש עצמו וגם ל"שכונת הספר" (=שכונת הבעלים שלו).
    private Map<UUID, String> buildNeighborhoodMap(List<UserDto> allUsers) {
        Map<UUID, String> map = new HashMap<>();
        for (UserDto u : allUsers) {
            map.put(u.getUserId(), u.getNeighborhoodName());
        }
        return map;
    }

    //----------------------------2----------------------------------------------
    //פונקציה 2- מקבלת את המשתמש והסטורית ההשאלות שלו ומחזירה את הנוסחא התמאימה
    // תוקן: "עיר" הוחלף ב"שכונה" - user.getNeighborhoodName() מגיע ישירות מ-userservice.
    // "שכונת הספר" נגזרת דרך neighborhoodByUserId.get(loan.getOwnerId()) - שכונת בעל הספר,
    // כי אין שדה מיקום ישיר על הספר עצמו.
    public String CheckUserStrategy(UserDto user, Map<UUID, String> neighborhoodByUserId)
    {
        List<LoanHistoryDto> history = loanClient.getLoanHistory(user.getUserId());
        String userNeighborhood = user.getNeighborhoodName();

        if (history == null || history.isEmpty()) {
            return "המשתמש גר בשכונה: " + userNeighborhood + ". " +
                    "מאחר ואין היסטוריית השאלות, השתמש בחישוב הבא: " +
                    "50% גיל + 30% פופולריות + 20% קרבה = Score. " +
                    "העדף ספרים משכונת: " + userNeighborhood;
        }
        long booksFromUserNeighborhood = history.stream()
                .filter(loan -> userNeighborhood != null
                        && userNeighborhood.equals(neighborhoodByUserId.get(loan.getOwnerId())))
                .count();
        double ratio = (double) booksFromUserNeighborhood / history.size();
        if (ratio > 0.6)
        {
            // מעל 60% מהספרים מהשכונה שלו - משקל גבוה לקרבה
            return "המשתמש גר בשכונה: " + userNeighborhood + ". " +
                    "מאחר ומעל 60% מהספרים שהשאיל הם משכונתו, השתמש בחישוב הבא: " +
                    "20% קטגוריה + 20% גיל + 20% פופולריות + 40% קרבה = Score. " +
                    "העדף ספרים משכונת: " + userNeighborhood;
        }
        else {
            // פחות מ-60% מהספרים מהשכונה שלו - משקל נמוך לקרבה
            return "המשתמש גר בשכונה: " + userNeighborhood + ". " +
                    "מאחר ופחות מ-60% מהספרים שהשאיל הם משכונתו, השתמש בחישוב הבא: " +
                    "50% קטגוריה + 20% גיל + 20% פופולריות + 10% קרבה = Score. " +
                    "העדף ספרים משכונת: " + userNeighborhood;
        }
    }
    public BookRecommendationDto getRecommendationFromDB(UUID userId) {
        BookRecommendation entity = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No recommendations found for user: " + userId));

        // המרה מ-Entity ל-DTO
        return new BookRecommendationDto(
                entity.getReasonForChoice(),
                entity.getGeneratedDate(),
                entity.getBookIds()
        );
    }
    //--------------------------------------3-------------------------------
    //פונקציה 3-בונה את הפרומפט ל-AI מקבלת את הנוסחא שנבחרה
    // תוקן: מציג את שכונת הבעלים של כל ספר (במקום "עיר" שלא היה קיים)
    // תוקן: מסננים כאן את הספרים שלא בעל המלצה מסודרת:
    // 1) ספרים שבבעלות המשתמש עצמו - אין טעם להמליץ למישהו על ספר שכבר שלו.
    // 2) ספרים שלא זמינים כרגע (available=false) - אין טעם להמליץ על ספר שאי אפשר לשאול כרגע.
    // גם מחזקים בהוראות הפורמט שלא לכלול מזהה כפול באותה רשימה, כדי למנוע כפילויות בתשובת ה-AI.
    public String BuildAiPrompt(String strategyPromptBlock, Map<UUID, String> neighborhoodByUserId, UUID requestingUserId) {

        List<BookDto> books = bookCacheService.getBooks().stream()
                .filter(BookDto::isAvailable)
                .filter(book -> !requestingUserId.equals(book.getOwnerId())) // בטוח גם אם ownerId=null
                .toList();
        StringBuilder prompt = new StringBuilder();

        prompt.append(" חלק 1: אסטרטגיית המשקלים והנוסחה המוגדרת \n");
        prompt.append(strategyPromptBlock);
        prompt.append("\n\n");

        prompt.append(" חלק 2: רשימת הספרים להשאלה כרגע בספריה \n");
        for (BookDto book : books) {
            String bookNeighborhood = neighborhoodByUserId.get(book.getOwnerId());
            prompt.append("- מזהה: ").append(book.getBookId())
                    .append(", שם: ").append(book.getTitle())
                    .append(", קטגוריה: ").append(book.getCategory())
                    .append(", שכונה: ").append(bookNeighborhood != null ? bookNeighborhood : "לא ידוע")
                    .append("\n");
        }
        // חלק 3 - הוראות הפורמט (JSON קשיח)
        prompt.append("\n=== חלק 3: משימה והוראות פורמט ===\n");
        prompt.append("בחר בדיוק 5 ספרים מהרשימה לעיל. הבחירה חייבת להתבצע בקפידה על פי אסטרטגיית המשקלים והנוסחה הנתונה תחת הכותרת 'חלק 1'.\n");
        prompt.append("חשוב: כל מזהה ספר (bookId) חייב להופיע פעם אחת בלבד במערך - אין להחזיר מזהה כפול.\n");
        prompt.append("החזר את התשובה בפורמט JSON תקני בלבד המורכב מאובייקט יחיד עם השדות הבאים:\n");
        prompt.append("bookIds - מערך של מזהי הספרים שנבחרו, בדיוק כפי שהם מופיעים למעלה תחת 'מזהה' (מחרוזות UUID, לא מספרים)\n");
        prompt.append("reasonForChoice - נימוק קצר וכולל בעברית מדוע נבחרו ספרים אלו\n");
        prompt.append("דגש לנימוק: עליך להרחיב ולהסביר בפירוט מדוע נבחרו ספרים אלו, ולציין במפורש האם הבחירה התבססה בעיקר על קרבת שכונת המשתמש, על הקטגוריות המועדפות עליו, או על שילוב של השניים בהתאם לאחוזים שבנוסחה.\n");
        prompt.append("אל תכתוב שום טקסט חופשי, הסברים או הקדמות לפני או אחרי ה-JSON. החזר רק את ה-JSON עצמו.\n\n");

        prompt.append("מבנה ה-JSON הנדרש (הדוגמה להמחשת הפורמט בלבד, את המזהים האמיתיים יש לקחת מרשימת הספרים למעלה):\n");
        prompt.append("{\n");
        prompt.append("  \"bookIds\": [\"11111111-1111-1111-1111-111111111111\", \"22222222-2222-2222-2222-222222222222\"],\n");
        prompt.append("  \"reasonForChoice\": \"נימוק מורחב ומפורט בעברית\"\n");
        prompt.append("}\n");
        return prompt.toString();
    }
    //--------------------------------------4-------------------------------

    public BookRecommendationDto executeAiRequest(String prompt) {

        if (prompt == null || prompt.trim().isEmpty()) {
            System.err.println("Error: Provided AI JSON string is empty.");
            return new com.library.smart_library_ai.dto.BookRecommendationDto();
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // תוקן: רשת ביטחון נוספת מעבר ל-response_format=json_object שהוגדר ב-GroqAiService.
            // אם בכל זאת יגיע טקסט חופשי סביב ה-JSON (הקדמה/סיכום), נחלץ רק את מה שבין
            // הסוגריים המסולסלים החיצוניים { ... } לפני הפענוח, במקום לקרוס על כל התוכן כולו.
            String jsonOnly = extractJsonObject(prompt);

            // 1. פירוק ה-JSON שמילא את השדות שהגיעו מה-AI
            BookRecommendationDto parsedDto = objectMapper.readValue(
                    jsonOnly,
                    BookRecommendationDto.class
            );

            parsedDto.setGeneratedDate(LocalDateTime.now());

            System.out.println("Successfully parsed AI JSON and enriched with current date.");
            return parsedDto;

        } catch (Exception e) {
            System.err.println("Failed to parse AI JSON string: " + e.getMessage());
            e.printStackTrace();
            return new BookRecommendationDto();
        }
    }

    // מחלץ את תת-המחרוזת שבין ה-'{' הראשון ל-'}' האחרון, כדי להיפטר מטקסט חופשי
    // שמודל AI עלול להוסיף לפני/אחרי ה-JSON, למרות ההוראה המפורשת להימנע מכך.
    private String extractJsonObject(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start == -1 || end == -1 || end < start) {
            return raw; // אין סוגריים מסולסלים בכלל - נחזיר כמו שהוא ונתן ל-parser להיכשל בבירור
        }
        return raw.substring(start, end + 1);
    }
    //--------------------------------------5-------------------------------

    @Transactional
    public boolean saveRecommendations(UUID userId, BookRecommendationDto aiResponse) {
        // הגנה ראשונית - אם האובייקט שהתקבל פגום או ריק מרשימת ספרים
        if (aiResponse == null || aiResponse.getBookIds() == null || aiResponse.getBookIds().isEmpty()) {
            System.err.println("Error: Cannot save recommendations because AI response or book list is empty.");
            return false;
        }

        try {
            // אופציונלי: ניקוי המלצות שבועיות קודמות של המשתמש כדי למנוע כפילויות בטבלה
            repository.deleteByUserId(userId);

            // 1. יצירת אובייקט ה-Entity שלכן
            BookRecommendation entity = new BookRecommendation();

            // 2. מיפוי והזרקת הנתונים מתוך הפרמטרים וה-DTO
            entity.setUserId(userId);
            entity.setReasonForChoice(aiResponse.getReasonForChoice());
            // תוקן: רשת ביטחון - גם אם ה-AI (למרות ההוראה המפורשת בפרומפט) יחזיר את אותו
            // מזהה ספר יותר מפעם אחת, מסננים כפילויות כאן לפני השמירה, כדי שלא יוצג אותו
            // ספר פעמיים בכרטיסי ההמלצה.
            entity.setBookIds(aiResponse.getBookIds().stream().distinct().toList());
            entity.setGeneratedDate(aiResponse.getGeneratedDate()); // התאריך שכבר מולא בפונקציה 4

            // 3. שמירה פיזית בבסיס הנתונים (מפעיל אוטומטית גם את שמירת ה-ElementCollection)
            repository.save(entity);

            System.out.println("Successfully saved weekly recommendations to DB for user: " + userId);
            return true;

        } catch (Exception e) {
            System.err.println("Failed to save recommendations to the database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    //--------------------------------------6-------------------------------
    public UserDto GetUserById(UUID userId) {
        UserDto user = userClient.getUserById(userId);
        return user;
    }

    public void getRecommendationsForUser(UserDto user, Map<UUID, String> neighborhoodByUserId) {
        // שליחה לפונקציה 2
        String strategy = CheckUserStrategy(user, neighborhoodByUserId);
        // שליחה לפונקציה 3
        String prompt = BuildAiPrompt(strategy, neighborhoodByUserId, user.getUserId());
        // שליחה לפונקציה 5
        String response = aiService.generateAsync(prompt);
        // שליחה לפונקציה 6
        BookRecommendationDto aiJsonExecution = executeAiRequest(response);

        boolean isSaved = saveRecommendations(user.getUserId(), aiJsonExecution);

        if (!isSaved) {
            System.err.println("Failed to save recommendations to database for user: " + user.getUserId());
            throw new RuntimeException("Database save failed");
        }
    }




    // ---------------------------------------------------------

    // ADD – יצירת Recommendation בסיסי למשתמש חדש
    // ---------------------------------------------------------
    // תוקן: שני באגים קריטיים כאן:
    // 1) הבדיקה "כבר קיים" זרקה RuntimeException *מחוץ* ל-try/catch, וה-Controller לא תפס אותה -
    //    זה גרם ל-500 Internal Server Error גולמי בכל ניסיון ליצור המלצה למשתמש שכבר יש לו רשומה
    //    (כולל רשומה ריקה/כושלת - ראו סעיף 2), במקום תגובה ברורה כמו 409 Conflict.
    // 2) הרשומה הריקה (entity) נשמרה ל-DB *לפני* שקריאת ה-AI הצליחה. אם קריאת ה-AI נכשלה
    //    (למשל בגלל מודל/מפתח לא תקינים - ראו התיקון ב-application.properties), הרשומה הריקה
    //    נשארה ב-DB לצמיתות: ה-GET היה מחזיר "0 המלצות, לא סופקה סיבה" במקום 404 אמיתי,
    //    וכל ניסיון חוזר ליצור היה נתקל תמיד ב"כבר קיים" -> קורס ב-500 (סעיף 1).
    //    כעת השמירה מתבצעת רק לאחר שהתקבלה תשובת AI תקינה עם רשימת ספרים בפועל.
    @Transactional
    public boolean add(UUID userId) {
        // 1. בדיקה חד-פעמית: אם כבר קיים רישום, נזרוק חריגה ייעודית שה-Controller יודע לתפוס
        if (repository.findByUserId(userId).isPresent()) {
            throw new RecommendationAlreadyExistsException(userId);
        }

        try {
            UserDto user = userClient.getUserById(userId);
            // צריך את כל המשתמשים כדי לפתור "שכונת ספר" = שכונת הבעלים שלו
            List<UserDto> allUsers = userClient.getAllUsers();
            Map<UUID, String> neighborhoodByUserId = buildNeighborhoodMap(allUsers);

            // מייצרים את ההמלצה בפועל דרך ה-AI *לפני* השמירה, כדי לא להשאיר רשומה ריקה ב-DB אם זה נכשל
            String strategy = CheckUserStrategy(user, neighborhoodByUserId);
            String prompt = BuildAiPrompt(strategy, neighborhoodByUserId, userId);
            String response = aiService.generateAsync(prompt);
            BookRecommendationDto aiJsonExecution = executeAiRequest(response);

            boolean isSaved = saveRecommendations(userId, aiJsonExecution);
            if (!isSaved) {
                System.err.println("Error initializing recommendation for user " + userId + ": AI response was empty or invalid");
            } else {
                System.out.println("Initialized recommendation record for user: " + userId);
            }
            return isSaved;
        } catch (RecommendationAlreadyExistsException e) {
            throw e; // ניתפס למעלה ב-Controller כ-409
        } catch (Exception e) {
            // כשל בקריאה לשירותים חיצוניים (userservice/book-loaning) או ל-AI (Groq) -
            // לא זורקים 500 גולמי, אלא מחזירים false כדי שה-Controller יגיב עם 502 ברור.
            System.err.println("Error initializing recommendation for user " + userId + ": " + e.getMessage());
            return false;
        }
    }

    // חריגה ייעודית: מאפשרת ל-Controller להחזיר 409 Conflict ידידותי במקום 500 גולמי וסתום.
    public static class RecommendationAlreadyExistsException extends RuntimeException {
        public RecommendationAlreadyExistsException(UUID userId) {
            super("Recommendation already exists for user " + userId);
        }
    }
    // ---------------------------------------------------------
    // DELETE – מחיקת Recommendation של משתמש
    // ---------------------------------------------------------
    @Transactional
    public void delete(UUID userId) {
        repository.deleteByUserId(userId);
    }

    // הוסרו: loadHistory() ו-bookIntialzation() - היו מתודות מוק/דמו שלא נקראו משום מקום
    // בקוד בפועל, והשתמשו בקבועים מטיפוס int (בהתנגשות עם המעבר ל-UUID).
}
