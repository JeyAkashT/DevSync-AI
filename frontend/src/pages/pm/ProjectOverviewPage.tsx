import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "sonner";

import { fetchProject, patchProject } from "../../features/pm/pmApi";
import type { ProjectDetail } from "../../features/pm/types";
import { Card } from "../../shared/ui/Card";

export function ProjectOverviewPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [proj, setProj] = useState<ProjectDetail | null>(null);
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!projectId) return;
    void (async () => {
      try {
        const p = await fetchProject(projectId);
        setProj(p);
        setDescription(p.description ?? "");
      } catch {
        toast.error("Failed to load project.");
      }
    })();
  }, [projectId]);

  async function save() {
    if (!projectId) return;
    setSaving(true);
    try {
      const p = await patchProject(projectId, { description });
      setProj(p);
      toast.success("Project updated.");
    } finally {
      setSaving(false);
    }
  }

  if (!proj) return <p className="text-slate-400">Loading project…</p>;

  return (
    <div className="grid w-full gap-4 md:grid-cols-2">
      <Card title="Highlights">
        <dl className="space-y-2 text-sm text-slate-300">
          <div className="flex justify-between gap-4">
            <dt>Key</dt>
            <dd className="font-mono text-white">{proj.key}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt>Status</dt>
            <dd className="text-white">{proj.status}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt>Tasks</dt>
            <dd className="text-white">{proj.taskCount}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt>Open bugs</dt>
            <dd className="text-white">{proj.bugCount}</dd>
          </div>
          <div className="flex justify-between gap-4">
            <dt>Sprint pulse</dt>
            <dd className="text-white">{proj.activeSprint ? "Active sprint detected" : "No active sprint"}</dd>
          </div>
        </dl>
      </Card>

      <Card title="Description">
        <textarea
          className="mb-3 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
          rows={6}
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
        <button
          type="button"
          disabled={saving}
          className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-500 disabled:opacity-50"
          onClick={() => void save()}
        >
          {saving ? "Saving…" : "Save description"}
        </button>
      </Card>
    </div>
  );
}
