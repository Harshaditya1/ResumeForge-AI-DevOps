import { createContext, useContext, useState } from "react";
import {
  getToken,
  saveToken,
  removeToken,
} from "../utils/authStorage";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => getToken());
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getToken());
  const loading = false;

  const login = (jwtToken, userData = null) => {
    saveToken(jwtToken);

    setToken(jwtToken);
    setUser(userData);
    setIsAuthenticated(true);
  };

  const logout = () => {
    removeToken();

    setToken(null);
    setUser(null);
    setIsAuthenticated(false);
  };

  const value = {
    user,
    token,
    isAuthenticated,
    loading,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
}