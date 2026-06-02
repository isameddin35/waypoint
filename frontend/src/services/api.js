import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default api;

export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

export const routeApi = {
  getAll: (params) => api.get('/routes', { params }),
  getById: (id) => api.get(`/routes/${id}`),
  create: (data) => api.post('/routes', data),
  update: (id, data) => api.put(`/routes/${id}`, data),
  delete: (id) => api.delete(`/routes/${id}`),
  getFeatured: () => api.get('/routes/featured'),
  getCount: () => api.get('/routes/count'),
  getUserRoutes: () => api.get('/user/routes'),
};

export const routePointApi = {
  getByRoute: (routeId) => api.get(`/routes/${routeId}/points`),
  addBatch: (routeId, data) => api.post(`/routes/${routeId}/points/batch`, data),
  fetchElevation: (routeId) => api.post(`/routes/${routeId}/points/elevation/fetch`),
};

export const reviewApi = {
  getByRoute: (routeId) => api.get(`/routes/${routeId}/reviews`),
  create: (routeId, data) => api.post(`/routes/${routeId}/reviews`, data),
  delete: (routeId, reviewId) => api.delete(`/routes/${routeId}/reviews/${reviewId}`),
};

export const photoApi = {
  getByRoute: (routeId) => api.get(`/routes/${routeId}/photos`),
  upload: (routeId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/routes/${routeId}/photos`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },
};

export const wildlifeApi = {
  getAll: () => api.get('/wildlife'),
  getByRoute: (routeId) => api.get(`/routes/${routeId}/wildlife`),
  create: (data) => api.post('/wildlife', data),
};

export const favoriteApi = {
  toggle: (routeId) => api.post(`/routes/${routeId}/favorite`),
  check: (routeId) => api.get(`/routes/${routeId}/favorite`),
  getAll: () => api.get('/favorites'),
};

export const adminApi = {
  getUsers: () => api.get('/admin/users'),
  getRoutes: () => api.get('/admin/routes'),
  getReports: () => api.get('/admin/reports'),
  getStats: () => api.get('/admin/stats'),
};
