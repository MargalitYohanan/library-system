import { bookAndLoanApi as api } from './axios';

// כל הנתיבים תואמים בדיוק ל-BookController בשרת (@RequestMapping("/api/books"))
// axios.js מוגדר עם baseURL: http://localhost:9000/api , לכן כאן כותבים רק את החלק שאחרי /api

// GET /api/books/ - שליפת כל הספרים
export const getAllBooks = async () => {
  const response = await api.get('/books/');
  return response.data;
};

// GET /api/books/{id} - שליפת ספר לפי מזהה
export const getBookById = async (id) => {
  const response = await api.get(`/books/${id}`);
  return response.data;
};

// GET /api/books/search?title=&author=&category= - חיפוש ספרים
export const searchBooks = async ({ title, author, category } = {}) => {
  const params = {};
  if (title) params.title = title;
  if (author) params.author = author;
  if (category) params.category = category;

  const response = await api.get('/books/search', { params });
  return response.data;
};

// POST /api/books/addBook - הוספת ספר חדש
export const addBook = async (book) => {
  const response = await api.post('/books/addBook', book);
  return response.data;
};

// PUT /api/books/updateBook - עדכון ספר קיים
export const updateBook = async (book) => {
  const response = await api.put('/books/updateBook', book);
  return response.data;
};

// PATCH /api/books/updateAvailability/{id}?isAvailable=true|false
export const updateAvailability = async (id, isAvailable) => {
  const response = await api.patch(`/books/updateAvailability/${id}`, null, {
    params: { isAvailable },
  });
  return response.data;
};

// DELETE /api/books/removeBook/{id} - מחיקת ספר
export const removeBook = async (id) => {
  const response = await api.delete(`/books/removeBook/${id}`);
  return response.data;
};

// GET /api/books/getBooksByOwner/{ownerId} - כל הספרים בבעלות משתמש מסוים
export const getAllBooksByOwnerId = async (ownerId) => {
  const response = await api.get(`/books/getBooksByOwner/${ownerId}`);
  return response.data;
};

// GET /api/books/getBooksBorrowedByUser/{borrowerId} - ספרים ששאל משתמש מסוים
export const getBooksBorrowedByUser = async (borrowerId) => {
  const response = await api.get(`/books/getBooksBorrowedByUser/${borrowerId}`);
  return response.data;
};
