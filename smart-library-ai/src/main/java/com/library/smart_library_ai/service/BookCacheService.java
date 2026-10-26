package com.library.smart_library_ai.service;

import com.library.smart_library_ai.dto.BookDto;
import com.library.smart_library_ai.service.clients.BookClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookCacheService {
    private final BookClient bookClient;
    // נותן רשימה ריקה כברירת מחדל כדי למנוע NullPointerException
    private List<BookDto> cachedBooks = new ArrayList<>();

    public BookCacheService(BookClient bookClient) {
        this.bookClient = bookClient;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void refreshBooks() {
        try {
            System.out.println("Refreshing book cache...");
            this.cachedBooks = bookClient.getAllBooks();
            System.out.println("Book cache refreshed successfully! Total books: " + cachedBooks.size());
        } catch (Exception e) {
            System.err.println("Failed to refresh book cache: " + e.getMessage());
            // כאן אפשר להחליט אם להשאיר את ה-cache הישן או לרוקן
        }
    }

    // טעינה ראשונית רק לאחר שהאפליקציה עלתה לחלוטין ובאופן אסינכרוני
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void init() {
        System.out.println("Application is ready. Starting background cache initialization...");
        refreshBooks();
    }

    public List<BookDto> getBooks() {
        return cachedBooks;
    }
}