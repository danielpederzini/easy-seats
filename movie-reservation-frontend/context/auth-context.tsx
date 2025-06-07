"use client";

import React, { createContext, useContext, useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { checkAuth as apiCheckAuth, login as apiLogin, signup as apiSignup, logout as apiLogout } from "@/lib/api/auth";
import { LoginRequest, SignupRequest, UserProfile } from "@/lib/types";
import { getProfile } from "@/lib/api/users";
import { ApiError } from "next/dist/server/api-utils";

interface AuthContextType {
  user: UserProfile | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest, redirectUrl: string | null) => Promise<void>;
  signup: (userData: SignupRequest) => Promise<void>;
  logout: () => Promise<void>;
  error: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  // Check if user is already logged in on mount
  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
      try {
        setIsLoading(true);
        const isAuthenticated = await apiCheckAuth();
        
        if (isAuthenticated) {
          const user = await getProfile();
          setUser(user);
        }
      } catch (error: ApiError | any) {
        const status = error.statusCode;

        if (status) {
          console.error(`Auth check failed with status ${status}`);
        } else {
          console.error("Failed to check user auth");
        }

      } finally {
        setIsLoading(false);
      }
    };


  const login = async (credentials: LoginRequest, redirectUrl: string | null) => {
    try {
      setIsLoading(true);
      setError(null);
      await apiLogin(credentials);
      fetchUserProfile();
      router.push(redirectUrl ? redirectUrl : "/movies");
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      if (status === 401) {
        setError("Invalid email or password.");
      } else if (status === 500) {
        setError("Server error. Please try again later.");
      } else if (status) {
        setError(`Login failed with status ${status}.`);
      } else {
        setError("Failed to login. Please try again later.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const signup = async (userData: SignupRequest) => {
    try {
      setIsLoading(true);
      setError(null);
      await apiSignup(userData);
      fetchUserProfile();
      router.push('/movies');
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      if (status === 409) {
        setError("Email already in use. Please try a different email.");
      } else if (status === 500) {
        setError("Server error. Please try again later.");
      } else if (status) {
        setError(`Singup failed with status ${status}.`);
      } else {
        setError("Failed to signup. Check your data and try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const fetchUserProfile = async () => {
    try {
      const user = await getProfile();
      setUser(user);
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      if (status) {
        console.error(`Profile fetch failed with status ${status}`);
      } else {
        console.error("Failed to fetch user profile");
      }
    }
  }

  const logout = async () => {
    try {
      setIsLoading(true);
      await apiLogout();
    } catch (err: ApiError | any) {
      const status = err.statusCode;

      if (status === 500) {
        console.error("Server error when logging out");
      } else if (status) {
        console.error(`Logout failed with status ${status}`);
      } else {
        console.error("Failed to logout");
      }

    } finally {
      setIsLoading(false);
      setUser(null);
      router.push('/auth/login');
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        signup,
        logout,
        error,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};