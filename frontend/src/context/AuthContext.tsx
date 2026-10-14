import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { authApi } from '../services/api';
import { SESSION_EVENT, loadSession, saveSession } from '../services/session';
import type { Session } from '../types';

interface AuthContextType {
  session: Session | null;
  login: (email: string, password: string) => Promise<void>;
  register: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<Session | null>(loadSession);

  // Follows the saved session: it also changes when the server rejects an expired token
  useEffect(() => {
    const sync = () => setSession(loadSession());
    window.addEventListener(SESSION_EVENT, sync);
    return () => window.removeEventListener(SESSION_EVENT, sync);
  }, []);

  const login = async (email: string, password: string) => saveSession(await authApi.login({ email, password }));
  const register = async (name: string, email: string, password: string) =>
    saveSession(await authApi.register({ name, email, password }));
  const logout = () => saveSession(null);

  return <AuthContext.Provider value={{ session, login, register, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}
