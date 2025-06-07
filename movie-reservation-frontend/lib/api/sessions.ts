import apiClient from "@/lib/utils/api-client";
import { Seat, SessionDetailed } from "@/lib/types";

export const getSession = async (id: number): Promise<SessionDetailed> => {
  return apiClient.get<SessionDetailed>(`/api/sessions/${id}`);
};

export const tryReserveSeatsInCache = async (sessionId: number, seatId: number): Promise<void> => {
  const params = new URLSearchParams();
  const clientId = apiClient.getClientId();
  if (clientId) params.append("clientId", clientId);
  return apiClient.post(`/api/sessions/${sessionId}/seats/${seatId}/cache?${params.toString()}`);
};

export const releaseSeatsFromCache = async (sessionId: number, seatId: number): Promise<void> => {
  const params = new URLSearchParams();
  const clientId = apiClient.getClientId();
  if (clientId) params.append("clientId", clientId);
  return apiClient.delete(`/api/sessions/${sessionId}/seats/${seatId}/cache?${params.toString()}`);
};