import apiClient from "@/lib/utils/api-client";
import { BookingResponse, BookingDetailed, Pageable } from "@/lib/types";

export const getBookings = async (
  page: number,
  status?: string
): Promise<Pageable<BookingDetailed>> => {
  const params = new URLSearchParams();
  params.append("page", page.toString());

  let statuses: string[] = [];

  if (status && status != "all") {
    statuses.push(status);
    if (status === "AWAITING_PAYMENT") {
      statuses.push("PAYMENT_RETRY");
    } else if (status === "CANCELLED") {
      statuses.push("AWAITING_CANCELLATION");
    }
  }

  params.append("statuses", statuses.toString());
  return apiClient.get<Pageable<BookingDetailed>>(`/api/bookings?${params.toString()}`);
};

export const createBooking = async (
  sessionId: number,
  seatIds: number[],
  successUrl: string,
  cancelUrl: string
): Promise<BookingResponse> => {
  return apiClient.post<BookingResponse>('/api/bookings', {
    sessionId,
    seatIds,
    successUrl,
    cancelUrl
  });
};

export const createQrCode = async (bookingId: number): Promise<Blob> => {
  return apiClient.post(`/api/bookings/${bookingId}/qr-code`, null);
}

export const tryCancelBooking = async (bookingId: number): Promise<void> => {
  return apiClient.patch(`/api/bookings/${bookingId}/cancel`, null);
}

export const tryConfirmingPayment = async (bookingId: number, checkoutId: string | null): Promise<boolean> => {
  return apiClient.patch<boolean>(`/api/bookings/${bookingId}/try-confirming`, { checkoutId });
}