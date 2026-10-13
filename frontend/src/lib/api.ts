import { ApiResponse } from '@/types';

export class ApiError extends Error {
  public errors?: Record<string, string>;
  public status: number;

  constructor(message: string, status: number, errors?: Record<string, string>) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errors = errors;
  }
}

function getCsrfTokenFromCookie(): string | null {
  const match = document.cookie.match(new RegExp('(^|;\\s*)XSRF-TOKEN=([^;]*)'));
  return match ? decodeURIComponent(match[2]) : null;
}

export async function apiRequest<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const cleanEndpoint = endpoint.startsWith('/') ? endpoint.slice(1) : endpoint;
  const url = endpoint.startsWith('/api/') || endpoint.startsWith('api/')
    ? (endpoint.startsWith('/') ? endpoint : `/${endpoint}`)
    : `/api/v1/${cleanEndpoint}`;
  const headers = new Headers(options.headers || {});

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  headers.set('Accept', 'application/json');
  headers.set('X-Requested-With', 'XMLHttpRequest');

  // Attach CSRF Token on modifying methods
  const method = options.method?.toUpperCase() || 'GET';
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
    const csrfToken = getCsrfTokenFromCookie();
    if (csrfToken) {
      headers.set('X-XSRF-TOKEN', csrfToken);
    }
  }

  const response = await fetch(url, {
    ...options,
    headers,
    credentials: 'include', // Compulsory for HttpOnly persistent sessions
  });

  if (response.status === 204) {
    return {} as T;
  }

  let json: ApiResponse<T>;
  try {
    json = await response.json();
  } catch (err) {
    if (!response.ok) {
      throw new ApiError(`Request failed with status ${response.status}`, response.status);
    }
    return {} as T;
  }

  if (!response.ok || !json.success) {
    throw new ApiError(
      json.message || `Request failed with status ${response.status}`,
      response.status,
      json.errors
    );
  }

  return json.data;
}

export const api = {
  get: async <T = any>(url: string, options?: RequestInit) => {
    const data = await apiRequest<T>(url, { method: 'GET', ...options });
    return { data };
  },
  post: async <T = any>(url: string, body?: any, options?: RequestInit) => {
    const isFormData = body instanceof FormData;
    const data = await apiRequest<T>(url, {
      method: 'POST',
      body: isFormData ? body : (body !== undefined ? JSON.stringify(body) : undefined),
      ...options,
    });
    return { data };
  },
  put: async <T = any>(url: string, body?: any, options?: RequestInit) => {
    const isFormData = body instanceof FormData;
    const data = await apiRequest<T>(url, {
      method: 'PUT',
      body: isFormData ? body : (body !== undefined ? JSON.stringify(body) : undefined),
      ...options,
    });
    return { data };
  },
  patch: async <T = any>(url: string, body?: any, options?: RequestInit) => {
    const isFormData = body instanceof FormData;
    const data = await apiRequest<T>(url, {
      method: 'PATCH',
      body: isFormData ? body : (body !== undefined ? JSON.stringify(body) : undefined),
      ...options,
    });
    return { data };
  },
  delete: async <T = any>(url: string, options?: RequestInit) => {
    const data = await apiRequest<T>(url, { method: 'DELETE', ...options });
    return { data };
  },
};
