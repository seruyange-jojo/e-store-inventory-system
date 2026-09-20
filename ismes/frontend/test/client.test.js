import assert from "node:assert/strict";
import test from "node:test";
import { AxiosError } from "axios";
import { apiClient } from "../src/api/client.js";
import { onSessionCleared, readSession, saveSession } from "../src/lib/session.js";
import { loginErrorMessage } from "../src/lib/loginError.js";

function installStorage(t) {
  const values = new Map();
  const original = Object.getOwnPropertyDescriptor(globalThis, "localStorage");
  Object.defineProperty(globalThis, "localStorage", {
    configurable: true,
    value: {
      getItem: (key) => values.get(key) ?? null,
      setItem: (key, value) => values.set(key, value),
      removeItem: (key) => values.delete(key),
    },
  });
  t.after(() => {
    if (original) Object.defineProperty(globalThis, "localStorage", original);
    else delete globalThis.localStorage;
  });
  saveSession("existing-token", { username: "admin", fullName: "Test Admin", role: "ADMIN" });
}

test("login never sends an old bearer token, even if a header was supplied", async (t) => {
  installStorage(t);
  await apiClient.post("/auth/login", { username: "admin", password: "test" }, {
    headers: { Authorization: "Bearer stale-token" },
    adapter: async (config) => {
      assert.equal(config.headers.get("Authorization"), undefined);
      return { status: 200, data: {}, headers: {}, config };
    },
  });
});

test("authenticated requests use the saved token and a 401 clears storage and auth state", async (t) => {
  installStorage(t);
  let authenticated = true;
  t.after(onSessionCleared(() => { authenticated = false; }));
  await assert.rejects(apiClient.get("/dashboard/summary", {
    adapter: async (config) => {
      assert.equal(config.headers.get("Authorization"), "Bearer existing-token");
      throw new AxiosError("Unauthorized", "ERR_BAD_REQUEST", config, null, {
        status: 401, data: {}, headers: {}, config,
      });
    },
  }));
  assert.equal(readSession(), null);
  assert.equal(authenticated, false);
});

test("connection failures, incorrect credentials, and server failures have distinct messages", () => {
  assert.match(loginErrorMessage(new AxiosError("Network Error", "ERR_NETWORK")), /connect to the server/);
  assert.match(loginErrorMessage({ isAxiosError: true, response: { status: 401 } }), /Incorrect username or password/);
  assert.equal(loginErrorMessage({ isAxiosError: true, response: { status: 503 } }), "Could not sign in. Please try again later.");
});
