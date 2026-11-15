import { useState } from "react";
import { clearSession, loadSession, saveSession } from "./api";
import Home from "./components/Home";
import LoginForm from "./components/LoginForm";
import RegisterForm from "./components/RegisterForm";

export default function App() {
  // { token, user } of the signed-in user, or null. Read from localStorage once, when the page loads.
  const [session, setSession] = useState(() => loadSession());
  const [tab, setTab] = useState("login");
  // Email of the account just created; the sign-in form shows a success note and fills it in.
  const [registeredEmail, setRegisteredEmail] = useState("");
  // True when the backend rejected the token (it lasts 1 hour), so we explain why they were signed out.
  const [sessionExpired, setSessionExpired] = useState(false);

  function showTab(name) {
    setTab(name);
    setRegisteredEmail("");
    setSessionExpired(false);
  }

  function handleRegistered(email) {
    setTab("login");
    setRegisteredEmail(email);
  }

  // data is the login response: { message, user, token }.
  function handleLogin(data) {
    saveSession(data.token, data.user);
    setSession({ token: data.token, user: data.user });
    setSessionExpired(false);
  }

  function handleLogout() {
    clearSession();
    setSession(null);
    showTab("login");
  }

  function handleSessionExpired() {
    handleLogout();
    setSessionExpired(true);
  }

  return (
    <main className="card">
      <h1 className="brand">Job Portal</h1>

      {session ? (
        <Home
          user={session.user}
          token={session.token}
          onLogout={handleLogout}
          onSessionExpired={handleSessionExpired}
        />
      ) : (
        <>
          {sessionExpired && (
            <p className="message error notice">Your session expired. Please sign in again.</p>
          )}

          <div className="tabs">
            <button
              type="button"
              className={tab === "login" ? "tab active" : "tab"}
              onClick={() => showTab("login")}
            >
              Sign in
            </button>
            <button
              type="button"
              className={tab === "register" ? "tab active" : "tab"}
              onClick={() => showTab("register")}
            >
              Register
            </button>
          </div>

          {tab === "login" ? (
            <LoginForm registeredEmail={registeredEmail} onLogin={handleLogin} />
          ) : (
            <RegisterForm onRegistered={handleRegistered} />
          )}
        </>
      )}
    </main>
  );
}
