import { ApiError } from "next/dist/server/api-utils";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8888";

class ApiClient {
  private clientId: string | null = null;

  constructor() {
    if (typeof window !== "undefined") {
      const clientId = sessionStorage.getItem("clientId")

      if (!clientId) {
        this.clientId = crypto.randomUUID();
        sessionStorage.setItem("clientId", this.clientId)
      } else {
        this.clientId = clientId
      }
    }
  }

  getClientId() {
    return this.clientId;
  }

  async checkAuth(): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/check`, {
        method: "GET",
        credentials: "include" as RequestCredentials

      });

      if (!response.ok) {
        return false;
      }

      return true;
    } catch (error) {
      return false;
    }
  }

  async refreshAccessToken(): Promise<boolean> {
    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/refresh-token`, {
        method: "POST",
        credentials: "include" as RequestCredentials
      });

      if (!response.ok) {
        return false;
      }

      return true;
    } catch (error) {
      return false;
    }
  }


  async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers: Record<string, string> = {
      "Content-Type": "application/json",
      ...(options.headers as Record<string, string>),
    };

    const config = {
      ...options,
      headers,
      credentials: "include" as RequestCredentials
    };

    let response = await fetch(url, config);

    // Try refreshing token if unauthorized, then retry request
    if (response.status === 401 || response.status === 403) {
      const refreshSuccess = await this.refreshAccessToken();

      if (refreshSuccess) {
        response = await fetch(url, config);
      } else {
        throw new ApiError(response.status, "refresh-error");
      }
    }

    if (!response.ok) {
      throw new ApiError(response.status, "");
    }

    if (response.status === 204) {
      return {} as T;
    }

    const contentType = response.headers.get("Content-Type") || "";

    if (contentType.includes("application/json")) {
      return await response.json();
    } else if (contentType.includes("image/png")) {
      return (await response.blob()) as T;
    } else if (contentType.startsWith("text/")) {
      return (await response.text()) as T;
    } else {
      return (await response.text()) as T;
    }
  }

  // Convenience methods for common HTTP methods
  async get<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, { ...options, method: "GET" });
  }

  async post<T>(endpoint: string, data?: any, options: RequestInit & { responseType?: 'json' | 'blob' } = {}): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: "POST",
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async put<T>(endpoint: string, data?: any, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: "PUT",
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async patch<T>(endpoint: string, data?: any, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: "PATCH",
      body: data ? JSON.stringify(data) : undefined,
    });
  }

  async delete<T>(endpoint: string, data?: any, options: RequestInit = {}): Promise<T> {
    return this.request<T>(endpoint, {
      ...options,
      method: "DELETE",
      body: data ? JSON.stringify(data) : undefined,
    });
  }
}

// Create a singleton instance
const apiClient = new ApiClient();

export default apiClient;