import React, { createContext, useContext, useState, useEffect } from 'react';
import axiosClient from '../services/axiosClient';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [currentUser, setCurrentUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const userStr = localStorage.getItem('currentUser');
    if (token && userStr) {
      try {
        setAccessToken(token);
        setCurrentUser(JSON.parse(userStr));
      } catch (err) {
        // Clear corrupt storage
        localStorage.removeItem('accessToken');
        localStorage.removeItem('currentUser');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await axiosClient.post('/auth/login', { email, password });
      
      // Standard Response format: response.data.data
      const authData = response.data.data;
      
      const user = {
        userId: authData.userId,
        fullName: authData.fullName,
        email: authData.email,
        role: authData.role,
      };

      localStorage.setItem('accessToken', authData.accessToken);
      if (authData.refreshToken) {
        localStorage.setItem('refreshToken', authData.refreshToken);
      }
      localStorage.setItem('currentUser', JSON.stringify(user));

      setAccessToken(authData.accessToken);
      setCurrentUser(user);
      return { success: true, user };
    } catch (error) {
      console.error('Login error:', error);
      const message = error.response?.data?.message || 'Invalid email or password';
      return { success: false, message };
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');
    setAccessToken(null);
    setCurrentUser(null);
  };

  const isAuthenticated = () => {
    return !!accessToken;
  };

  const value = {
    currentUser,
    accessToken,
    loading,
    login,
    logout,
    isAuthenticated,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
