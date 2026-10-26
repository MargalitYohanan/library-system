import { userApi as api } from "./axios";

export const getProfile = () => api.get('/users/profile');

export const updateProfile = (data) => api.put('/users/profile', data);