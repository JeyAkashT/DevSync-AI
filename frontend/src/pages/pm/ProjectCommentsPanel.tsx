import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "sonner";

import { fetchTasksBoard, fetchTaskComments, postTaskComment } from "../../features/pm/pmApi";
import type { Comment, Task } from "../../features/pm/types";
import { Card } from "../../shared/ui/Card";

export function ProjectCommentsPanel() {
  const { projectId } = useParams<{ projectId: string }>();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [taskId, setTaskId] = useState<string>("");
  const [comments, setComments] = useState<Comment[]>([]);
  const [body, setBody] = useState("");

  useEffect(() => {
    if (!projectId) return;
    void (async () => {
      try {
        const ts = await fetchTasksBoard(projectId);
        setTasks(ts);
        if (ts.length > 0) setTaskId(ts[0].id);
      } catch {
        toast.error("Failed to load tasks.");
      }
    })();
  }, [projectId]);

  useEffect(() => {
    if (!taskId) return;
    void (async () => {
      try {
        const env = await fetchTaskComments(taskId);
        setComments(env.items);
      } catch {
        toast.error("Unable to load comments.");
      }
    })();
  }, [taskId]);

  async function submit(ev: React.FormEvent) {
    ev.preventDefault();
    if (!taskId || !body.trim()) return;
    await postTaskComment(taskId, { body: body.trim() });
    setBody("");
    const env = await fetchTaskComments(taskId);
    setComments(env.items);
    toast.success("Comment posted.");
  }

  const selected = tasks.find((t) => t.id === taskId);

  return (
    <div className="space-y-4">
      <Card title="Pick a task thread">
        {tasks.length === 0 ? (
          <p className="text-sm text-slate-500">Create tasks on the board tab first.</p>
        ) : (
          <select
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            value={taskId}
            onChange={(e) => setTaskId(e.target.value)}
          >
            {tasks.map((t) => (
              <option key={t.id} value={t.id}>
                {t.title}
              </option>
            ))}
          </select>
        )}
        <p className="mt-2 text-xs text-slate-500">{selected ? selected.description ?? "" : "No tasks yet."}</p>
      </Card>

      <Card title="Composer">
        <form onSubmit={(e) => void submit(e)} className="space-y-2">
          <textarea
            rows={3}
            className="w-full rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 text-sm text-white"
            placeholder="Add an update…"
            value={body}
            onChange={(e) => setBody(e.target.value)}
          />
          <button type="submit" disabled={!taskId} className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white disabled:opacity-40">
            Comment
          </button>
        </form>
      </Card>

      <Card title="Thread">
        <div className="space-y-3">
          {comments.map((c) => (
            <article key={c.id} className="rounded-lg border border-slate-800 bg-slate-950/60 p-3">
              <p className="text-xs uppercase tracking-wide text-slate-500">{c.authorEmail}</p>
              <p className="mt-1 whitespace-pre-wrap text-sm text-white">{c.body}</p>
              <p className="mt-2 text-xs text-slate-600">{new Date(c.createdAt).toLocaleString()}</p>
            </article>
          ))}
          {comments.length === 0 ? <p className="text-sm text-slate-500">No notes yet.</p> : null}
        </div>
      </Card>
    </div>
  );
}
