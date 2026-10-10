import apiClient from "./apiClient";

export const loginUser = async (
  email,
  password
) => {
  const response = await apiClient.post(
    "/api/auth/login",
    {
      email,
      password,
    }
  );

  return response.data;
};

export const registerUser = async (request) => {
  const response = await apiClient.post(
    "/api/auth/register",
    request
  );

  return response.data;
};