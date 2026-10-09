import { createContext, useContext, useMemo, useState } from "react";

const AuthContext = createContext(null);

function decodeToken(token) {
  try {
    const payload = token.split(".")[1];
    const decoded = atob(
      payload.replace(/-/g, "+").replace(/_/g, "/")
    );

    return JSON.parse(decoded);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(
    localStorage.getItem("token")
  );

  const user = useMemo(() => {
    if (!token) return null;

    const payload = decodeToken(token);

    if (!payload) return null;

    return {
      userId: payload.userId,
      role: payload.role,
      email: payload.sub,
    };
  }, [token]);

  const login = (newToken) => {
    localStorage.setItem("token", newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        login,
        logout,
        isAuthenticated: !!token,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
