package Book.and.Loaning.Management.System.Controller;

import Book.and.Loaning.Management.System.DTO.BookDTO;
import Book.and.Loaning.Management.System.Entity.Category;
import Book.and.Loaning.Management.System.Services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
@CrossOrigin(origins = "http://localhost:3000") 
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable("id") UUID id) { 
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PutMapping("/updateBook")
    public ResponseEntity<BookDTO> updateBook(@RequestBody BookDTO book) {
        return ResponseEntity.ok(bookService.updateBook(book));
    }

    @DeleteMapping("/removeBook/{id}")
    public ResponseEntity<Void> removeBook(@PathVariable("id") UUID id) { 
        bookService.removeBook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/addBook")
    public ResponseEntity<BookDTO> addBook(@RequestBody BookDTO book) {
        return ResponseEntity.ok(bookService.addBook(book));
    }

    @GetMapping("/getBooksBorrowedByUser/{borrowerId}")
    public ResponseEntity<List<BookDTO>> getBooksBorrowedByUser(@PathVariable("borrowerId") UUID borrowerId) {
        List<BookDTO> borrowedBooks = bookService.getBooksBorrowedByUser(borrowerId);
        return ResponseEntity.ok(borrowedBooks);
    }

    @GetMapping("/getBooksByOwner/{ownerId}")
    public ResponseEntity<List<BookDTO>> getBooksByOwner(@PathVariable("ownerId") UUID ownerId) {
        List<BookDTO> books = bookService.getAllBooksByOwnerId(ownerId);
        return ResponseEntity.ok(books);
    }

    @PatchMapping("/updateAvailability/{id}")
    public ResponseEntity<Void> updateAvailability(@PathVariable("id") UUID id, @RequestParam("isAvailable") boolean isAvailable) { // <--- תוקן
        bookService.updateAvailability(id, isAvailable);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> searchBooks(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "category", required = false) Category category) { 
        List<BookDTO> books = bookService.searchBooks(title, author, category);
        return ResponseEntity.ok(books);
    }
}