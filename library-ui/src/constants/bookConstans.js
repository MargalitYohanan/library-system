// קבועים ותרגומים לערכי ה-enum שמגיעים מהשרת

export const CATEGORY_LABELS = {
  RECIPES: 'מתכונים',
  COMICS: 'קומיקס',
  KODESH: 'קודש',
  ENGLISH: 'אנגלית',
  KNOWLEDGE: 'ידע',
  STUDY: 'לימוד',
  READING: 'קריאה',
  MEDICINE: 'רפואה',
  ART: 'אמנות',
  MUSIC: 'מוזיקה',
  BUSINESS: 'עסקים',
  PSYCHOLOGY: 'פסיכולוגיה',
};

export const TARGET_AGE_LABELS = {
  YOUTH: 'נוער',
  ADULTS: 'מבוגרים',
  CHILDREN: 'ילדים',
  TODDLERS: 'פעוטות',
};

export const LOAN_STATUS_LABELS = {
  PENDING_APPROVAL: 'ממתין לאישור',
  REJECTED: 'נדחה',
  LOANED: 'מושאל',
  RETURNED: 'הוחזר',
};

export const CATEGORY_OPTIONS = Object.entries(CATEGORY_LABELS).map(
  ([value, label]) => ({ value, label })
);

export const TARGET_AGE_OPTIONS = Object.entries(TARGET_AGE_LABELS).map(
  ([value, label]) => ({ value, label })
);
