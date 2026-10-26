import { useNavigate } from 'react-router-dom';
import './BookCard.css';

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;utf8,' +
  encodeURIComponent(`
    <svg xmlns="http://www.w3.org/2000/svg" width="240" height="320" viewBox="0 0 240 320">
      <rect width="240" height="320" fill="#EDE6D8"/>
      <rect x="30" y="40" width="180" height="240" rx="4" fill="#D8CBAE"/>
      <rect x="30" y="40" width="14" height="240" fill="#C2AE82"/>
      <text x="120" y="170" font-family="Arial" font-size="16" fill="#8A7B5C" text-anchor="middle">אין תמונה</text>
    </svg>
  `);

function BookCard({ book, onRequestLoan }) {
  const navigate = useNavigate();
  const isAvailable = !!book.available;

  const handleCardClick = () => {
    navigate(`/books/${book.id}`);
  };

  const handleRequestClick = (e) => {
    e.stopPropagation();
    if (!isAvailable) return;
    onRequestLoan?.(book);
  };

  return (
    <div
      className={`book-card ${!isAvailable ? 'book-card--unavailable' : ''}`}
      onClick={handleCardClick}
      role="button"
      tabIndex={0}
    >
      <div className="book-card__image-wrap">
        <img
          className="book-card__image"
          src={book.imageUrl || PLACEHOLDER_IMAGE}
          alt={book.title}
        />
        {!isAvailable && (
          <span className="book-card__badge">לא זמין</span>
        )}
      </div>

      <div className="book-card__body">
        <h3 className="book-card__title">{book.title}</h3>

        <button
          className="book-card__btn"
          disabled={!isAvailable}
          onClick={handleRequestClick}
        >
          {isAvailable ? 'בקש השאלה' : 'לא זמין'}
        </button>
      </div>
    </div>
  );
}

export default BookCard;
