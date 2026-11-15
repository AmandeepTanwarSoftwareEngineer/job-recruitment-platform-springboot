import { useState } from "react";
import { clearSession, loadSession, saveSession } from "./api";
import Home from "./components/Home";
import LoginForm from "./components/LoginForm";
import RegisterForm from "./components/RegisterForm";

export default function App() {
  // The signed-in user, or null. Read from localStorage once, when the page loads.
  const [user, setUser] = useState(() => loadSession()?.user ?? null);
  const [tab, setTab] = useState("login");
  // Email of the account just created; the sign-in form shows a success note and fills it in.
  const [registeredEmail, setRegisteredEmail] = useState("");

  function showTab(name) {
    setTab(name);
    setRegisteredEmail("");
  }

  function handleRegistered(email) {
    setTab("login");
    setRegisteredEmail(email);
  }

  // data is the login response: { message, user, token }.
  function handleLogin(data) {
    saveSession(data.token, data.user);
    setUser(data.user);
  }

  function handleLogout() {
    clearSession();
    setUser(null);
    showTab("login");
  }

  return (
    <main className="card">
      <h1 className="brand">Job Portal</h1>

      {user ? (
        <Home user={user} onLogout={handleLogout} />
      ) : (
        <>
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
