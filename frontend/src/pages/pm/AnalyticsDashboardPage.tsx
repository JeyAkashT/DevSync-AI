import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "sonner";

import { fetchProjectAnalytics } from "../../features/pm/pmApi";
import type { AnalyticsSeriesPoint, AnalyticsWorkloadPoint, ProjectAnalytics } from "../../features/pm/types";
import { Card } from "../../shared/ui/Card";

const palette = ["#22c55e", "#38bdf8", "#f59e0b", "#f43f5e", "#a78bfa", "#14b8a6"];

function formatLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function KpiTile({ label, value, detail }: { label: string; value: string | number; detail: string }) {
  return (
    <div className="rounded-lg border border-slate-800 bg-slate-900/60 p-4">
      <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</p>
      <p className="mt-2 text-2xl font-semibold text-white">{value}</p>
      <p className="mt-1 text-xs text-slate-400">{detail}</p>
    </div>
  );
}

function DonutMeter({ label, value, accent }: { label: string; value: number; accent: string }) {
  const safeValue = Math.max(0, Math.min(100, value));
  const radius = 42;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference - (safeValue / 100) * circumference;

  return (
    <div className="flex items-center gap-4">
      <svg className="h-28 w-28 flex-none -rotate-90" viewBox="0 0 120 120" role="img" aria-label={label}>
        <circle cx="60" cy="60" r={radius} fill="none" stroke="#1e293b" strokeWidth="12" />
        <circle
          cx="60"
          cy="60"
          r={radius}
          fill="none"
          stroke={accent}
          strokeLinecap="round"
          strokeWidth="12"
          strokeDasharray={circumference}
          strokeDashoffset={offset}
        />
      </svg>
      <div>
        <p className="text-sm font-medium text-slate-300">{label}</p>
        <p className="mt-1 text-3xl font-semibold text-white">{safeValue.toFixed(1)}%</p>
      </div>
    </div>
  );
}

function BarChart({ title, data }: { title: string; data: AnalyticsSeriesPoint[] }) {
  const max = Math.max(...data.map((d) => d.count), 1);

  return (
    <Card title={title}>
      <div className="space-y-3">
        {data.map((point, index) => (
          <div key={point.key} className="grid grid-cols-[7rem_1fr_3rem] items-center gap-3 text-sm">
            <span className="truncate text-slate-300" title={formatLabel(point.label)}>
              {formatLabel(point.label)}
            </span>
            <div className="h-3 rounded-full bg-slate-800">
              <div
                className="h-3 rounded-full"
                style={{
                  width: `${(point.count / max) * 100}%`,
                  backgroundColor: palette[index % palette.length],
                }}
              />
            </div>
            <span className="text-right font-medium text-white">{point.count}</span>
          </div>
        ))}
      </div>
    </Card>
  );
}

function WorkloadChart({ rows }: { rows: AnalyticsWorkloadPoint[] }) {
  const max = Math.max(...rows.map((r) => r.assignedTasks), 1);

  return (
    <Card title="Workload">
      {rows.length === 0 ? (
        <p className="text-sm text-slate-400">No assigned tasks yet.</p>
      ) : (
        <div className="space-y-4">
          {rows.map((row) => (
            <div key={row.userId} className="space-y-2">
              <div className="flex items-center justify-between gap-3 text-sm">
                <span className="truncate text-slate-300" title={row.label}>
                  {row.label}
                </span>
                <span className="font-medium text-white">
                  {row.openTasks}/{row.assignedTasks} open
                </span>
              </div>
              <div className="h-3 overflow-hidden rounded-full bg-slate-800">
                <div
                  className="h-full rounded-full bg-cyan-400"
                  style={{ width: `${(row.assignedTasks / max) * 100}%` }}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </Card>
  );
}

export function AnalyticsDashboardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [analytics, setAnalytics] = useState<ProjectAnalytics | null>(null);

  useEffect(() => {
    if (!projectId) return;
    void (async () => {
      try {
        setAnalytics(await fetchProjectAnalytics(projectId));
      } catch {
        toast.error("Failed to load analytics.");
      }
    })();
  }, [projectId]);

  const kpis = analytics?.kpis;
  const updated = useMemo(() => {
    if (!analytics) return "";
    return new Intl.DateTimeFormat(undefined, { month: "short", day: "numeric", year: "numeric" }).format(
      new Date(`${analytics.asOfDate}T00:00:00`)
    );
  }, [analytics]);

  if (!analytics || !kpis) return <p className="text-slate-400">Loading analytics...</p>;

  return (
    <div className="w-full space-y-5">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h2 className="text-xl font-semibold text-white">Analytics</h2>
          <p className="mt-1 text-sm text-slate-400">Project health as of {updated}</p>
        </div>
      </div>

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <KpiTile label="Tasks" value={kpis.totalTasks} detail={`${kpis.openTasks} open, ${kpis.completedTasks} done`} />
        <KpiTile label="Overdue" value={kpis.overdueTasks} detail="Open tasks past due date" />
        <KpiTile label="Bugs" value={kpis.totalBugs} detail={`${kpis.openBugs} open, ${kpis.criticalBugs} critical`} />
        <KpiTile label="Sprints" value={kpis.totalSprints} detail={`${kpis.activeSprints} active sprint(s)`} />
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card>
          <DonutMeter label="Task completion" value={kpis.taskCompletionRate} accent="#22c55e" />
        </Card>
        <Card>
          <DonutMeter label="Bug resolution" value={kpis.bugResolutionRate} accent="#38bdf8" />
        </Card>
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <BarChart title="Task Status" data={analytics.taskStatus} />
        <BarChart title="Bug Status" data={analytics.bugStatus} />
        <BarChart title="Bug Severity" data={analytics.bugSeverity} />
        <BarChart title="Sprint Status" data={analytics.sprintStatus} />
      </div>

      <WorkloadChart rows={analytics.workload} />
    </div>
  );
}
