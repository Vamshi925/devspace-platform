import apiClient from "./apiClient";

export const loginUser = async (email, password) => {
  const response = await apiClient.post("/api/auth/login", {
    email,
    password,
  });

  return response.data;
};
