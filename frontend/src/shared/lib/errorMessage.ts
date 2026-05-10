import axios from "axios";

type ProblemBody = {
  detail?: string;
  title?: string;
  errors?: Record<string, string>;
};

export function extractErrorMessage(payload: unknown): string {
  if (axios.isAxiosError(payload)) {
    const data = payload.response?.data as ProblemBody | undefined;
    if (data?.errors && Object.keys(data.errors).length > 0) {
      return Object.entries(data.errors)
        .map(([k, v]) => `${k}: ${v}`)
        .join("; ");
    }
    if (data?.detail) {
      return data.detail;
    }
    if (payload.message) {
      return payload.message;
    }
  }
  if (payload instanceof Error) {
    return payload.message;
  }
  return "Something went wrong.";
}
