import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "sonner";

import { createBug, fetchBugPage, patchBug } from "../../features/pm/pmApi";
import type { Bug } from "../../features/pm/types";
import { Card } from "../../shared/ui/Card";
import { DataTable, type Column } from "../../shared/ui/DataTable";

export function BugTrackerPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [bugs, setBugs] = useState<Bug[]>([]);

  useEffect(() => {
    if (!projectId) return;
    void (async () => {
      try {
        const envelope = await fetchBugPage(projectId);
        setBugs(envelope.items);
      } catch {
        toast.error("Failed to fetch bugs.");
      }
    })();
  }, [projectId]);

  const columns: Column<Bug>[] = [
    { header: "Title", cell: (b) => b.title },
    { header: "Severity", cell: (b) => b.severity },
    {
      header: "Status",
      cell: (b) => (
        <select
          key={`${b.id}:${b.status}`}
          className="rounded-md border border-slate-700 bg-slate-900 px-2 py-1 text-xs text-white"
          defaultValue={b.status}
          onChange={(ev) =>
            void patchBugSafe(b.id, { status: ev.target.value }).then(() => toast.success("Bug updated."))
          }
        >
          {["OPEN", "TRIAGED", "IN_PROGRESS", "RESOLVED", "CLOSED"].map((s) => (
            <option key={s} value={s}>
              {s}
            </option>
          ))}
        </select>
      ),
    },
  ];

  async function patchBugSafe(id: string, body: Record<string, unknown>) {
    await patchBug(id, body);
    const envelope = await fetchBugPage(projectId!);
    setBugs(envelope.items);
  }

  async function quickCreate(ev: React.FormEvent) {
    ev.preventDefault();
    if (!projectId) return;
    const form = ev.target as HTMLFormElement & { title?: HTMLInputElement };
    const ti = form.title?.value.trim();
    if (!ti) return;
    await createBug(projectId, {
      title: ti,
      severity: "MINOR",
      status: "OPEN",
    });
    form.title!.value = "";
    const envelope = await fetchBugPage(projectId);
    setBugs(envelope.items);
    toast.success("Bug filed.");
  }

  return (
    <div className="w-full space-y-4">
      <Card title="Log bug">
        <form onSubmit={(e) => void quickCreate(e)} className="flex flex-wrap gap-2">
          <input name="title" placeholder="Title" required className="flex-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white min-w-[12rem]" />
          <button className="rounded-lg bg-rose-600 px-4 py-2 text-sm text-white hover:bg-rose-500">
            Submit
          </button>
        </form>
      </Card>
      <Card title={`Backlog (${bugs.length})`}>
        <DataTable rows={bugs} columns={columns} />
      </Card>
    </div>
  );
}
