import { aiApi as recApi } from './axios';

// שליפת המלצות לפי מזהה משתמש
export const fetchRecommendation = async (userId) => {
    try {
        const response = await recApi.get(`/${userId}`);
        return response.data;
    } catch (error) {
        if (error.response && error.response.status === 404) return null;
        throw error;
    }
};

// יצירת המלצה חדשה למשתמש
export const createRecommendation = async (userId) => {
    const response = await recApi.post(`/add/${userId}`);
    return response.data;
};

// מחיקת המלצות משתמש
export const deleteRecommendation = async (userId) => {
    const response = await recApi.delete(`/${userId}`);
    return response.data;
};