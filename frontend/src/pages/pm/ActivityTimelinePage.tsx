import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "sonner";

import { fetchActivity } from "../../features/pm/pmApi";
import type { ActivityEntry } from "../../features/pm/types";
import { Card } from "../../shared/ui/Card";

export function ActivityTimelinePage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [rows, setRows] = useState<ActivityEntry[]>([]);

  useEffect(() => {
    if (!projectId) return;
    void (async () => {
      try {
        const env = await fetchActivity(projectId);
        setRows(env.items);
      } catch {
        toast.error("Unable to load activity.");
      }
    })();
  }, [projectId]);

  return (
    <Card title={`Activity (${rows.length})`}>
      <ol className="space-y-4 border-l border-slate-800 pl-4">
        {rows.map((row) => (
          <li key={row.id} className="relative">
            <span className="absolute -left-[21px] mt-1 size-2 rounded-full bg-sky-500" />
            <div className="rounded-lg border border-slate-800 bg-slate-950/60 p-3">
              <p className="text-sm font-medium text-white">{row.action}</p>
              <p className="text-xs text-slate-500">
                {row.entityType} · {row.entityId}
              </p>
              <p className="text-xs text-slate-600">{new Date(row.createdAt).toLocaleString()}</p>
            </div>
          </li>
        ))}
      </ol>
      {rows.length === 0 ? <p className="text-sm text-slate-500">No activity logged yet.</p> : null}
    </Card>
  );
}
