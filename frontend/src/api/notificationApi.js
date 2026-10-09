import apiClient from "./apiClient";

export const getMyNotifications = async () => {
  const response = await apiClient.get("/api/notifications/my");
  return response.data;
};

export const markNotificationAsRead = async (notificationId) => {
  const response = await apiClient.patch(
    `/api/notifications/${notificationId}/read`
  );

  return response.data;
};
