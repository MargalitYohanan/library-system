import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getBookById } from '../../api/bookApi';
import { requestLoan } from '../../api/loanApi';
import { fetchCurrentUserId } from '../../utils/currentUser';
import {
  CATEGORY_LABELS,
  TARGET_AGE_LABELS,
} from '../../constants/bookConstans';
import './BookDetails.css';

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="320" height="420" viewBox="0 0 320 420">
      <rect width="320" height="420" fill="#EDE6D8"/>
      <rect x="40" y="50" width="240" height="320" rx="4" fill="#D8CBAE"/>
      <rect x="40" y="50" width="18" height="320" fill="#C2AE82"/>
      <text x="160" y="220" font-family="Arial" font-size="18" fill="#8A7B5C" text-anchor="middle">אין תמונה</text>
    </svg>
  `);

function BookDetails() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState('');
  const [requesting, setRequesting] = useState(false);
  const [currentUserId, setCurrentUserId] = useState(null);

  useEffect(() => {
    fetchCurrentUserId()
      .then(setCurrentUserId)
      .catch(() => {
        /* המשתמש עדיין יכול לצפות בפרטי הספר גם אם הזיהוי נכשל */
      });
  }, []);

  useEffect(() => {
    let isMounted = true;

    const loadBook = async () => {
      setLoading(true);
      setError('');
      try {
        const data = await getBookById(id);
        if (isMounted) setBook(data);
      } catch (err) {
        if (isMounted) setError('לא ניתן לטעון את פרטי הספר.');
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    loadBook();
    return () => {
      isMounted = false;
    };
  }, [id]);

  const handleRequestLoan = async () => {
    if (!book || !book.available) return;
    if (!currentUserId) {
      setFeedback('לא ניתן לזהות את המשתמש המחובר. נסו לרענן את הדף.');
      return;
    }
    if (book.ownerId === currentUserId) {
      setFeedback('לא ניתן להשאיל ספר שבבעלותך.');
      return;
    }
    setRequesting(true);
    setFeedback('');
    try {
      await requestLoan(book.id, currentUserId);
      setFeedback('בקשת ההשאלה נשלחה בהצלחה לבעל הספר.');
      setBook((prev) => ({ ...prev, available: false }));
    } catch (err) {
      const serverMessage = err?.response?.data;
      setFeedback(
        typeof serverMessage === 'string'
          ? serverMessage
          : 'שליחת בקשת ההשאלה נכשלה. נסו שוב.'
      );
    } finally {
      setRequesting(false);
    }
  };

  if (loading) {
    return <div className="book-details book-details--status">טוען פרטי ספר...</div>;
  }

  if (error || !book) {
    return (
      <div className="book-details book-details--status">
        <p>{error || 'הספר לא נמצא.'}</p>
        <button className="book-details__back" onClick={() => navigate(-1)}>
          חזרה
        </button>
      </div>
    );
  }

  const isAvailable = !!book.available;

  return (
    <div className="book-details">
      <button className="book-details__back" onClick={() => navigate(-1)}>
        → חזרה לקטלוג
      </button>

      <div className="book-details__content">
        <div className="book-details__image-wrap">
          <img
            className={`book-details__image ${!isAvailable ? 'book-details__image--gray' : ''}`}
            src={book.imageUrl || PLACEHOLDER_IMAGE}
            alt={book.title}
          />
        </div>

        <div className="book-details__info">
          <span
            className={`book-details__status ${
              isAvailable ? 'book-details__status--available' : 'book-details__status--unavailable'
            }`}
          >
            {isAvailable ? 'זמין להשאלה' : 'לא זמין'}
          </span>

          <h1 className="book-details__title">{book.title}</h1>
          <p className="book-details__author">מאת {book.author}</p>

          <dl className="book-details__meta">
            <div className="book-details__meta-row">
              <dt>קטגוריה</dt>
              <dd>{CATEGORY_LABELS[book.category] || book.category}</dd>
            </div>
            <div className="book-details__meta-row">
              <dt>גיל יעד</dt>
              <dd>{TARGET_AGE_LABELS[book.targetAge] || book.targetAge}</dd>
            </div>
            <div className="book-details__meta-row">
              <dt>מצב הספר</dt>
              <dd>{book.bookCondition}</dd>
            </div>
            <div className="book-details__meta-row">
              <dt>משך השאלה</dt>
              <dd>{book.loanDurationDays} ימים</dd>
            </div>
          </dl>

          {book.description && (
            <p className="book-details__description">{book.description}</p>
          )}

          {feedback && <div className="book-details__feedback">{feedback}</div>}

          <button
            className="book-details__request-btn"
            disabled={!isAvailable || requesting}
            onClick={handleRequestLoan}
          >
            {requesting ? 'שולח בקשה...' : isAvailable ? 'בקש השאלה' : 'הספר אינו זמין'}
          </button>
        </div>
      </div>
    </div>
  );
}

export default BookDetails;
