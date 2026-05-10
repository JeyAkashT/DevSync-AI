import type { ReactNode } from "react";

export type Column<T> = {
  header: string;
  cell: (row: T) => ReactNode;
  className?: string;
};

type Props<T> = { rows: T[]; columns: Column<T>[]; empty?: string };

export function DataTable<T>({ rows, columns, empty = "Nothing here yet." }: Props<T>) {
  if (rows.length === 0) {
    return <p className="text-sm text-slate-500">{empty}</p>;
  }
  return (
    <div className="overflow-x-auto rounded-lg border border-slate-800">
      <table className="min-w-full divide-y divide-slate-800 text-sm">
        <thead className="bg-slate-900/90">
          <tr>
            {columns.map((c) => (
              <th key={c.header} className={`px-3 py-2 text-left font-medium text-slate-400 ${c.className ?? ""}`}>
                {c.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800">
          {rows.map((row, idx) => (
            <tr key={idx} className="bg-slate-950/60">
              {columns.map((c) => (
                <td key={c.header} className={`px-3 py-2 text-slate-200 ${c.className ?? ""}`}>
                  {c.cell(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
