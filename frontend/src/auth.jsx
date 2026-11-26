import { createContext, useContext, useState } from "react";

const AuthContext = createContext(null);

// The token lives in sessionStorage (cleared when the tab closes). Trade-off: any XSS bug could read it;
// the safer production option is an httpOnly cookie. See README "Security notes".
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = sessionStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  });

  const login = (data) => {
    sessionStorage.setItem("token", data.token);
    const u = { name: data.name, role: data.role };
    sessionStorage.setItem("user", JSON.stringify(u));
    setUser(u);
  };

  const logout = () => {
    sessionStorage.clear();
    setUser(null);
  };

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
