import assert from "node:assert/strict";
import test from "node:test";
import { clearSession, onSessionCleared, readSession, saveSession } from "../src/lib/session.js";

const user = { username: "admin", fullName: "Test Admin", role: "ADMIN" };

function memoryStorage(entries = {}) {
  const values = new Map(Object.entries(entries));
  return {
    getItem: (key) => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, value),
    removeItem: (key) => values.delete(key),
  };
}

test("a complete saved session survives a page reload", () => {
  const storage = memoryStorage();
  saveSession("valid-token", user, storage);
  assert.deepEqual(readSession(storage), { token: "valid-token", user });
});

test("corrupt, incomplete, and invalid sessions are cleared instead of crashing", () => {
  const invalidSessions = [
    { ismes_token: "token", ismes_user: "{broken json" },
    { ismes_user: JSON.stringify(user) },
    { ismes_token: "token" },
    { ismes_token: "undefined", ismes_user: JSON.stringify(user) },
    { ismes_token: "token", ismes_user: JSON.stringify({ username: "admin" }) },
    { ismes_token: "", ismes_user: "" },
  ];
  for (const entry of invalidSessions) {
    const storage = memoryStorage(entry);
    assert.equal(readSession(storage), null);
    assert.equal(storage.getItem("ismes_token"), null);
    assert.equal(storage.getItem("ismes_user"), null);
  }
});

test("clearing a session notifies the in-memory auth subscriber and supports cleanup", () => {
  const storage = memoryStorage();
  let currentUser = user;
  const unsubscribe = onSessionCleared(() => { currentUser = null; });
  clearSession(storage);
  assert.equal(currentUser, null);
  unsubscribe();
  currentUser = user;
  clearSession(storage);
  assert.equal(currentUser, user);
});

test("unavailable browser storage does not prevent a signed-out page from loading", () => {
  const storage = {
    getItem() { throw new Error("Storage blocked"); },
    removeItem() { throw new Error("Storage blocked"); },
  };
  assert.equal(readSession(storage), null);
});

test("partial storage writes are rolled back and report a useful error", () => {
  const storage = memoryStorage();
  const originalSetItem = storage.setItem;
  storage.setItem = (key, value) => {
    if (key === "ismes_user") throw new Error("Quota exceeded");
    originalSetItem(key, value);
  };
  assert.throws(() => saveSession("token", user, storage), /Enable browser storage/);
  assert.equal(storage.getItem("ismes_token"), null);
  assert.equal(storage.getItem("ismes_user"), null);
});

test("invalid login responses never become saved sessions", () => {
  const storage = memoryStorage();
  assert.throws(() => saveSession(undefined, user, storage), /invalid sign-in response/);
  assert.equal(readSession(storage), null);
});
