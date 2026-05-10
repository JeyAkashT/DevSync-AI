import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="w-full">
      <h1 className="text-xl font-semibold text-white">Not found</h1>
      <p className="mt-2 text-slate-400">The page you requested does not exist.</p>
      <Link className="mt-4 inline-block text-sm font-medium text-sky-400 hover:text-sky-300" to="/">
        Back home
      </Link>
    </div>
  );
}
