// Movie types
export enum MovieGenre {
  ACTION = "ACTION",
  COMEDY = "COMEDY",
  DRAMA = "DRAMA",
  HORROR = "HORROR",
  SCI_FI = "SCI_FI",
  THRILLER = "THRILLER",
  ROMANCE = "ROMANCE",
  ANIMATION = "ANIMATION",
  DOCUMENTARY = "DOCUMENTARY",
  FANTASY = "FANTASY",
  MYSTERY = "MYSTERY",
  SPORTS = "SPORTS",
  WESTERN = "WESTERN",
  HISTORICAL = "HISTORICAL",
}

export interface Movie {
  id: number;
  title: string;
  description: string;
  genre: MovieGenre;
  formattedDuration: string;
  isOnDisplay: boolean;
  hasSessions: boolean;
  posterUrl?: string;
  releaseDate: string;
}

// Session types
export enum Language {
  ENGLISH = "ENGLISH",
  SPANISH = "SPANISH",
  FRENCH = "FRENCH",
  GERMAN = "GERMAN",
  JAPANESE = "JAPANESE",
  CHINESE = "CHINESE",
  KOREAN = "KOREAN",
  PORTUGUESE = "PORTUGUESE",
  ITALIAN = "ITALIAN",
  RUSSIAN = "RUSSIAN"
}

export interface Session {
  id: number;
  startTime: string;
  endTime: string;
  audioLanguage: Language;
  hasSubtitles: boolean;
  threeD: boolean;
  standardSeatPrice: number;
  vipSeatPrice: number;
  pwdSeatPrice: number;
  theaterId: number;
  theaterName: string,
  theaterLogoUrl: string,
  theaterAddress: string;
  screenName: string;
  hasFreeSeats: boolean;
}

export interface SessionDetailed {
  id: number;
  startTime: string;
  endTime: string;
  audioLanguage: Language;
  hasSubtitles: boolean;
  threeD: boolean;
  standardSeatPrice: number;
  vipSeatPrice: number;
  pwdSeatPrice: number;
  theaterId: number;
  theaterName: string,
  theaterLogoUrl: string;
  theaterAddress: string;
  screenName: string;
  movie: Movie;
  seats: Seat[];
}

// Seat types
export enum SeatType {
  STANDARD = "STANDARD",
  VIP = "VIP",
  PWD = "PWD"
}

export interface Seat {
  id: number;
  seatRow: string;
  seatNumber: number;
  seatType: SeatType;
  taken?: boolean;
}

export interface SeatUpdate {
  id: number;
  originId: string;
  taken: boolean;
}

// Booking types
export enum BookingStatus {
  AWAITING_PAYMENT = "AWAITING_PAYMENT",
  PAYMENT_RETRY = "PAYMENT_RETRY",
  PAYMENT_CONFIRMED = "PAYMENT_CONFIRMED",
  AWAITING_CANCELLATION = "AWAITING_CANCELLATION",
  CANCELLED = "CANCELLED",
  PAST = "PAST"
}

export interface BookingResponse {
  bookingId: number;
  checkoutId: string;
  checkoutUrl: string;
}

export interface BookingDetailed {
  id: number;
  bookingStatus: BookingStatus;
  totalPrice: number;
  checkoutId: string;
  checkoutUrl: string;
  createdAt: string;
  updatedAt: string;
  expiresAt: string;
  checkoutCompleted: boolean
  movie: Movie;
  session: Session;
  bookedSeats: BookedSeat[];
}

export interface BookedSeat {
  id: number;
  seatRow: string;
  seatNumber: number;
  seatType: SeatType;
  seatPrice: number;
  qrCode: string;
}

// Auth types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
}

export interface TokenRequest {
  token: string;
}

// User types
export interface UserProfile {
  id: number
  userName: string;
  email: string;
  userRole: string;
}

// Pageable
export interface Pageable<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
};