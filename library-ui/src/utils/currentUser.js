import { getProfile } from '../api/userApi';

export const fetchCurrentUserId = async () => {
  const res = await getProfile();
  const profile = res.data;

  const id = profile?.id ?? profile?.userId;

  if (id === undefined || id === null) {
    throw new Error('לא ניתן לזהות את מזהה המשתמש המחובר מתוך הפרופיל.');
  }

  return id;
};
