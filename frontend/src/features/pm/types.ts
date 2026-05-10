export type OrgSummary = { id: string; name: string; slug: string };

export type PageEnvelope<T> = {
  items: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
};

export type ProjectSummary = {
  id: string;
  name: string;
  key: string;
  status: string;
  ownerId: string | null;
  organizationId: string;
};

export type ProjectDetail = {
  id: string;
  organizationId: string;
  name: string;
  key: string;
  description: string | null;
  status: string;
  ownerId: string | null;
  repositoryUrl: string | null;
  taskCount: number;
  bugCount: number;
  activeSprint: boolean;
};

export type Task = {
  id: string;
  projectId: string;
  sprintId: string | null;
  title: string;
  description: string | null;
  priority: string;
  status: string;
  assigneeUserId: string | null;
  dueDate: string | null;
  position: number;
  createdAt: string;
  updatedAt: string;
};

export type Bug = {
  id: string;
  projectId: string;
  taskId: string | null;
  title: string;
  description: string | null;
  severity: string;
  status: string;
  reporterUserId: string;
  assigneeUserId: string | null;
  createdAt: string;
  updatedAt: string;
};

export type Sprint = {
  id: string;
  projectId: string;
  name: string;
  startDate: string;
  endDate: string;
  goal: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
};

export type Comment = {
  id: string;
  authorUserId: string;
  authorEmail: string;
  body: string;
  parentCommentId: string | null;
  createdAt: string;
};

export type ActivityEntry = {
  id: string;
  actorUserId: string | null;
  action: string;
  entityType: string;
  entityId: string;
  payload: Record<string, unknown> | null;
  createdAt: string;
};

export type AnalyticsSeriesPoint = {
  key: string;
  label: string;
  count: number;
};

export type AnalyticsWorkloadPoint = {
  userId: string;
  label: string;
  assignedTasks: number;
  openTasks: number;
};

export type ProjectAnalytics = {
  projectId: string;
  asOfDate: string;
  kpis: {
    totalTasks: number;
    completedTasks: number;
    openTasks: number;
    overdueTasks: number;
    taskCompletionRate: number;
    totalBugs: number;
    openBugs: number;
    criticalBugs: number;
    bugResolutionRate: number;
    totalSprints: number;
    activeSprints: number;
  };
  taskStatus: AnalyticsSeriesPoint[];
  bugStatus: AnalyticsSeriesPoint[];
  bugSeverity: AnalyticsSeriesPoint[];
  sprintStatus: AnalyticsSeriesPoint[];
  workload: AnalyticsWorkloadPoint[];
};

export const TASK_STATUSES = ["BACKLOG", "TODO", "IN_PROGRESS", "REVIEW", "DONE"] as const;
