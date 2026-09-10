"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { api, setSessionEndedHandler } from "./api";
import { clearTokens, readTokens, writeTokens } from "./tokens";
import type { AuthResponse, User } from "./types";

type AuthStatus = "loading" | "authenticated" | "anonymous";

type AuthContextValue = {
  user: User | null;
  status: AuthStatus;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, displayName: string, password: string) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [status, setStatus] = useState<AuthStatus>(() =>
    readTokens() ? "loading" : "anonymous",
  );

  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
    setStatus("anonymous");
  }, []);

  useEffect(() => {
    setSessionEndedHandler(() => {
      setUser(null);
      setStatus("anonymous");
    });
  }, []);

  useEffect(() => {
    if (!readTokens()) return;
    let cancelled = false;
    api
      .get<User>("/auth/me")
      .then((me) => {
        if (cancelled) return;
        setUser(me);
        setStatus("authenticated");
      })
      .catch(() => {
        if (cancelled) return;
        clearTokens();
        setStatus("anonymous");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const applyAuth = (response: AuthResponse) => {
    writeTokens({ accessToken: response.accessToken, refreshToken: response.refreshToken });
    setUser(response.user);
    setStatus("authenticated");
  };

  const login = useCallback(async (username: string, password: string) => {
    const response = await api.post<AuthResponse>("/auth/login", { username, password }, false);
    applyAuth(response);
  }, []);

  const register = useCallback(
    async (username: string, displayName: string, password: string) => {
      const response = await api.post<AuthResponse>(
        "/auth/register",
        { username, displayName, password },
        false,
      );
      applyAuth(response);
    },
    [],
  );

  return (
    <AuthContext.Provider value={{ user, status, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}
