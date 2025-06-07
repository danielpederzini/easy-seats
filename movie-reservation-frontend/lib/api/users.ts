import apiClient from "@/lib/utils/api-client";
import { UserProfile } from "@/lib/types";

export const getProfile = async (): Promise<UserProfile> => {
  return apiClient.get<UserProfile>(`/api/users/fromToken`);
};