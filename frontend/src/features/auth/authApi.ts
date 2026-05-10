import { http } from "../../shared/lib/http";

import type { AuthResponseDto, AuthUser } from "./types";

export async function loginApi(body: {
  email: string;
  password: string;
}): Promise<AuthResponseDto> {
  const { data } = await http.post<AuthResponseDto>("/api/v1/auth/login", body);
  return data;
}

export async function registerApi(body: {
  email: string;
  password: string;
  fullName?: string;
}): Promise<AuthResponseDto> {
  const { data } = await http.post<AuthResponseDto>("/api/v1/auth/register", body);
  return data;
}

export async function fetchMeApi(): Promise<AuthUser> {
  const { data } = await http.get<AuthUser>("/api/v1/me");
  return data;
}
