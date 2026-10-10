import apiClient from "./apiClient";

export const updateUserRole = async (email, role) => {
  const response = await apiClient.patch(
    "/api/admin/users/role",
    {
      email,
      role,
    }
  );

  return response.data;
};