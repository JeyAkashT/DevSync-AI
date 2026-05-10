import { type ReactNode } from "react";
import { Navigate } from "react-router-dom";

import { useAppSelector } from "../../app/hooks";
import { selectToken } from "./authSlice";

export function GuestOnly({ children }: { children: ReactNode }) {
  const token = useAppSelector(selectToken);
  if (token) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}
