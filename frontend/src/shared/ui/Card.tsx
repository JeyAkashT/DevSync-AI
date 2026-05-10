import type { ReactNode } from "react";

type Props = { title?: string; children: ReactNode; className?: string };

export function Card({ title, children, className = "" }: Props) {
  return (
    <div className={`rounded-xl border border-slate-800 bg-slate-900/60 p-4 shadow-sm ${className}`}>
      {title ? <h3 className="mb-2 text-sm font-semibold text-white">{title}</h3> : null}
      {children}
    </div>
  );
}
