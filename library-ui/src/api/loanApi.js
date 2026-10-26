import { bookAndLoanApi as api } from './axios';

/* =========================================================================
   חלק א' - נתיבים שכבר קיימים ב-LoanController כפי שהוא היום
   ========================================================================= */

// GET /api/loans/ - כל ההשאלות
export const getAllLoans = async () => {
  const response = await api.get('/loans/');
  return response.data;
};

// GET /api/loans/{id} - השאלה לפי מזהה
export const getLoanById = async (id) => {
  const response = await api.get(`/loans/${id}`);
  return response.data;
};

// POST /api/loans/requestLoan - יצירת בקשת השאלה
// LoanDTO דורש bookId ו-borrowerID (לא borrowerId!) - אות ID גדולה בסוף, כמו בשרת.
export const requestLoan = async (bookId, borrowerID) => {
  const response = await api.post('/loans/requestLoan', { bookId, borrowerID });
  return response.data;
};

// DELETE /api/loans/deleteLoan/{id}
export const deleteLoan = async (id) => {
  const response = await api.delete(`/loans/deleteLoan/${id}`);
  return response.data;
};

/* =========================================================================
   חלק ב' - פעולות אישור/דחייה/החזרה של השאלה
   ========================================================================= */

export const approveLoan = async (loanId, currentUserId) => {
  const response = await api.patch(`/loans/${loanId}/approve`, null, {
    headers: { userId: currentUserId },
  });
  return response.data;
};

export const rejectLoan = async (loanId, currentUserId) => {
  const response = await api.patch(`/loans/${loanId}/reject`, null, {
    headers: { userId: currentUserId },
  });
  return response.data;
};

// PATCH /api/loans/{id}/return
export const returnBook = async (loanId) => {
  const response = await api.patch(`/loans/${loanId}/return`);
  return response.data;
};

// GET /api/loans/pending-requests/{ownerId}
export const getRequestsByOwnerId = async (ownerId) => {
  const response = await api.get(`/loans/pending-requests/${ownerId}`);
  return response.data;
};

export const getLoansByOwner = async (ownerId) => {
  const response = await api.get(`/loans/findLoansByOwnerId/${ownerId}`);
  return response.data;
};

// GET /api/loans/getBorrowedBooksByOwnerId/{ownerId}
export const getBorrowedBooksByOwner = async (ownerId) => {
  const response = await api.get(`/loans/getBorrowedBooksByOwnerId/${ownerId}`);
  return response.data;
};
