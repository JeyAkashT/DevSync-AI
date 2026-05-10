import { type ReactNode, useEffect } from "react";
import { Navigate } from "react-router-dom";

import { useAppDispatch, useAppSelector } from "../../app/hooks";
import { loadCurrentUser, selectAuthLoading, selectAuthUser, selectToken } from "./authSlice";

type Props = { children: ReactNode };

export function RequireAuth({ children }: Props) {
  const dispatch = useAppDispatch();
  const token = useAppSelector(selectToken);
  const user = useAppSelector(selectAuthUser);
  const loading = useAppSelector(selectAuthLoading);

  useEffect(() => {
    if (token && !user) {
      void dispatch(loadCurrentUser());
    }
  }, [dispatch, token, user]);

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (!user || loading) {
    return (
      <div className="flex flex-1 items-center justify-center text-sm text-slate-400">
        Authenticating…
      </div>
    );
  }

  return <>{children}</>;
}
