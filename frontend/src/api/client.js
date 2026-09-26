import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8081",
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token && !config.url?.includes("/api/auth/login")) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
