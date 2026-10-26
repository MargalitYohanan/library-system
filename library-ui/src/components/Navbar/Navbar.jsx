import { useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import { logout } from "../../api/authApi";
import NotificationBell from "../NotificationBell/NotificationBell";
import "./Navbar.css";

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  const [token, setToken] = useState(null);
  const [fullName, setFullName] = useState("");

  useEffect(() => {
    setToken(localStorage.getItem("token"));
    setFullName(localStorage.getItem("fullName") || "");
  }, [location]);

  const handleLogout = () => {
    logout();
    setToken(null);
    setFullName("");
    navigate("/");
  };

  const initial = fullName ? fullName.trim().charAt(0) : "";

  return (
    <header className="navbar">
      <div className="navbar__logo" onClick={() => navigate("/main")}>
        <span className="navbar__logo-icon">📚</span>
        <span>ספריית השכונה</span>
      </div>

      <div className="navbar__actions">
        {token ? (
          <>
            <NotificationBell />

            <div className="navbar__user">
              <span className="navbar__avatar">{initial}</span>
              <span className="navbar__user-name">{fullName}</span>
            </div>

            <button className="navbar__btn navbar__btn--logout" onClick={handleLogout}>
              התנתקות
            </button>
          </>
        ) : (
          <>
            <button className="navbar__btn" onClick={() => navigate("/")}>
              התחברות
            </button>
            <button className="navbar__btn navbar__btn--primary" onClick={() => navigate("/register")}>
              הרשמה
            </button>
          </>
        )}
      </div>
    </header>
  );
}
