import { useEffect, useState, useCallback } from 'react';
import { getAllBooks, searchBooks } from '../../api/bookApi';
import { requestLoan } from '../../api/loanApi';
import { fetchCurrentUserId } from '../../utils/currentUser';
import { CATEGORY_OPTIONS } from '../../constants/bookConstans';
import BookCard from '../../components/BookCard/BookCard';
import './BookCatalog.css';

function BookCatalog() {
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [currentUserId, setCurrentUserId] = useState(null);

  const [filters, setFilters] = useState({ title: '', author: '', category: '' });

  useEffect(() => {
    fetchCurrentUserId()
      .then(setCurrentUserId)
      .catch(() => setError('לא ניתן לזהות את המשתמש המחובר. יש להתחבר מחדש.'));
  }, []);

  const loadAllBooks = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getAllBooks();
      setBooks(data || []);
    } catch (err) {
      setError('אירעה שגיאה בטעינת הספרים. נסו שוב מאוחר יותר.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAllBooks();
  }, [loadAllBooks]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    const hasFilters = filters.title || filters.author || filters.category;

    if (!hasFilters) {
      loadAllBooks();
      return;
    }

    setLoading(true);
    setError('');
    try {
      const data = await searchBooks(filters);
      setBooks(data || []);
    } catch (err) {
      setError('אירעה שגיאה בחיפוש. נסו שוב מאוחר יותר.');
    } finally {
      setLoading(false);
    }
  };

  const handleClearSearch = () => {
    setFilters({ title: '', author: '', category: '' });
    loadAllBooks();
  };

  const handleRequestLoan = async (book) => {
    setFeedback('');
    if (!currentUserId) {
      setFeedback('לא ניתן לזהות את המשתמש המחובר. נסו לרענן את הדף.');
      return;
    }
    if (book.ownerId === currentUserId) {
      setFeedback('לא ניתן להשאיל ספר שבבעלותך.');
      return;
    }
    try {
      await requestLoan(book.id, currentUserId);
      setFeedback(`בקשת ההשאלה עבור "${book.title}" נשלחה בהצלחה לבעל הספר.`);
      // הספר הופך ללא זמין מיידית במסך
      setBooks((prev) =>
        prev.map((b) => (b.id === book.id ? { ...b, available: false } : b))
      );
    } catch (err) {
      const serverMessage = err?.response?.data;
      setFeedback(
        typeof serverMessage === 'string'
          ? serverMessage
          : 'שליחת בקשת ההשאלה נכשלה. נסו שוב.'
      );
    }
  };

  return (
    <div className="catalog-page">
      <header className="catalog-page__header">
        <h1 className="catalog-page__title">קטלוג הספרים</h1>
        <p className="catalog-page__subtitle">
          חפשו ספר לפי שם, מחבר או קטגוריה, ובקשו השאלה בלחיצת כפתור.
        </p>
      </header>

      <form className="catalog-search" onSubmit={handleSearch}>
        <input
          className="catalog-search__input"
          type="text"
          name="title"
          placeholder="חיפוש לפי שם הספר"
          value={filters.title}
          onChange={handleFilterChange}
        />
        <input
          className="catalog-search__input"
          type="text"
          name="author"
          placeholder="חיפוש לפי מחבר"
          value={filters.author}
          onChange={handleFilterChange}
        />
        <select
          className="catalog-search__select"
          name="category"
          value={filters.category}
          onChange={handleFilterChange}
        >
          <option value="">כל הקטגוריות</option>
          {CATEGORY_OPTIONS.map((opt) => (
            <option key={opt.value} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
        <button type="submit" className="catalog-search__btn">
          חיפוש
        </button>
        <button
          type="button"
          className="catalog-search__btn catalog-search__btn--ghost"
          onClick={handleClearSearch}
        >
          איפוס
        </button>
      </form>

      {feedback && <div className="catalog-feedback">{feedback}</div>}
      {error && <div className="catalog-error">{error}</div>}

      {loading ? (
        <div className="catalog-status">טוען ספרים...</div>
      ) : books.length === 0 ? (
        <div className="catalog-status">לא נמצאו ספרים התואמים את החיפוש.</div>
      ) : (
        <div className="catalog-grid">
          {books.map((book) => (
            <BookCard key={book.id} book={book} onRequestLoan={handleRequestLoan} />
          ))}
        </div>
      )}
    </div>
  );
}

export default BookCatalog;
