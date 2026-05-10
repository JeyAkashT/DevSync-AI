import axios from "axios";
import { toast } from "sonner";

import {
  notifyUnauthorized,
  readAccessToken,
  shouldIgnore401ForRequest,
} from "../../features/auth/session";
import { extractErrorMessage } from "./errorMessage";

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "";

export const http = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

http.interceptors.request.use((config) => {
  const token = readAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (res) => res,
  (err: unknown) => {
    if (axios.isAxiosError(err)) {
      const status = err.response?.status;
      const url = err.config?.url ?? "";

      if (status === 401 && !shouldIgnore401ForRequest(url)) {
        notifyUnauthorized();
      }

      if (status && status >= 400) {
        const isAuthForm = url.includes("/api/v1/auth/login") || url.includes("/api/v1/auth/register");
        const silentUnauthorized = status === 401 && !isAuthForm;
        if (!silentUnauthorized) {
          toast.error(extractErrorMessage(err));
        }
      }
    } else {
      toast.error("Unexpected error.");
    }
    return Promise.reject(err);
  }
);
