// Calls go to "/api/...". In development Vite forwards them to Spring Boot on localhost:8080
// (see vite.config.js).

const TOKEN_KEY = "token";
const USER_KEY = "user";

// POST with a JSON body. Returns { ok, status, data } (see request below).
export function postJson(url, body, token) {
  return request("POST", url, body, token);
}

// PUT with a JSON body (create-or-replace, e.g. the candidate profile).
export function putJson(url, body, token) {
  return request("PUT", url, body, token);
}

// PATCH with a JSON body (change part of something, e.g. an application's status).
export function patchJson(url, body, token) {
  return request("PATCH", url, body, token);
}

// GET. Returns { ok, status, data } (see request below).
export function getJson(url, token) {
  return request("GET", url, undefined, token);
}

// On success data is the response body (e.g. the AuthResponse { message, user, token });
// on failure it is the ApiErrorResponse ({ message, fieldErrors }).
// Pass a token for endpoints that need sign-in; it is sent as "Authorization: Bearer <token>".
// Leave it out for register and login: a bad token there would get a 401.
async function request(method, url, body, token) {
  const headers = {};
  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(url, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    const data = await response
      .json()
      .catch(() => ({ message: `Request failed (${response.status}). Is the backend running?` }));
    return { ok: response.ok, status: response.status, data };
  } catch {
    return { ok: false, status: 0, data: { message: "Could not reach the server." } };
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
