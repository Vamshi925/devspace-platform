import axios from "axios";

const apiClient = axios.create({
  baseURL: "",
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (!error.response) {
      error.userMessage = "Unable to connect to DevSpace services.";
      return Promise.reject(error);
    }

    const status = error.response.status;
    const url = error.config?.url || "";
    const isPublicAuthRequest =
      url.includes("/api/auth/login") ||
      url.includes("/api/auth/register");

    if (status === 401) {
      if (!isPublicAuthRequest) {
        localStorage.removeItem("token");

        if (window.location.pathname !== "/") {
          window.location.href = "/";
        }
      }

      error.userMessage =
        error.response?.data?.message ||
        (isPublicAuthRequest
          ? "Invalid email or password."
          : "Your session has expired. Please sign in again.");
    } else if (status === 403) {
      error.userMessage =
        error.response?.data?.message ||
        "You do not have permission to perform this action.";
    } else if (status === 404) {
      error.userMessage =
        error.response?.data?.message ||
        "The requested resource was not found.";
    } else if (status >= 500) {
      error.userMessage =
        error.response?.data?.message ||
        "DevSpace is temporarily unavailable. Please try again.";
    } else {
      error.userMessage =
        error.response?.data?.message ||
        "Unable to complete the request.";
    }

    return Promise.reject(error);
  }
);

export default apiClient;
