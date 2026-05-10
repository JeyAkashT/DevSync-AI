import { Suspense } from "react";
import { NavLink, Outlet, useParams } from "react-router-dom";

import type { TabKey } from "./projectRoutes";

const tabClass = ({ isActive }: { isActive: boolean }) =>
  `rounded-md px-3 py-2 text-sm font-medium ${
    isActive ? "bg-slate-800 text-white" : "text-slate-400 hover:bg-slate-800/70 hover:text-white"
  }`;

function tabTo(projectId: string, segment: TabKey) {
  return `/projects/${projectId}/${segment}`;
}

export function ProjectWorkspaceLayout() {
  const { projectId } = useParams<{ projectId: string }>();
  if (!projectId) {
    return <p className="text-slate-400">Missing project.</p>;
  }

  return (
    <div className="flex w-full flex-col gap-4">
      <nav className="flex flex-wrap gap-2 rounded-lg border border-slate-800 bg-slate-900/70 p-2">
        <NavLink className={tabClass} to={tabTo(projectId, "overview")}>
          Overview
        </NavLink>
        <NavLink className={tabClass} to={tabTo(projectId, "board")}>
          Board
        </NavLink>
        <NavLink className={tabClass} to={tabTo(projectId, "bugs")}>
          Bugs
        </NavLink>
        <NavLink className={tabClass} to={tabTo(projectId, "sprints")}>
          Sprints
        </NavLink>
        <NavLink className={tabClass} to={tabTo(projectId, "analytics")}>
          Analytics
        </NavLink>
        <NavLink className={tabClass} to={tabTo(projectId, "comments")}>
          Comments
        </NavLink>
        <NavLink className={tabClass} to={tabTo(projectId, "activity")}>
          Activity
        </NavLink>
      </nav>
      <Suspense fallback={<div className="text-slate-400">Loading…</div>}>
        <Outlet />
      </Suspense>
    </div>
  );
}
