import { createBrowserRouter, Navigate } from "react-router-dom";

import { DashboardPage } from "../pages/DashboardPage";
import { LoginPage } from "../pages/LoginPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { RegisterPage } from "../pages/RegisterPage";
import { ActivityTimelinePage } from "../pages/pm/ActivityTimelinePage";
import { AnalyticsDashboardPage } from "../pages/pm/AnalyticsDashboardPage";
import { BugTrackerPage } from "../pages/pm/BugTrackerPage";
import { ProjectCommentsPanel } from "../pages/pm/ProjectCommentsPanel";
import { ProjectListPage } from "../pages/pm/ProjectListPage";
import { ProjectOverviewPage } from "../pages/pm/ProjectOverviewPage";
import { ProjectWorkspaceLayout } from "../pages/pm/ProjectWorkspaceLayout";
import { SprintPlanningPage } from "../pages/pm/SprintPlanningPage";
import { TaskBoardPage } from "../pages/pm/TaskBoardPage";
import { GuestOnly } from "../features/auth/GuestOnly";
import { RequireAuth } from "../features/auth/RequireAuth";
import { AppLayout } from "./shell/AppLayout";

export const router = createBrowserRouter([
  {
    path: "/login",
    element: (
      <GuestOnly>
        <LoginPage />
      </GuestOnly>
    ),
  },
  {
    path: "/register",
    element: (
      <GuestOnly>
        <RegisterPage />
      </GuestOnly>
    ),
  },
  {
    element: (
      <RequireAuth>
        <AppLayout />
      </RequireAuth>
    ),
    children: [
      {
        path: "/",
        element: <DashboardPage />,
      },
      {
        path: "/projects",
        element: <ProjectListPage />,
      },
      {
        path: "/projects/:projectId",
        element: <ProjectWorkspaceLayout />,
        children: [
          {
            index: true,
            element: <Navigate to="board" replace />,
          },
          {
            path: "overview",
            element: <ProjectOverviewPage />,
          },
          {
            path: "board",
            element: <TaskBoardPage />,
          },
          {
            path: "bugs",
            element: <BugTrackerPage />,
          },
          {
            path: "sprints",
            element: <SprintPlanningPage />,
          },
          {
            path: "analytics",
            element: <AnalyticsDashboardPage />,
          },
          {
            path: "comments",
            element: <ProjectCommentsPanel />,
          },
          {
            path: "activity",
            element: <ActivityTimelinePage />,
          },
        ],
      },
      {
        path: "*",
        element: <NotFoundPage />,
      },
    ],
  },
]);
