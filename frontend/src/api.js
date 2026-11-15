// Calls go to "/api/...". In development Vite forwards them to Spring Boot on localhost:8080
// (see vite.config.js).

const TOKEN_KEY = "token";
const USER_KEY = "user";

// Sends JSON and returns { ok, data }. On success data is the AuthResponse
// ({ message, user, token }); on failure it is the ApiErrorResponse ({ message, fieldErrors }).
export async function postJson(url, body) {
  try {
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    const data = await response
      .json()
      .catch(() => ({ message: `Request failed (${response.status}). Is the backend running?` }));
    return { ok: response.ok, data };
  } catch {
    return { ok: false, data: { message: "Could not reach the server." } };
  }
}

// ---------- Signed-in session, kept in localStorage so a page refresh keeps you signed in ----------

export function saveSession(token, user) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

// Returns { token, user }, or null if nobody is signed in or the token has expired (tokens last 1 hour).
export function loadSession() {
  const token = localStorage.getItem(TOKEN_KEY);
  const user = localStorage.getItem(USER_KEY);
  if (!token || !user || isExpired(token)) {
    clearSession();
    return null;
  }
  return { token, user: JSON.parse(user) };
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

// A JWT is "header.payload.signature". The payload's "exp" is the expiry time in seconds.
function isExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")));
    return payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}
