const TOKEN_KEY = "ismes_token";
const USER_KEY = "ismes_user";
const clearListeners = new Set();

function isUser(user) {
  return user && ["username", "fullName", "role"].every(
    (field) => typeof user[field] === "string" && user[field].trim().length > 0
  );
}

function isToken(token) {
  return typeof token === "string" && token.trim().length > 0 &&
    token !== "undefined" && token !== "null";
}

export function readSession(storage) {
  try {
    storage ??= globalThis.localStorage;
    const token = storage.getItem(TOKEN_KEY);
    const savedUser = storage.getItem(USER_KEY);
    if (token === null && savedUser === null) return null;

    const user = savedUser ? JSON.parse(savedUser) : null;
    if (isToken(token) && isUser(user)) return { token, user };
  } catch {
    // Old or incomplete browser state must never prevent the login page loading.
  }

  clearSession(storage);
  return null;
}

export function saveSession(token, user, storage) {
  if (!isToken(token) || !isUser(user)) {
    throw new Error("The server returned an invalid sign-in response. Please try again.");
  }

  try {
    storage ??= globalThis.localStorage;
    storage.setItem(TOKEN_KEY, token);
    storage.setItem(USER_KEY, JSON.stringify(user));
  } catch {
    clearSession(storage);
    throw new Error("Could not save your session. Enable browser storage and try again.");
  }
}

export function clearSession(storage) {
  try {
    storage ??= globalThis.localStorage;
    storage.removeItem(TOKEN_KEY);
    storage.removeItem(USER_KEY);
  } catch {
    // In-memory authentication still needs to clear when storage is unavailable.
  }
  for (const listener of clearListeners) listener();
}

export function onSessionCleared(listener) {
  clearListeners.add(listener);
  return () => clearListeners.delete(listener);
}
