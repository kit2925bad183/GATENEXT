import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { User, Role } from '@/types';
import { apiRequest } from '@/lib/api';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<User>;
  logout: () => Promise<void>;
  refreshSession: () => Promise<void>;
  isAdmin: boolean;
  isSecurity: boolean;
  isResident: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const refreshSession = useCallback(async () => {
    try {
      setLoading(true);
      const data = await apiRequest<User>('auth/session');
      if (data && data.id) {
        setUser(data);
      } else {
        setUser(null);
      }
      setError(null);
    } catch (err: any) {
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshSession();
  }, [refreshSession]);

  const login = async (email: string, password: string): Promise<User> => {
    setError(null);
    try {
      const data = await apiRequest<User>('auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      setUser(data);
      return data;
    } catch (err: any) {
      setError(err.message || 'Login failed');
      throw err;
    }
  };

  const logout = async () => {
    try {
      await apiRequest('auth/logout', { method: 'POST' });
    } catch (err) {
      // Ignore logout network errors
    } finally {
      setUser(null);
    }
  };

  const isAdmin = user?.role === 'ADMINISTRATOR';
  const isSecurity = user?.role === 'SECURITY';
  const isResident = user?.role === 'RESIDENT';

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        error,
        login,
        logout,
        refreshSession,
        isAdmin,
        isSecurity,
        isResident,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
