export const getErrorMessage = (
  error,
  fallback = "Something went wrong"
) => {
  return (
    error?.userMessage ||
    error?.response?.data?.message ||
    fallback
  );
};
