import { useEffect, useState } from "react";

import { dismissNotice, pushNotice } from "../features/app/appSlice";
import { useAppDispatch, useAppSelector } from "../app/hooks";
import { selectAuthUser } from "../features/auth/authSlice";
import { fetchPublicHealth } from "../shared/api/health";

export function DashboardPage() {
  const dispatch = useAppDispatch();
  const user = useAppSelector(selectAuthUser);
  const notices = useAppSelector((s) => s.app.notices);
  const [checking, setChecking] = useState(false);

  useEffect(() => {
    async function ping() {
      setChecking(true);
      try {
        const data = await fetchPublicHealth();
        dispatch(
          pushNotice({
            message: `API responded: ${data.status}`,
            variant: "info",
          })
        );
      } catch {
        // Global Axios interceptor surfaces the error toast
      } finally {
        setChecking(false);
      }
    }
    void ping();
  }, [dispatch]);

  const displayName = user?.fullName?.trim() || user?.email || "there";

  return (
    <div className="w-full space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-white">Welcome, {displayName}</h1>
        <p className="mt-2 max-w-2xl text-slate-400">
          You are signed in with roles: {user?.roles?.join(", ") ?? "—"}. The dashboard pings the public health
          endpoint (no auth required).
        </p>
      </div>

      {notices.length > 0 && (
        <ul className="space-y-2">
          {notices.map((n) => (
            <li
              key={n.id}
              className={`flex items-center justify-between gap-4 rounded-lg border px-4 py-3 text-sm ${
                n.variant === "error"
                  ? "border-rose-500/40 bg-rose-950/40 text-rose-100"
                  : "border-emerald-500/30 bg-emerald-950/30 text-emerald-100"
              }`}
            >
              <span>{n.message}</span>
              <button
                type="button"
                className="rounded-md bg-white/10 px-2 py-1 text-xs text-white hover:bg-white/15"
                onClick={() => dispatch(dismissNotice(n.id))}
              >
                Dismiss
              </button>
            </li>
          ))}
        </ul>
      )}

      <div className="rounded-xl border border-slate-800 bg-slate-900/50 p-4 text-sm text-slate-400">
        {checking ? "Checking backend…" : "Health check attempted once on mount."}
      </div>
    </div>
  );
}
