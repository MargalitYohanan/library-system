package com.library.smart_library_ai.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.library.smart_library_ai.dto.BookDto;
import com.library.smart_library_ai.dto.LoanHistoryDto;
import com.library.smart_library_ai.dto.BookRecommendationDto;
import com.library.smart_library_ai.dto.UserDto;
import com.library.smart_library_ai.entity.BookRecommendation;
import com.library.smart_library_ai.repository.BookRecommendationRepository;
import com.library.smart_library_ai.service.clients.GroqAiService;
import com.library.smart_library_ai.service.clients.LoanClient;
import com.library.smart_library_ai.service.clients.UserClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class BookRecommendationServiceTest {

    @Mock
    private BookRecommendationRepository repository;

    @Mock
    private UserClient userClient;

    @Mock
    private LoanClient loanClient;

    @Mock
    private BookCacheService bookCacheService;

    @Mock
    private GroqAiService aiService;

    @InjectMocks
    private BookRecommendationService service;

    // UUIDs לשימוש בטסטים
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID BOOK_ID_1 = UUID.randomUUID();
    private final UUID BOOK_ID_2 = UUID.randomUUID();
    private final UUID OWNER_ID_TLV = UUID.randomUUID();
    private final UUID OWNER_ID_HAIFA = UUID.randomUUID();

    // -----------------------------------------------------------------
    // CheckUserStrategy
    // -----------------------------------------------------------------

    @Test
    void testCheckUserStrategy_LowHistoryRatio() {

        UserDto user = new UserDto();
        user.setUserId(USER_ID);
        user.setNeighborhoodName("תל אביב");

        Map<UUID, String> neighborhoodByUserId = new HashMap<>();
        neighborhoodByUserId.put(USER_ID, "תל אביב");
        neighborhoodByUserId.put(OWNER_ID_HAIFA, "חיפה");

        // היסטוריית השאלות - ספר יחיד, מבעלים שלא מתל אביב => יחס נמוך
        LoanHistoryDto loan = new LoanHistoryDto();
        loan.setBookId(BOOK_ID_1);
        loan.setOwnerId(OWNER_ID_HAIFA);

        when(loanClient.getLoanHistory(USER_ID))
                .thenReturn(List.of(loan));

        String strategy = service.CheckUserStrategy(user, neighborhoodByUserId);

        assertTrue(
                strategy.contains("10% קרבה")
                        || strategy.contains("50% גיל")
        );
    }

    @Test
    void testCheckUserStrategy_Logic() {

        UserDto user = new UserDto();
        user.setUserId(USER_ID);
        user.setNeighborhoodName("תל אביב");

        Map<UUID, String> neighborhoodByUserId = new HashMap<>();
        neighborhoodByUserId.put(USER_ID, "תל אביב");
        neighborhoodByUserId.put(OWNER_ID_TLV, "תל אביב");
        neighborhoodByUserId.put(OWNER_ID_HAIFA, "חיפה");

        List<LoanHistoryDto> history = new ArrayList<>();

        LoanHistoryDto loan1 = new LoanHistoryDto();
        loan1.setBookId(BOOK_ID_1);
        loan1.setOwnerId(OWNER_ID_HAIFA);
        history.add(loan1);

        LoanHistoryDto loan2 = new LoanHistoryDto();
        loan2.setBookId(BOOK_ID_2);
        loan2.setOwnerId(OWNER_ID_HAIFA);
        history.add(loan2);

        when(loanClient.getLoanHistory(USER_ID))
                .thenReturn(history);

        String strategy = service.CheckUserStrategy(user, neighborhoodByUserId);

        // אף אחד מהספרים לא מבעלים משכונת "תל אביב" => יחס 0, נופל למסלול המשקל הנמוך
        assertTrue(strategy.contains("10% קרבה"));
    }

    // -----------------------------------------------------------------
    // BuildAiPrompt
    // -----------------------------------------------------------------

    @Test
    void testBuildAiPrompt_ContainsRequiredFormat() {

        // תוקן: נוסף available=true (השדה החדש) וב-requestingUserId משתמשים במשתמש שונה
        // מבעל הספר (USER_ID, לא OWNER_ID_TLV) - אחרת הספר היה מסונן החוצה כ"ספר שבבעלות
        // המבקש" ובדיקת ה-contains הייתה נכשלת.
        BookDto book = new BookDto(
                BOOK_ID_1,
                "Title",
                "Category",
                OWNER_ID_TLV,
                true
        );

        when(bookCacheService.getBooks())
                .thenReturn(List.of(book));

        Map<UUID, String> neighborhoodByUserId = new HashMap<>();
        neighborhoodByUserId.put(OWNER_ID_TLV, "תל אביב");

        String prompt = service.BuildAiPrompt("Test Strategy", neighborhoodByUserId, USER_ID);

        assertTrue(prompt.contains("bookIds"));
        assertTrue(prompt.contains("reasonForChoice"));

        // מאחר שהמערכת עברה ל-UUID,
        // ה-Prompt צריך להכיל את ה-UUID של הספר.
        assertTrue(prompt.contains(BOOK_ID_1.toString()));
    }

    // -----------------------------------------------------------------
    // ExecuteAiRequest
    // -----------------------------------------------------------------

    @Test
    void testExecuteAiRequest_ValidJson() {

        String jsonPrompt =
                "{"
                        + "\"bookIds\": [\"" + BOOK_ID_1 + "\", \"" + BOOK_ID_2 + "\"],"
                        + "\"reasonForChoice\": \"Good match\""
                        + "}";

        BookRecommendationDto result =
                service.executeAiRequest(jsonPrompt);

        assertNotNull(result);
        assertEquals("Good match", result.getReasonForChoice());
        assertNotNull(result.getGeneratedDate());

        assertNotNull(result.getBookIds());
        assertEquals(2, result.getBookIds().size());
        assertEquals(BOOK_ID_1, result.getBookIds().get(0));
        assertEquals(BOOK_ID_2, result.getBookIds().get(1));
    }

    @Test
    void testExecuteAiRequest_InvalidJson() {

        String invalidJson = "{ invalid }";

        BookRecommendationDto result =
                service.executeAiRequest(invalidJson);

        assertNotNull(result);
        assertNull(result.getReasonForChoice());
    }

    // -----------------------------------------------------------------
    // SaveRecommendations
    // -----------------------------------------------------------------

    @Test
    void testSaveRecommendations_DeletesAndSaves() {

        BookRecommendationDto dto =
                new BookRecommendationDto();

        dto.setBookIds(
                List.of(BOOK_ID_1, BOOK_ID_2)
        );

        dto.setReasonForChoice("Reason");
        dto.setGeneratedDate(LocalDateTime.now());

        boolean result =
                service.saveRecommendations(USER_ID, dto);

        assertTrue(result);

        verify(repository, times(1))
                .deleteByUserId(USER_ID);

        verify(repository, times(1))
                .save(any(BookRecommendation.class));
    }

    @Test
    void testSaveRecommendations_InvalidDto_ReturnsFalse() {

        boolean result =
                service.saveRecommendations(USER_ID, null);

        assertFalse(result);

        verify(repository, never())
                .deleteByUserId(USER_ID);

        verify(repository, never())
                .save(any());
    }

    // -----------------------------------------------------------------
    // GetRecommendationsForUser
    // -----------------------------------------------------------------

    @Test
    void testGetRecommendationsForUser_Success() {

        UserDto mockUser = new UserDto();

        mockUser.setUserId(USER_ID);
        mockUser.setNeighborhoodName("תל אביב");

        Map<UUID, String> neighborhoodByUserId = new HashMap<>();
        neighborhoodByUserId.put(USER_ID, "תל אביב");

        String mockAiResponse =
                "{"
                        + "\"bookIds\": [\"" + BOOK_ID_1 + "\", \"" + BOOK_ID_2 + "\"],"
                        + "\"reasonForChoice\": \"ניסוי\""
                        + "}";

        when(loanClient.getLoanHistory(USER_ID))
                .thenReturn(new ArrayList<>());

        when(bookCacheService.getBooks())
                .thenReturn(new ArrayList<>());

        when(aiService.generateAsync(anyString()))
                .thenReturn(mockAiResponse);

        service.getRecommendationsForUser(mockUser, neighborhoodByUserId);

        verify(repository, times(1))
                .save(any(BookRecommendation.class));
    }

    @Test
    void testGetRecommendationsForUser_FullFlow_Success() {

        UserDto mockUser = new UserDto();

        mockUser.setUserId(USER_ID);
        mockUser.setNeighborhoodName("תל אביב");

        Map<UUID, String> neighborhoodByUserId = new HashMap<>();
        neighborhoodByUserId.put(USER_ID, "תל אביב");

        when(loanClient.getLoanHistory(USER_ID))
                .thenReturn(new ArrayList<>());

        when(bookCacheService.getBooks())
                .thenReturn(new ArrayList<>());

        String mockAiResponse =
                "{"
                        + "\"bookIds\": [\"" + BOOK_ID_1 + "\", \"" + BOOK_ID_2 + "\"],"
                        + "\"reasonForChoice\": \"בגלל מיקום ותחומי עניין\""
                        + "}";

        when(aiService.generateAsync(anyString()))
                .thenReturn(mockAiResponse);

        service.getRecommendationsForUser(mockUser, neighborhoodByUserId);

        verify(aiService, times(1))
                .generateAsync(anyString());

        verify(repository, times(1))
                .save(any(BookRecommendation.class));
    }

    @Test
    void testGetRecommendationsForUser_FullFlow() {

        UserDto user = new UserDto();

        user.setUserId(USER_ID);
        user.setNeighborhoodName("תל אביב");

        Map<UUID, String> neighborhoodByUserId = new HashMap<>();
        neighborhoodByUserId.put(USER_ID, "תל אביב");

        String aiJsonResponse =
                "{"
                        + "\"bookIds\": [\"" + BOOK_ID_1 + "\", \"" + BOOK_ID_2 + "\"],"
                        + "\"reasonForChoice\": \"בחירה מבוססת מיקום\""
                        + "}";

        when(loanClient.getLoanHistory(USER_ID))
                .thenReturn(new ArrayList<>());

        when(bookCacheService.getBooks())
                .thenReturn(new ArrayList<>());

        when(aiService.generateAsync(anyString()))
                .thenReturn(aiJsonResponse);

        service.getRecommendationsForUser(user, neighborhoodByUserId);

        verify(repository, times(1))
                .save(any(BookRecommendation.class));

        verify(aiService, times(1))
                .generateAsync(anyString());
    }

    // -----------------------------------------------------------------
    // GetUserById
    // -----------------------------------------------------------------

    @Test
    void testGetUserById_CallsClientCorrectly() {

        UserDto mockUser = new UserDto();

        mockUser.setUserId(USER_ID);

        when(userClient.getUserById(USER_ID))
                .thenReturn(mockUser);

        UserDto result =
                service.GetUserById(USER_ID);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());

        verify(userClient, times(1))
                .getUserById(USER_ID);
    }

    // -----------------------------------------------------------------
    // Delete
    // -----------------------------------------------------------------

    @Test
    void testDelete_CallsRepository() {

        service.delete(USER_ID);

        verify(repository, times(1))
                .deleteByUserId(USER_ID);
    }

    // -----------------------------------------------------------------
    // Add
    // -----------------------------------------------------------------

    @Test
    void testAdd_InitializationSuccess() {

        UserDto mockUser = new UserDto();

        mockUser.setUserId(USER_ID);
        mockUser.setNeighborhoodName("תל אביב");

        when(repository.findByUserId(USER_ID))
                .thenReturn(Optional.empty());

        when(userClient.getUserById(USER_ID))
                .thenReturn(mockUser);

        // add() בונה כעת גם מיפוי שכונות דרך getAllUsers() (כדי לפתור "שכונת ספר" = שכונת הבעלים)
        when(userClient.getAllUsers())
                .thenReturn(List.of(mockUser));

        when(loanClient.getLoanHistory(USER_ID))
                .thenReturn(new ArrayList<>());

        when(bookCacheService.getBooks())
                .thenReturn(new ArrayList<>());

        when(aiService.generateAsync(anyString()))
                .thenReturn(
                        "{"
                                + "\"bookIds\": [\"" + BOOK_ID_1 + "\"],"
                                + "\"reasonForChoice\": \"test\""
                                + "}"
                );

        boolean result =
                service.add(USER_ID);

        assertTrue(result);

        // פעם אחת באתחול הרשומה
        // ופעם נוספת בתוך getRecommendationsForUser
        verify(repository, times(2))
                .save(any(BookRecommendation.class));
    }
}
