import {
  createContext,
  useContext,
  useMemo,
  useState,
} from "react";

const AuthContext = createContext(null);

function decodeToken(token) {
  try {
    const payload = JSON.parse(
      atob(token.split(".")[1])
    );

    return {
      userId: payload.userId,
      email: payload.sub,
      role: payload.role,
    };
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(
    localStorage.getItem("token")
  );

  const user = useMemo(() => {
    if (!token) {
      return null;
    }

    return decodeToken(token);
  }, [token]);

  const login = (newToken) => {
    localStorage.setItem(
      "token",
      newToken
    );

    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem("token");
    setToken(null);

    window.location.href = "/";
  };

  const value = {
    token,
    user,
    login,
    logout,
    isAuthenticated: !!token,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}