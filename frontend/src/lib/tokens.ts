const ACCESS_KEY = "ml.accessToken";
const REFRESH_KEY = "ml.refreshToken";

export type Tokens = { accessToken: string; refreshToken: string };

export function readTokens(): Tokens | null {
  if (typeof window === "undefined") return null;
  try {
    const accessToken = window.localStorage.getItem(ACCESS_KEY);
    const refreshToken = window.localStorage.getItem(REFRESH_KEY);
    if (!accessToken || !refreshToken) return null;
    return { accessToken, refreshToken };
  } catch {
    return null;
  }
}

export function writeTokens(tokens: Tokens) {
  try {
    window.localStorage.setItem(ACCESS_KEY, tokens.accessToken);
    window.localStorage.setItem(REFRESH_KEY, tokens.refreshToken);
  } catch {
    // storage unavailable, ignore
  }
}

export function clearTokens() {
  try {
    window.localStorage.removeItem(ACCESS_KEY);
    window.localStorage.removeItem(REFRESH_KEY);
  } catch {
    // ignore
  }
}
