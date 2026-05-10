import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "sonner";

import { fetchMyOrganizations, fetchProjects, createProject } from "../../features/pm/pmApi";
import type { OrgSummary, ProjectSummary } from "../../features/pm/types";
import { useSelectedOrganization } from "../../features/pm/useSelectedOrganization";
import { Card } from "../../shared/ui/Card";
import { DataTable, type Column } from "../../shared/ui/DataTable";

export function ProjectListPage() {
  const [orgs, setOrgs] = useState<OrgSummary[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [projects, setProjects] = useState<ProjectSummary[]>([]);
  const [q, setQ] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");

  const { orgId, setOrgId } = useSelectedOrganization(orgs);

  useEffect(() => {
    async function boot() {
      try {
        const o = await fetchMyOrganizations();
        setOrgs(o);
      } catch {
        toast.error("Unable to load organizations.");
      } finally {
        setLoading(false);
      }
    }
    void boot();
  }, []);

  useEffect(() => {
    if (typeof orgId !== "string") return;
    const organizationId = orgId;
    async function load() {
      try {
        const envelope = await fetchProjects(organizationId, q || undefined);
        setProjects(envelope.items);
      } catch {
        toast.error("Failed to fetch projects.");
      }
    }
    void load();
  }, [orgId, q]);

  async function submitCreate(ev: React.FormEvent) {
    ev.preventDefault();
    if (!orgId) return;
    try {
      await createProject(orgId, {
        name: name.trim(),
        description: description.trim() || undefined,
      });
      toast.success("Project created.");
      setCreateOpen(false);
      setName("");
      setDescription("");
      const envelope = await fetchProjects(orgId, q || undefined);
      setProjects(envelope.items);
    } catch {
      /* interceptor toast */
    }
  }

  const columns: Column<ProjectSummary>[] = [
    { header: "Name", cell: (p) => p.name },
    {
      header: "Key",
      cell: (p) => (
        <Link className="text-sky-400 hover:underline" to={`/projects/${p.id}/board`}>
          {p.key}
        </Link>
      ),
    },
    { header: "Status", cell: (p) => p.status },
  ];

  if (loading || !orgs) {
    return <p className="text-slate-400">Loading workspaces…</p>;
  }

  if (orgs.length === 0) {
    return (
      <Card title="Organizations">
        <p className="text-sm text-slate-400">
          You need an organization membership to use projects. Enable seed (`DEVSYNC_SEED_ENABLED=true`) or insert one in
          the database.
        </p>
      </Card>
    );
  }

  return (
    <div className="w-full space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-white">Projects</h1>
          <p className="text-sm text-slate-400">Org-scoped work tracking with JWT RBAC enforcement.</p>
        </div>
        <button
          type="button"
          className="rounded-lg bg-sky-600 px-4 py-2 text-sm font-medium text-white hover:bg-sky-500"
          onClick={() => setCreateOpen(true)}
        >
          New project
        </button>
      </div>

      <Card title="Organization">
        <select
          className="mt-2 w-full max-w-md rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
          value={orgId ?? ""}
          onChange={(e) => setOrgId(e.target.value || null)}
        >
          {orgs.map((o) => (
            <option key={o.id} value={o.id}>
              {o.name} ({o.slug})
            </option>
          ))}
        </select>
      </Card>

      <Card title="Search">
        <input
          placeholder="Search by name / description…"
          className="w-full max-w-md rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
      </Card>

      <Card title={`Projects (${projects.length})`}>
        <DataTable rows={projects} columns={columns} />
      </Card>

      {createOpen ? (
        <div className="fixed inset-0 flex items-center justify-center bg-black/60 p-4">
          <form
            onSubmit={(e) => void submitCreate(e)}
            className="w-full max-w-md space-y-4 rounded-xl border border-slate-800 bg-slate-900 p-6 shadow-xl"
          >
            <h2 className="text-lg font-semibold text-white">Create project</h2>
            <label className="block text-sm text-slate-300">
              Name
              <input
                required
                className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white"
                value={name}
                onChange={(e) => setName(e.target.value)}
              />
            </label>
            <label className="block text-sm text-slate-300">
              Description
              <textarea
                className="mt-1 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-white"
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </label>
            <div className="flex justify-end gap-2">
              <button
                type="button"
                className="rounded-md border border-slate-700 px-3 py-1.5 text-sm text-slate-300"
                onClick={() => setCreateOpen(false)}
              >
                Cancel
              </button>
              <button type="submit" className="rounded-md bg-sky-600 px-3 py-1.5 text-sm text-white">
                Create
              </button>
            </div>
          </form>
        </div>
      ) : null}
    </div>
  );
}
