import axios from "axios";

const GET_CACHE_TTL_MS = 3000;
const getCache = new Map();
const pendingGets = new Map();
let cacheVersion = 0;

const getBaseUrl = () => {
  if (import.meta.env.VITE_API_BASE_URL && import.meta.env.VITE_API_BASE_URL.trim() !== "") {
    return import.meta.env.VITE_API_BASE_URL.trim().replace(/\/+$/, "");
  }
  if (typeof window !== "undefined" && window.location.port === "3000") {
    return "";
  }
  return "http://localhost:8082";
};

const apiClient = axios.create({
  baseURL: getBaseUrl(),
  timeout: 15000,
  headers: {
    "Content-Type": "application/json",
  },
});

export const clearGetCache = () => {
  cacheVersion += 1;
  getCache.clear();
};

export const getCached = async (url, config = {}) => {
  const token = localStorage.getItem("rf_access_token") || "";
  const key = `${token}:${url}:${JSON.stringify(config.params || {})}`;
  const now = Date.now();
  const cached = getCache.get(key);

  if (cached && cached.expiresAt > now) {
    return cached.data;
  }

  const pending = pendingGets.get(key);
  if (pending) {
    return pending;
  }

  const requestVersion = cacheVersion;
  const request = apiClient
    .get(url, config)
    .then((response) => {
      if (requestVersion === cacheVersion) {
        getCache.set(key, {
          data: response.data,
          expiresAt: Date.now() + GET_CACHE_TTL_MS,
        });
      }
      return response.data;
    })
    .finally(() => {
      pendingGets.delete(key);
    });

  pendingGets.set(key, request);
  return request;
};

apiClient.interceptors.request.use(
  (config) => {
    if (config.method && config.method.toLowerCase() !== "get") {
      clearGetCache();
    }

    const token = localStorage.getItem("rf_access_token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.code === "ECONNABORTED" || error?.message?.includes("timeout")) {
      error.message = "Connection timed out. Please try again.";
    } else if (error.message === "Network Error" || !error.response) {
      error.message = "Unable to connect to the server. Please ensure the backend is running.";
    }
    return Promise.reject(error);
  }
);

export default apiClient;