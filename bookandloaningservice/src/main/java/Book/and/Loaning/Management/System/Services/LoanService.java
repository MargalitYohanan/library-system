package Book.and.Loaning.Management.System.Services;

import Book.and.Loaning.Management.System.DTO.AdditionalDetailDTO;
import Book.and.Loaning.Management.System.DTO.BookDTO;
import Book.and.Loaning.Management.System.DTO.LoanDTO;
import Book.and.Loaning.Management.System.DTO.LoanHistoryDTO;
import Book.and.Loaning.Management.System.DTO.UserToUserNotificationDTO;
import Book.and.Loaning.Management.System.Entity.BookEntity;
import Book.and.Loaning.Management.System.Entity.LoanEntity;
import Book.and.Loaning.Management.System.Entity.LoanStatus;
import Book.and.Loaning.Management.System.Exceptions.BookServiceException;
import Book.and.Loaning.Management.System.Exceptions.LoanServiceException;
import Book.and.Loaning.Management.System.Mapping.LoanMapper; // ייבוא המאפר
import Book.and.Loaning.Management.System.Repository.BookRepository;
import Book.and.Loaning.Management.System.Repository.LoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final LoanMapper loanMapper;
    private final NotificationClient notificationClient;
    private final ActivityClient activityClient;

    public List<LoanDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(loanMapper::toDTO)
                .collect(Collectors.toList());
    }

    public LoanDTO getLoanById(UUID id) {
        LoanEntity entity = loanRepository.findById(id)
                .orElseThrow(() -> new LoanServiceException("Loan with ID " + id + " not found."));
        return loanMapper.toDTO(entity);
    }

    @Transactional
    public LoanDTO createLoan(LoanDTO loanDto) {
        // המרה ל-Entity לצורך בדיקות עסקיות
        LoanEntity loan = loanMapper.toEntity(loanDto);

        BookEntity book = bookRepository.findById(loan.getBookId())
                .orElseThrow(() -> new LoanServiceException("Cannot create loan, Book ID " + loan.getBookId() + " does not exist."));

        if (!book.isAvailable()) {
            throw new LoanServiceException("Book '" + book.getTitle() + "' is not available.");
        }

        if (book.getOwnerId().equals(loan.getBorrowerID())) {
            throw new LoanServiceException("You cannot borrow your own book.");
        }

        loan.setLoanStatus(LoanStatus.PENDING_APPROVAL);
        bookService.updateAvailability(loan.getBookId(), false);
        LoanEntity savedLoan = loanRepository.save(loan);

        // התראה לבעל הספר שיש בקשת השאלה חדשה
        notificationClient.sendLoanRequestAlert(buildLoanNotification(
                savedLoan.getBorrowerID(), book.getOwnerId(), book.getTitle()));

        // רישום פעילות אצל המבקש (הלווה)
        activityClient.logLoanRequestCreated(savedLoan.getBorrowerID(), book.getTitle());

        return loanMapper.toDTO(savedLoan);
    }

    public LoanDTO updateLoan(LoanDTO loanDto) {
        if (!loanRepository.existsById(loanDto.getId())) {
            throw new LoanServiceException("Cannot update, Loan ID " + loanDto.getId() + " not found.");
        }
        LoanEntity entity = loanMapper.toEntity(loanDto);
        LoanEntity updatedLoan = loanRepository.save(entity);
        return loanMapper.toDTO(updatedLoan);
    }

    @Transactional
    public void deleteLoan(UUID id) {
        LoanEntity loan = loanRepository.findById(id)
                .orElseThrow(() -> new LoanServiceException("Cannot delete, Loan ID " + id + " not found."));
        UUID bookId = loan.getBookId();
        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new LoanServiceException("Cannot delete, Book ID " + bookId + " not found."));

        bookService.updateAvailability(bookId, true);
        loanRepository.deleteById(id);
    }

    public List<LoanDTO> findLoansByOwnerId(UUID ownerId) {
        if (ownerId == null || ownerId.equals(new UUID(0, 0))) {
            throw new LoanServiceException("Invalid User ID: " + ownerId);
        }
        List<LoanEntity> loans = loanRepository.findLoansByOwnerId(ownerId);
        return loans.stream().map(loanMapper::toDTO).toList();
    }

    @Transactional
    public LoanEntity approveLoan(UUID loanId, UUID currentUserId) {

        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanServiceException("ההשאלה לא נמצאה"));
        if (loan.getLoanStatus() != LoanStatus.PENDING_APPROVAL) {
            throw new LoanServiceException("רק בקשות במצב המתנה ניתנות לאישור");
        }
        BookEntity book = bookRepository.findById(loan.getBookId())
                .orElseThrow(() -> new BookServiceException("הספר לא נמצא"));
        if (!book.getOwnerId().equals(currentUserId)) {
            throw new LoanServiceException("אין לך הרשאה לאשר השאלה לספר זה!");
        }
        book.setAvailable(false);
        bookRepository.save(book);
        loan.setLoanStatus(LoanStatus.LOANED);
        loan.setLoanDate(java.time.LocalDate.now());
        loan.setRequiredDate(java.time.LocalDate.now().plusDays(30));

        LoanEntity savedLoan = loanRepository.save(loan);

        // התראה למשאיל שהבקשה שלו אושרה
        notificationClient.sendLoanApproval(buildLoanNotification(
                book.getOwnerId(), savedLoan.getBorrowerID(), book.getTitle()));

        // רישום פעילות אצל בעל הספר (מי שאישר)
        activityClient.logLoanRequestApproved(currentUserId, book.getTitle());

        return savedLoan;
    }

    @Transactional
    public LoanEntity rejectLoan(UUID loanId, UUID currentUserId) {

        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanServiceException("ההשאלה לא נמצאה"));

        if (loan.getLoanStatus() != LoanStatus.PENDING_APPROVAL) {
            throw new LoanServiceException("ניתן לדחות רק בקשות במצב המתנה");
        }
        BookEntity book = bookRepository.findById(loan.getBookId())
                .orElseThrow(() -> new BookServiceException("הספר לא נמצא"));
        if (!book.getOwnerId().equals(currentUserId)) {
            throw new LoanServiceException("אין לך הרשאה לדחות השאלה לספר זה!");
        }


        loan.setLoanStatus(LoanStatus.REJECTED);
        book.setAvailable(true);

        bookRepository.save(book);
        LoanEntity savedLoan = loanRepository.save(loan);

        // רישום פעילות אצל בעל הספר (מי שדחה)
        activityClient.logLoanRequestRejected(currentUserId, book.getTitle());

        return savedLoan;
    }

    public List<LoanDTO> getPendingRequestsByOwnerId(UUID ownerId) {

        List<LoanEntity> pendingLoans = loanRepository.findPendingLoansByOwnerId(ownerId);
        return pendingLoans.stream()
                .map(loanMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LoanDTO returnBook(UUID loanId) {

        LoanEntity loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanServiceException("Loan with ID " + loanId + " not found."));
        BookEntity book = bookRepository.findById(loan.getBookId())
                .orElseThrow(() -> new BookServiceException("Book with ID " + loan.getBookId() + " not found."));
        loan.setLoanStatus(LoanStatus.RETURNED);
        loan.setReturnDate(java.time.LocalDate.now());
        book.setAvailable(true);
        bookRepository.save(book);
        LoanDTO returnedLoan = loanMapper.toDTO(loanRepository.save(loan));

        // רישום פעילות אצל הלווה שהחזיר את הספר
        activityClient.logLoanReturned(loan.getBorrowerID(), book.getTitle());

        return returnedLoan;
    }

    @Transactional
    public List<BookDTO> getBorrowedBooksByOwnerId(UUID ownerId) {
        List<LoanDTO> loanDTOList = findLoansByOwnerId(ownerId);
        return loanDTOList.stream().filter(loan -> loan.getLoanStatus() == LoanStatus.LOANED)
                .map(loan -> bookService.getBookById(loan.getBookId()))
                .collect(Collectors.toList());
    }

    /**
     * היסטוריית ההשאלות של משתמש כלווה - מוגש דרך /api/loans/history/{borrowerId}
     * עבור smart-library-ai לבניית המלצות. שדה מיקום/שכונה לא נכלל בכוונה - ראו TODO
     * ב-CheckUserStrategy בצד smart-library-ai.
     */
    public List<LoanHistoryDTO> getLoanHistory(UUID borrowerId) {
        return loanRepository.findByBorrowerID(borrowerId).stream()
                .map(loan -> {
                    BookEntity book = bookRepository.findById(loan.getBookId()).orElse(null);
                    String category = (book != null && book.getCategory() != null) ? book.getCategory().name() : null;
                    UUID ownerId = book != null ? book.getOwnerId() : null;
                    return new LoanHistoryDTO(loan.getBookId(), category, ownerId);
                })
                .collect(Collectors.toList());
    }

    /**
     * בונה DTO סטנדרטי להתראה בין שני משתמשים (מי שולח -> למי, ועל איזה ספר).
     */
    private UserToUserNotificationDTO buildLoanNotification(UUID fromUserId, UUID toUserId, String bookTitle) {
        UserToUserNotificationDTO dto = new UserToUserNotificationDTO();
        dto.setFromUserId(fromUserId);
        dto.setToUserId(toUserId);

        AdditionalDetailDTO bookDetail = new AdditionalDetailDTO();
        bookDetail.setTypeId(4); // 4 = "bookName" ב-type_additional_details (ראו notification-service/data.sql)
        bookDetail.setValue(bookTitle);

        dto.setAdditionalDetails(List.of(bookDetail));
        return dto;
    }
}
