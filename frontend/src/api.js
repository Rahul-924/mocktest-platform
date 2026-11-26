import axios from "axios";

// One configured axios instance used by every page.
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080" });

// Attach the JWT to every request.
api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// If the token expired or is invalid, clear it and go to the login page.
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const isAuthCall = err.config?.url?.includes("/api/auth/");
    if (err.response?.status === 401 && !isAuthCall) {
      sessionStorage.clear();
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export const errMsg = (e) => e.response?.data?.message || "Something went wrong. Please try again.";

export default api;
