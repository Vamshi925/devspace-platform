import apiClient from "./apiClient";

export const getTemplates = async () => {
  const response = await apiClient.get("/api/templates");
  return response.data;
};

export const getTemplateById = async (templateId) => {
  const response = await apiClient.get(
    `/api/templates/${templateId}`
  );

  return response.data;
};

export const createTemplate = async (request) => {
  const response = await apiClient.post(
    "/api/templates",
    request
  );

  return response.data;
};

export const activateTemplate = async (templateId) => {
  const response = await apiClient.patch(
    `/api/templates/${templateId}/activate`
  );

  return response.data;
};

export const deactivateTemplate = async (templateId) => {
  const response = await apiClient.patch(
    `/api/templates/${templateId}/deactivate`
  );

  return response.data;
};