export function loginErrorMessage(error) {
  if (error.isAxiosError && !error.response) {
    return "Could not connect to the server. Check that the backend is running and try again.";
  }
  if (error.response?.status === 401) {
    return "Incorrect username or password. Please try again.";
  }
  const serverMessage = error.response?.data?.message;
  if (typeof serverMessage === "string" && serverMessage.trim()) return serverMessage;
  if (!error.isAxiosError && error.message) return error.message;
  return "Could not sign in. Please try again later.";
}
