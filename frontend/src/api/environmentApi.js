import apiClient from "./apiClient";

export const getMyEnvironments = async () => {
  const response = await apiClient.get("/api/environments/my");
  return response.data;
};

export const getEnvironmentById = async (environmentId) => {
  const response = await apiClient.get(
    `/api/environments/${environmentId}`
  );

  return response.data;
};

export const getEnvironmentActivity = async (environmentId) => {
  const response = await apiClient.get(
    `/api/environments/${environmentId}/activity`
  );

  return response.data;
};

export const extendEnvironment = async (
  environmentId,
  additionalHours
) => {
  const response = await apiClient.patch(
    `/api/environments/${environmentId}/extend`,
    { additionalHours }
  );

  return response.data;
};

export const deleteEnvironment = async (environmentId) => {
  const response = await apiClient.delete(
    `/api/environments/${environmentId}`
  );

  return response.data;
};

export const createEnvironment = async (request) => {
  const response = await apiClient.post(
    "/api/environments",
    request
  );

  return response.data;
};
