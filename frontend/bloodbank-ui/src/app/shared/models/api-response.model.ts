/**
 * Standard API response wrapper matching backend ApiResponse<T>.
 */
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  timestamp: string;
}
