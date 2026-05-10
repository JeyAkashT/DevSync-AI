import { http } from "../../shared/lib/http";

import type {
  ActivityEntry,
  Bug,
  Comment,
  OrgSummary,
  PageEnvelope,
  ProjectAnalytics,
  ProjectDetail,
  ProjectSummary,
  Sprint,
  Task,
} from "./types";

export async function fetchMyOrganizations(): Promise<OrgSummary[]> {
  const { data } = await http.get<OrgSummary[]>("/api/v1/me/organizations");
  return data;
}

export async function fetchProjects(orgId: string, q?: string): Promise<PageEnvelope<ProjectSummary>> {
  const params = new URLSearchParams({ page: "0", size: "50" });
  if (q) params.set("q", q);
  const { data } = await http.get<PageEnvelope<ProjectSummary>>(
    `/api/v1/organizations/${orgId}/projects?${params.toString()}`
  );
  return data;
}

export async function createProject(
  orgId: string,
  body: { name: string; key?: string; description?: string }
): Promise<ProjectDetail> {
  const { data } = await http.post<ProjectDetail>(`/api/v1/organizations/${orgId}/projects`, body);
  return data;
}

export async function fetchProject(projectId: string): Promise<ProjectDetail> {
  const { data } = await http.get<ProjectDetail>(`/api/v1/projects/${projectId}`);
  return data;
}

export async function patchProject(
  projectId: string,
  body: Record<string, unknown>
): Promise<ProjectDetail> {
  const { data } = await http.patch<ProjectDetail>(`/api/v1/projects/${projectId}`, body);
  return data;
}

export async function fetchTasksBoard(projectId: string): Promise<Task[]> {
  const { data } = await http.get<Task[]>(`/api/v1/projects/${projectId}/tasks/board`);
  return data;
}

export async function createTask(projectId: string, body: Record<string, unknown>): Promise<Task> {
  const { data } = await http.post<Task>(`/api/v1/projects/${projectId}/tasks`, body);
  return data;
}

export async function patchTask(taskId: string, body: Record<string, unknown>): Promise<Task> {
  const { data } = await http.patch<Task>(`/api/v1/tasks/${taskId}`, body);
  return data;
}

export async function fetchBugPage(projectId: string): Promise<PageEnvelope<Bug>> {
  const params = new URLSearchParams({ page: "0", size: "100" });
  const { data } = await http.get<PageEnvelope<Bug>>(`/api/v1/projects/${projectId}/bugs?${params}`);
  return data;
}

export async function createBug(projectId: string, body: Record<string, unknown>): Promise<Bug> {
  const { data } = await http.post<Bug>(`/api/v1/projects/${projectId}/bugs`, body);
  return data;
}

export async function patchBug(bugId: string, body: Record<string, unknown>): Promise<Bug> {
  const { data } = await http.patch<Bug>(`/api/v1/bugs/${bugId}`, body);
  return data;
}

export async function fetchSprints(projectId: string): Promise<PageEnvelope<Sprint>> {
  const params = new URLSearchParams({ page: "0", size: "50" });
  const { data } = await http.get<PageEnvelope<Sprint>>(
    `/api/v1/projects/${projectId}/sprints?${params}`
  );
  return data;
}

export async function createSprint(projectId: string, body: Record<string, unknown>): Promise<Sprint> {
  const { data } = await http.post<Sprint>(`/api/v1/projects/${projectId}/sprints`, body);
  return data;
}

export async function fetchTaskComments(taskId: string): Promise<PageEnvelope<Comment>> {
  const params = new URLSearchParams({ page: "0", size: "50" });
  const { data } = await http.get<PageEnvelope<Comment>>(`/api/v1/tasks/${taskId}/comments?${params}`);
  return data;
}

export async function postTaskComment(taskId: string, body: { body: string }): Promise<Comment> {
  const { data } = await http.post<Comment>(`/api/v1/tasks/${taskId}/comments`, body);
  return data;
}

export async function fetchActivity(projectId: string): Promise<PageEnvelope<ActivityEntry>> {
  const params = new URLSearchParams({ page: "0", size: "50" });
  const { data } = await http.get<PageEnvelope<ActivityEntry>>(
    `/api/v1/projects/${projectId}/activity?${params}`
  );
  return data;
}

export async function fetchProjectAnalytics(projectId: string): Promise<ProjectAnalytics> {
  const { data } = await http.get<ProjectAnalytics>(`/api/v1/projects/${projectId}/analytics`);
  return data;
}
