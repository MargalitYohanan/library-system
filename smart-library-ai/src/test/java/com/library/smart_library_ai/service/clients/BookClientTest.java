package com.library.smart_library_ai.service.clients;

import com.library.smart_library_ai.dto.BookDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookClientTest {

    private static MockWebServer mockWebServer;
    private BookClient bookClient;

    private static final UUID BOOK_ID_1 =
            UUID.randomUUID();

    private static final UUID BOOK_ID_2 =
            UUID.randomUUID();

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void initialize() {

        // מחברים את ה-Client לכתובת של השרת המדומה
        String baseUrl = mockWebServer.url("/").toString();

        bookClient = new BookClient(
                WebClient.builder(),
                baseUrl
        );
    }

    @Test
    void testGetAllBooks_Success() {

        // הכנת תשובה מדומה מהשרת
        String jsonResponse =
                "["
                        + "{\"id\":\"" + BOOK_ID_1 + "\", \"title\":\"Java Basics\"},"
                        + "{\"id\":\"" + BOOK_ID_2 + "\", \"title\":\"Spring Guide\"}"
                        + "]";

        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(jsonResponse)
                        .addHeader("Content-Type", "application/json")
        );

        // הרצה
        List<BookDto> books =
                bookClient.getAllBooks();

        // בדיקה
        assertNotNull(books);
        assertEquals(2, books.size());

        assertEquals(
                BOOK_ID_1,
                books.get(0).getBookId()
        );

        assertEquals(
                "Java Basics",
                books.get(0).getTitle()
        );

        assertEquals(
                BOOK_ID_2,
                books.get(1).getBookId()
        );
    }
}
