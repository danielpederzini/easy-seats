import apiClient from "@/lib/utils/api-client";
import { LoginRequest, SignupRequest } from "@/lib/types";

export const checkAuth = async (): Promise<boolean> => {
  return apiClient.checkAuth();
};

export const authForWebsocket = async (clientId: string): Promise<string> => {
  return apiClient.post<string>(`/api/auth/ws?clientId=${clientId}`);
};

export const login = async (credentials: LoginRequest): Promise<void> => {
  return apiClient.post<void>("/api/auth/login", credentials);
};

export const signup = async (userData: SignupRequest): Promise<number> => {
  return apiClient.post<number>("/api/auth/signup", userData);
};

export const logout = async (): Promise<void> => {
  await apiClient.delete<void>("/api/auth/logout");
};

export const refreshToken = async (): Promise<boolean> => {
  return apiClient.refreshAccessToken();
};