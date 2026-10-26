// src/pages/AiRecommendations/AiRecommendations.jsx
import React, { useState, useEffect, useCallback } from "react";
import BookCard from "../../components/BookCard/BookCard";
import { fetchRecommendation, createRecommendation, deleteRecommendation } from "../../api/recommendationsApi";
import { getBookById } from "../../api/bookApi";
import "./AiRecommendations.css";

function AiRecommendations() {
 
  const userId = localStorage.getItem("userId");

  const [rec, setRec] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState(null);
  const [notFound, setNotFound] = useState(false);

  const [books, setBooks] = useState([]);
  const [booksLoading, setBooksLoading] = useState(false);

  const loadRecommendation = useCallback(async (id) => {
    if (!id) return;
    setIsLoading(true);
    setError(null);
    setNotFound(false);
    try {
      const data = await fetchRecommendation(id);
      if (!data) {
        setRec(null);
        setNotFound(true);
        return;
      }
      setRec(data);
    } catch (e) {
      setRec(null);
      setError(`לא הצלחנו לתקשר עם שרת ההמלצות. ודאו ששירות ה-AI (פורט 9090) רץ כראוי.`);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!userId) {
      setError("לא נמצא משתמש מחובר. יש להתחבר כדי לראות המלצות אישיות.");
      return;
    }
    loadRecommendation(userId);
  }, [userId, loadRecommendation]);

  useEffect(() => {
    const bookIds = rec?.bookIds ?? [];
    if (bookIds.length === 0) {
      setBooks([]);
      return;
    }
    let cancelled = false;
    setBooksLoading(true);
    Promise.allSettled(bookIds.map((id) => getBookById(id))).then((results) => {
      if (cancelled) return;
      const loaded = results
        .filter((r) => r.status === "fulfilled" && r.value)
        .map((r) => r.value);
      setBooks(loaded);
      setBooksLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [rec]);

  const handleRefresh = () => loadRecommendation(userId);

  const handleCreate = async () => {
    if (!userId) return;
    setIsLoading(true);
    setError(null);
    try {
      await createRecommendation(userId);
      await loadRecommendation(userId);
    } catch (e) {
      setError("יצירת ההמלצה נכשלה.");
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!userId) return;
    setIsLoading(true);
    setError(null);
    try {
      await deleteRecommendation(userId);
      setRec(null);
      setNotFound(true);
    } catch (e) {
      setError("מחיקת ההמלצה נכשלה.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="ai-container">
      <div className="toolbar-panel">
        <div className="action-buttons">
          <button onClick={handleRefresh} disabled={isLoading || !userId} className="btn-action primary">
            {isLoading ? "טוען..." : "רענן"}
          </button>
          <button onClick={handleCreate} disabled={isLoading || !userId} className="btn-action">
            צור המלצה
          </button>
          <button onClick={handleDelete} disabled={isLoading || !userId} className="btn-action danger">
            מחק המלצה
          </button>
        </div>
      </div>

      <hr style={{ margin: '20px 0', borderColor: '#e2e8f0' }} />

      <main className="main-content">
        {error && <div className="error-box">{error}</div>}

        {rec ? (
          <>
            <section className="reason-card">
              <h3>למה בחרנו לך את הספרים האלו?</h3>
              <p className="reason-text"><strong>{rec.reasonForChoice || "(לא סיפקו סיבה)"}</strong></p>
            </section>

            <section style={{ marginTop: '20px' }}>
              <h3>נמצאו {rec.bookIds?.length ?? 0} המלצות עבורך:</h3>
              {booksLoading ? (
                <p>טוען פרטי ספרים...</p>
              ) : (
                <div className="books-grid">
                  {books.map((book) => (
                    <BookCard key={book.id} book={book} />
                  ))}
                </div>
              )}
              {!booksLoading && books.length < (rec.bookIds?.length ?? 0) && (
                <p style={{ fontSize: '11px', color: '#a0aec0', marginTop: '15px' }}>
                  * חלק מהספרים המומלצים לא נמצאו יותר בשירות הספרים.
                </p>
              )}
            </section>
          </>
        ) : notFound && !isLoading ? (
          <section className="empty-state">
            <h2>אין עדיין המלצות עבורך</h2>
            <p>לחצו על כפתור "צור המלצה" כדי לבקש מה-AI לנתח ולייצר רשימה חדשה.</p>
          </section>
        ) : (
          !isLoading && !error && <p>אין המלצות טעונות כרגע.</p>
        )}
      </main>
    </div>
  );
}

export default AiRecommendations;
