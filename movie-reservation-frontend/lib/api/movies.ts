import apiClient from "@/lib/utils/api-client";
import { Movie, Pageable, Session } from "@/lib/types";

export const getMovies = async (
  page: number,
  search?: string,
  genre?: string
): Promise<Pageable<Movie>> => {
  // Build query params
  const params = new URLSearchParams();
  params.append("page", page.toString());
  if (search) params.append("search", search);
  if (genre && genre !== "all") params.append("genres", genre);
  return apiClient.get<Pageable<Movie>>(`/api/movies?${params.toString()}`);
};

export const getMovie = async (
  id: number,
): Promise<Movie> => {
  return apiClient.get<Movie>(`/api/movies/${id}`);
};

export const getMovieSessions = async (
  id: number,
  page: number
): Promise<Pageable<Session>> => {
  const params = new URLSearchParams();
  params.append("page", page.toString());
  return apiClient.get<Pageable<Session>>(`/api/movies/${id}/sessions?${params.toString()}`);
};