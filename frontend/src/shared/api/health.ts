import { http } from "../lib/http";

export async function fetchPublicHealth(): Promise<{ status: string }> {
  const { data } = await http.get<{ status: string }>("/api/v1/public/health");
  return data;
}
