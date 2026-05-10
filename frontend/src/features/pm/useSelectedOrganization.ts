import { useCallback, useEffect, useState } from "react";

const KEY = "devsync.selectedOrganizationId";

export function useSelectedOrganization(organizations: { id: string }[] | null) {
  const [orgId, setOrgIdInternal] = useState<string | null>(() => {
    try {
      return localStorage.getItem(KEY);
    } catch {
      return null;
    }
  });

  const setOrgId = useCallback((id: string | null) => {
    setOrgIdInternal(id);
    try {
      if (id) localStorage.setItem(KEY, id);
      else localStorage.removeItem(KEY);
    } catch {
      /* ignore */
    }
  }, []);

  useEffect(() => {
    if (!organizations || organizations.length === 0) return;
    if (!orgId || !organizations.some((o) => o.id === orgId)) {
      setOrgId(organizations[0].id);
    }
  }, [organizations, orgId, setOrgId]);

  return { orgId, setOrgId };
}
