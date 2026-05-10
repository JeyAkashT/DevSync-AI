export type AuthUser = {
  id: string;
  email: string;
  fullName: string | null;
  roles: string[];
};

export type AuthResponseDto = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
};
