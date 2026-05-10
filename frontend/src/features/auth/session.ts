const STORAGE_KEY = "devsync.ai.accessToken";

export function persistAccessToken(token: string): void {
  try {
    localStorage.setItem(STORAGE_KEY, token);
  } catch {
    /* ignore */
  }
}

export function clearAccessToken(): void {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch {
    /* ignore */
  }
}

export function readAccessToken(): string | null {
  try {
    return localStorage.getItem(STORAGE_KEY);
  } catch {
    return null;
  }
}

let auth401Handler: (() => void) | null = null;

export function setAuth401Handler(handler: () => void): void {
  auth401Handler = handler;
}

export function notifyUnauthorized(): void {
  auth401Handler?.();
}

export function shouldIgnore401ForRequest(url: string | undefined): boolean {
  if (!url) {
    return false;
  }
  return url.includes("/api/v1/auth/login") || url.includes("/api/v1/auth/register");
}
