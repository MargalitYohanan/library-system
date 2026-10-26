import { useCallback, useEffect, useState } from 'react';
import { fetchCurrentUserId } from '../../utils/currentUser';
import {
  addBook,
  getAllBooksByOwnerId,
  getBookById,
  removeBook,
  updateAvailability,
  updateBook,
} from '../../api/bookApi';
import {
  approveLoan,
  getBorrowedBooksByOwner,
  getRequestsByOwnerId,
  rejectLoan,
} from '../../api/loanApi';
import {
  CATEGORY_LABELS,
  CATEGORY_OPTIONS,
  TARGET_AGE_OPTIONS,
} from '../../constants/bookConstans';
import './PersonalArea.css';

const EMPTY_FORM = {
  title: '',
  author: '',
  category: CATEGORY_OPTIONS[0]?.value || '',
  description: '',
  bookCondition: '',
  loanDurationDays: 30,
  targetAge: TARGET_AGE_OPTIONS[0]?.value || '',
};

const VIEWS = {
  MY_BOOKS: 'myBooks',
  LENT_OUT: 'lentOut',
  PENDING: 'pending',
};

function getServerErrorMessage(err, fallback) {
  const data = err?.response?.data;
  return typeof data === 'string' && data ? data : fallback;
}

function PersonalArea() {
  const [userId, setUserId] = useState(null);
  const [activeView, setActiveView] = useState(VIEWS.MY_BOOKS);

  const [myBooks, setMyBooks] = useState([]);
  const [lentOutBooks, setLentOutBooks] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);

  // שלב ראשון - שליפת המשתמש המחובר. שירות הספרים/השאלות אינו מחלץ
  // משתמש מתוך טוקן, ולכן ownerId חייב להישלח במפורש בכל קריאה.
  useEffect(() => {
    fetchCurrentUserId()
      .then(setUserId)
      .catch(() => {
        setError('לא ניתן לזהות את המשתמש המחובר.');
        setLoading(false);
      });
  }, []);

  const loadMyBooks = useCallback(async (id) => {
    const data = await getAllBooksByOwnerId(id);
    setMyBooks(data || []);
  }, []);

  const loadLentOut = useCallback(async (id) => {
    const data = await getBorrowedBooksByOwner(id);
    setLentOutBooks(data || []);
  }, []);

  const loadPending = useCallback(async (id) => {
    const loans = (await getRequestsByOwnerId(id)) || [];
    const enriched = await Promise.all(
      loans.map(async (loan) => {
        try {
          const book = await getBookById(loan.bookId);
          return { ...loan, bookTitle: book.title, bookAuthor: book.author };
        } catch {
          return { ...loan, bookTitle: `ספר #${loan.bookId}` };
        }
      })
    );
    setPendingRequests(enriched);
  }, []);

  useEffect(() => {
    if (!userId) return;

    const loadAll = async () => {
      setLoading(true);
      setError('');
      try {
        await Promise.all([
          loadMyBooks(userId),
          loadLentOut(userId),
          loadPending(userId),
        ]);
      } catch (err) {
        setError(
          getServerErrorMessage(
            err,
            'אירעה שגיאה בטעינת הנתונים. ייתכן שחלק מנתיבי ה-API טרם נוספו בשרת.'
          )
        );
      } finally {
        setLoading(false);
      }
    };

    loadAll();
  }, [userId, loadMyBooks, loadLentOut, loadPending]);

  // ---------- ניהול ספרים ----------

  const openAddModal = () => {
    setEditingBook(null);
    setForm(EMPTY_FORM);
    setIsModalOpen(true);
  };

  const openEditModal = (book) => {
    setEditingBook(book);
    setForm({
      title: book.title || '',
      author: book.author || '',
      category: book.category || CATEGORY_OPTIONS[0]?.value || '',
      description: book.description || '',
      bookCondition: book.bookCondition || '',
      loanDurationDays: book.loanDurationDays || 30,
      targetAge: book.targetAge || TARGET_AGE_OPTIONS[0]?.value || '',
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingBook(null);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: name === 'loanDurationDays' ? Number(value) : value,
    }));
  };

  const handleSubmitForm = async (e) => {
    e.preventDefault();
    setFeedback('');
    try {
      if (editingBook) {
        await updateBook({
          ...editingBook,
          ...form,
        });
        setFeedback('פרטי הספר עודכנו בהצלחה.');
      } else {
        await addBook({ ...form, ownerId: userId });
        setFeedback('הספר נוסף בהצלחה לספרייה שלך.');
      }
      closeModal();
      loadMyBooks(userId);
    } catch (err) {
      setFeedback(getServerErrorMessage(err, 'שמירת הספר נכשלה. נסו שוב.'));
    }
  };

  const handleDelete = async (book) => {
    if (!book.available) return; 
    const confirmed = window.confirm(`למחוק את "${book.title}"?`);
    if (!confirmed) return;

    setFeedback('');
    try {
      await removeBook(book.id);
      setFeedback('הספר נמחק בהצלחה.');
      loadMyBooks(userId);
    } catch (err) {
      setFeedback(getServerErrorMessage(err, 'מחיקת הספר נכשלה.'));
    }
  };

  const handleToggleAvailability = async (book) => {
    setFeedback('');
    try {
      await updateAvailability(book.id, !book.available);
      setMyBooks((prev) =>
        prev.map((b) =>
          b.id === book.id ? { ...b, available: !b.available } : b
        )
      );
    } catch (err) {
      setFeedback(getServerErrorMessage(err, 'עדכון הזמינות נכשל.'));
    }
  };

  // ---------- ניהול בקשות השאלה ----------

  const handleApprove = async (loan) => {
    setFeedback('');
    try {
      await approveLoan(loan.id, userId);
      setFeedback('הבקשה אושרה והספר סומן כמושאל.');
      loadPending(userId);
      loadLentOut(userId);
      loadMyBooks(userId);
    } catch (err) {
      setFeedback(getServerErrorMessage(err, 'אישור הבקשה נכשל.'));
    }
  };

  const handleReject = async (loan) => {
    setFeedback('');
    try {
      await rejectLoan(loan.id, userId);
      setFeedback('הבקשה נדחתה.');
      loadPending(userId);
      loadMyBooks(userId);
    } catch (err) {
      setFeedback(getServerErrorMessage(err, 'דחיית הבקשה נכשלה.'));
    }
  };

  return (
    <div className="personal-area">
      <div className="personal-area__header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h1 className="personal-area__title">אזור אישי</h1>
        <button className="personal-area__add-btn" onClick={openAddModal}>
          + הוסף ספר
        </button>
      </div>

      <nav className="personal-area__tabs">
        <button
          className={`personal-area__tab ${activeView === VIEWS.MY_BOOKS ? 'is-active' : ''}`}
          onClick={() => setActiveView(VIEWS.MY_BOOKS)}
        >
          הספרים שלי
        </button>
        <button
          className={`personal-area__tab ${activeView === VIEWS.LENT_OUT ? 'is-active' : ''}`}
          onClick={() => setActiveView(VIEWS.LENT_OUT)}
        >
          ספרים שהשאלתי
        </button>
        <button
          className={`personal-area__tab ${activeView === VIEWS.PENDING ? 'is-active' : ''}`}
          onClick={() => setActiveView(VIEWS.PENDING)}
        >
          בקשות ממתינות לאישור
          {pendingRequests.length > 0 && (
            <span className="personal-area__tab-count">{pendingRequests.length}</span>
          )}
        </button>
      </nav>

      {feedback && <div className="personal-area__feedback">{feedback}</div>}
      {error && <div className="personal-area__error">{error}</div>}

      {loading ? (
        <div className="personal-area__status">טוען נתונים...</div>
      ) : (
        <>
          {activeView === VIEWS.MY_BOOKS && (
            <MyBooksList
              books={myBooks}
              onEdit={openEditModal}
              onDelete={handleDelete}
              onToggleAvailability={handleToggleAvailability}
              onAddClick={openAddModal} 
            />
          )}

          {activeView === VIEWS.LENT_OUT && <LentOutList books={lentOutBooks} />}

          {activeView === VIEWS.PENDING && (
            <PendingRequestsList
              requests={pendingRequests}
              onApprove={handleApprove}
              onReject={handleReject}
            />
          )}
        </>
      )}

      {isModalOpen && (
        <BookFormModal
          form={form}
          isEditing={!!editingBook}
          onChange={handleFormChange}
          onSubmit={handleSubmitForm}
          onClose={closeModal}
        />
      )}
    </div>
  );
}

function MyBooksList({ books, onEdit, onDelete, onToggleAvailability, onAddClick }) {
  if (books.length === 0) {
    return (
      <div className="personal-area__status" style={{ textAlign: 'center', padding: '20px' }}>
        <p>עדיין לא הוספת ספרים לספרייה שלך.</p>
        <button
          className="personal-area__add-btn"
          onClick={onAddClick}
          style={{ marginTop: '15px', padding: '10px 20px', cursor: 'pointer' }}
        >
          + הוסף את הספר הראשון שלך
        </button>
      </div>
    );
  }

  return (
    <div className="my-books-area">

      <div className="my-books-table">
        <div className="my-books-table__head">
          <span>שם הספר</span>
          <span>מחבר</span>
          <span>קטגוריה</span>
          <span>סטטוס</span>
          <span>פעולות</span>
        </div>

        {books.map((book) => (
          <div
            key={book.id}
            className={`my-books-table__row ${!book.available ? 'my-books-table__row--gray' : ''}`}
          >
            <span className="my-books-table__title">{book.title}</span>
            <span>{book.author}</span>
            <span>{CATEGORY_LABELS[book.category] || book.category}</span>
            <span className="my-books-table__status-cell">
              <label className="availability-toggle">
                <input
                  type="checkbox"
                  checked={!!book.available}
                  onChange={() => onToggleAvailability(book)}
                />
                <span className="availability-toggle__slider" />
              </label>
              {book.available ? 'זמין' : 'לא זמין'}
            </span>
            <span className="my-books-table__actions">
              <button className="my-books-table__btn" onClick={() => onEdit(book)}>
                עריכה
              </button>
              <button
                className="my-books-table__btn my-books-table__btn--danger"
                disabled={!book.available}
                title={!book.available ? 'ניתן למחוק רק ספר זמין' : ''}
                onClick={() => onDelete(book)}
              >
                מחיקה
              </button>
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function LentOutList({ books }) {
  if (books.length === 0) {
    return <div className="personal-area__status">אין כרגע ספרים שמושאלים לאחרים.</div>;
  }

  return (
    <div className="my-books-table">
      <div className="my-books-table__head my-books-table__head--loans">
        <span>שם הספר</span>
        <span>מחבר</span>
      </div>
      {books.map((book) => (
        <div key={book.id} className="my-books-table__row my-books-table__row--loans">
          <span className="my-books-table__title">{book.title}</span>
          <span>{book.author}</span>
        </div>
      ))}
    </div>
  );
}

function PendingRequestsList({ requests, onApprove, onReject }) {
  if (requests.length === 0) {
    return <div className="personal-area__status">אין כרגע בקשות השאלה הממתינות לאישורך.</div>;
  }

  return (
    <div className="my-books-table">
      <div className="my-books-table__head my-books-table__head--requests">
        <span>שם הספר</span>
        <span>פעולות</span>
      </div>
      {requests.map((req) => (
        <div key={req.id} className="my-books-table__row my-books-table__row--requests">
          <span className="my-books-table__title">{req.bookTitle}</span>
          <span className="my-books-table__actions">
            <button
              className="my-books-table__btn my-books-table__btn--approve"
              onClick={() => onApprove(req)}
            >
              אשר השאלה
            </button>
            <button
              className="my-books-table__btn my-books-table__btn--danger"
              onClick={() => onReject(req)}
            >
              דחה
            </button>
          </span>
        </div>
      ))}
    </div>
  );
}

function BookFormModal({ form, isEditing, onChange, onSubmit, onClose }) {
  return (
    <div className="book-modal-overlay" onClick={onClose}>
      <div className="book-modal" onClick={(e) => e.stopPropagation()}>
        <h2 className="book-modal__title">{isEditing ? 'עריכת ספר' : 'הוספת ספר חדש'}</h2>

        <form className="book-modal__form" onSubmit={onSubmit}>
          <label>
            שם הספר
            <input name="title" value={form.title} onChange={onChange} required />
          </label>

          <label>
            מחבר
            <input name="author" value={form.author} onChange={onChange} required />
          </label>

          <label>
            קטגוריה
            <select name="category" value={form.category} onChange={onChange}>
              {CATEGORY_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            גיל יעד
            <select name="targetAge" value={form.targetAge} onChange={onChange}>
              {TARGET_AGE_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            מצב הספר
            <input
              name="bookCondition"
              value={form.bookCondition}
              onChange={onChange}
              placeholder='לדוגמה: כמו חדש, מצב טוב'
            />
          </label>

          <label>
            ימי השאלה
            <input
              type="number"
              min="1"
              name="loanDurationDays"
              value={form.loanDurationDays}
              onChange={onChange}
            />
          </label>

          <label className="book-modal__full">
            תיאור קצר
            <textarea name="description" value={form.description} onChange={onChange} rows={3} />
          </label>

          <div className="book-modal__actions">
            <button type="button" className="book-modal__btn book-modal__btn--ghost" onClick={onClose}>
              ביטול
            </button>
            <button type="submit" className="book-modal__btn">
              {isEditing ? 'שמירת שינויים' : 'הוספת ספר'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default PersonalArea;
