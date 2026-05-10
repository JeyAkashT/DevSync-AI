import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "sonner";

import { createTask, fetchTasksBoard, patchTask } from "../../features/pm/pmApi";
import type { Task } from "../../features/pm/types";
import { TASK_STATUSES } from "../../features/pm/types";
import { Card } from "../../shared/ui/Card";

function groupTasks(tasks: Task[]) {
  const map = new Map<string, Task[]>();
  TASK_STATUSES.forEach((s) => map.set(s, []));
  tasks.forEach((t) => {
    const bucket = map.get(t.status);
    if (bucket) bucket.push(t);
  });
  return map;
}

export function TaskBoardPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [title, setTitle] = useState("");

  useEffect(() => {
    if (!projectId) return;
    void (async () => {
      try {
        const ts = await fetchTasksBoard(projectId);
        setTasks(ts);
      } catch {
        toast.error("Unable to fetch board.");
      }
    })();
  }, [projectId]);

  async function reload() {
    if (!projectId) return;
    const ts = await fetchTasksBoard(projectId);
    setTasks(ts);
  }

  async function addTask(ev: React.FormEvent) {
    ev.preventDefault();
    if (!projectId || !title.trim()) return;
    try {
      await createTask(projectId, {
        title: title.trim(),
        priority: "MEDIUM",
        status: "BACKLOG",
      });
      setTitle("");
      await reload();
      toast.success("Task created.");
    } catch {
      /* toast via interceptor */
    }
  }

  async function bumpStatus(task: Task, next: string) {
    try {
      await patchTask(task.id, { status: next });
      await reload();
    } catch {
      /* interceptor */
    }
  }

  const grouped = groupTasks(tasks);

  return (
    <div className="w-full space-y-4">
      <form onSubmit={(e) => void addTask(e)} className="flex flex-wrap items-end gap-2">
        <input
          placeholder="New task title"
          className="min-w-[12rem] flex-1 rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <button type="submit" className="rounded-lg bg-sky-600 px-4 py-2 text-sm font-medium text-white">
          Add
        </button>
      </form>

      <div className="grid gap-3 md:grid-cols-2 xl:grid-cols-5">
        {TASK_STATUSES.map((col) => (
          <Card key={col} title={col.replaceAll("_", " ")}>
            <div className="space-y-2">
              {(grouped.get(col) ?? []).map((t) => (
                <div key={t.id} className="rounded-lg border border-slate-800 bg-slate-950 px-3 py-2">
                  <p className="text-sm font-medium text-white">{t.title}</p>
                  <p className="text-xs text-slate-500">{t.priority}</p>
                  <select
                    className="mt-2 w-full rounded-md border border-slate-700 bg-slate-900 px-2 py-1 text-xs text-white"
                    value={t.status}
                    onChange={(e) => void bumpStatus(t, e.target.value)}
                  >
                    {TASK_STATUSES.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </div>
              ))}
              {(grouped.get(col) ?? []).length === 0 ? (
                <p className="text-xs text-slate-600">Drag-free column — backlog empty.</p>
              ) : null}
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
