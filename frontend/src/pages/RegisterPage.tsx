import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";

import { useAppDispatch, useAppSelector } from "../app/hooks";
import { clearError, register, selectAuthLoading } from "../features/auth/authSlice";
export function RegisterPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const loading = useAppSelector(selectAuthLoading);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [fullName, setFullName] = useState("");

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    dispatch(clearError());
    const result = await dispatch(
      register({
        email: email.trim(),
        password,
        fullName: fullName.trim() || undefined,
      })
    );
    if (register.fulfilled.match(result)) {
      toast.success("Account created — you are signed in.");
      navigate("/", { replace: true });
    }
  }

  return (
    <div className="flex min-h-[calc(100vh-0px)] items-center justify-center px-4">
      <div className="w-full max-w-sm space-y-6 rounded-xl border border-slate-800 bg-slate-900/60 p-8 shadow-xl">
        <div>
          <h1 className="text-xl font-semibold text-white">Create account</h1>
          <p className="mt-1 text-sm text-slate-400">Join your team on DevSync AI.</p>
        </div>
        <form onSubmit={(ev) => void onSubmit(ev)} className="space-y-4">
          <label className="block text-sm font-medium text-slate-300">
            Full name
            <input
              autoComplete="name"
              type="text"
              value={fullName}
              onChange={(ev) => setFullName(ev.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none ring-sky-500 focus:ring-2"
            />
          </label>
          <label className="block text-sm font-medium text-slate-300">
            Email
            <input
              autoComplete="email"
              required
              type="email"
              value={email}
              onChange={(ev) => setEmail(ev.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none ring-sky-500 focus:ring-2"
            />
          </label>
          <label className="block text-sm font-medium text-slate-300">
            Password
            <input
              autoComplete="new-password"
              required
              minLength={8}
              type="password"
              value={password}
              onChange={(ev) => setPassword(ev.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white outline-none ring-sky-500 focus:ring-2"
            />
            <span className="mt-1 block text-xs text-slate-500">At least 8 characters.</span>
          </label>
          <button
            type="submit"
            disabled={loading}
            className="w-full rounded-lg bg-sky-600 py-2 text-sm font-semibold text-white hover:bg-sky-500 disabled:opacity-50"
          >
            {loading ? "Creating…" : "Create account"}
          </button>
        </form>
        <p className="text-center text-sm text-slate-400">
          Already have an account?{" "}
          <Link className="font-medium text-sky-400 hover:text-sky-300" to="/login">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
