import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "sonner";

import { createSprint, fetchSprints } from "../../features/pm/pmApi";
import type { Sprint } from "../../features/pm/types";
import { Card } from "../../shared/ui/Card";
import { DataTable, type Column } from "../../shared/ui/DataTable";

export function SprintPlanningPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [rows, setRows] = useState<Sprint[]>([]);

  async function load() {
    if (!projectId) return;
    const envelope = await fetchSprints(projectId);
    setRows(envelope.items);
  }

  useEffect(() => {
    void (async () => {
      try {
        await load();
      } catch {
        toast.error("Failed to fetch sprints.");
      }
    })();
  }, [projectId]);

  async function create(ev: React.FormEvent) {
    ev.preventDefault();
    if (!projectId) return;
    const form = ev.target as HTMLFormElement & {
      name: HTMLInputElement;
      start: HTMLInputElement;
      end: HTMLInputElement;
      goal: HTMLInputElement;
    };
    try {
      await createSprint(projectId, {
        name: form.name.value,
        startDate: form.start.value,
        endDate: form.end.value,
        goal: form.goal.value || undefined,
        status: "PLANNED",
      });
      toast.success("Sprint created.");
      await load();
      form.reset();
    } catch {
      /* error toast from HTTP layer */
    }
  }

  const columns: Column<Sprint>[] = [
    { header: "Name", cell: (s) => s.name },
    { header: "Dates", cell: (s) => `${s.startDate} → ${s.endDate}` },
    { header: "Status", cell: (s) => s.status },
    { header: "Goal", cell: (s) => s.goal ?? "—" },
  ];

  return (
    <div className="space-y-4">
      <Card title="New sprint">
        <form onSubmit={(e) => void create(e)} className="grid gap-2 md:grid-cols-2">
          <input name="name" placeholder="Name" required className="rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white" />
          <div className="flex gap-2">
            <input type="date" name="start" required className="flex-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white" />
            <input type="date" name="end" required className="flex-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white" />
          </div>
          <textarea name="goal" placeholder="Goal" className="md:col-span-2 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white" rows={2} />
          <button type="submit" className="md:col-span-2 rounded-lg bg-sky-600 py-2 text-sm font-medium text-white">
            Create sprint
          </button>
        </form>
      </Card>
      <Card title={`Sprints (${rows.length})`}>
        <DataTable rows={rows} columns={columns} />
      </Card>
    </div>
  );
}
