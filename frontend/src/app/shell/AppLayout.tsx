import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { toast } from "sonner";

import { useAppDispatch, useAppSelector } from "../hooks";
import { logout, selectAuthUser } from "../../features/auth/authSlice";

const linkClass =
  "rounded-md px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-800 hover:text-white";
const activeClass = "bg-slate-800 text-white";

export function AppLayout() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const title = useAppSelector((s) => s.app.title);
  const user = useAppSelector(selectAuthUser);

  function handleLogout() {
    dispatch(logout());
    toast.success("Signed out.");
    navigate("/login", { replace: true });
  }

  return (
    <div className="flex min-h-full flex-col">
      <header className="border-b border-slate-800 bg-slate-900/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-6 px-4 py-4">
          <span className="text-lg font-semibold tracking-tight text-white">{title}</span>
          <div className="flex items-center gap-4">
            <nav className="flex gap-2">
              <NavLink end to="/" className={({ isActive }) => `${linkClass} ${isActive ? activeClass : ""}`}>
                Dashboard
              </NavLink>
              <NavLink to="/projects" className={({ isActive }) => `${linkClass} ${isActive ? activeClass : ""}`}>
                Projects
              </NavLink>
            </nav>
            <div className="hidden h-6 w-px bg-slate-700 sm:block" aria-hidden />
            <div className="flex items-center gap-3 text-sm text-slate-400">
              <span className="max-w-[12rem] truncate text-slate-200" title={user?.email}>
                {user?.email}
              </span>
              <button
                type="button"
                className="rounded-md border border-slate-700 px-3 py-1.5 text-xs font-medium text-slate-200 hover:bg-slate-800"
                onClick={handleLogout}
              >
                Sign out
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="mx-auto flex w-full max-w-6xl flex-1 px-4 py-8">
        <Outlet />
      </main>

      <footer className="border-t border-slate-800 px-4 py-6 text-center text-xs text-slate-500">
        DevSync AI — authenticated area
      </footer>
    </div>
  );
}
