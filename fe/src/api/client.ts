const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers: Record<string, string> = { ...(init?.headers as Record<string, string>) };
  if (init?.body && !(init.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }
  const res = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    const err = body as { error?: string; importErrors?: { message: string }[] };
    const details = err.importErrors?.map((e) => e.message).join('; ');
    throw new Error(details ? `${err.error ?? res.statusText}: ${details}` : (err.error ?? res.statusText));
  }
  return res.json() as Promise<T>;
}

export const api = {
  health: () => request<{ status: string; service: string }>('/health'),
  policies: () => request<import('../types').Policy[]>('/policies'),
  staff: (params?: {
    q?: string;
    department?: string;
    role?: string;
    page?: number;
    size?: number;
    sort?: string;
    dir?: 'asc' | 'desc';
  }) => {
    const qs = new URLSearchParams();
    if (params?.q) qs.set('q', params.q);
    if (params?.department) qs.set('department', params.department);
    if (params?.role) qs.set('role', params.role);
    if (params?.page != null) qs.set('page', String(params.page));
    if (params?.size != null) qs.set('size', String(params.size));
    if (params?.sort) qs.set('sort', params.sort);
    if (params?.dir) qs.set('dir', params.dir);
    const query = qs.toString();
    return request<import('../types').StaffPageResponse>(
      query ? `/staff?${query}` : '/staff',
    );
  },
  createStaff: (body: Omit<import('../types').Staff, 'id'>) =>
    request<import('../types').Staff>('/staff', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  updateStaff: (id: number, body: Omit<import('../types').Staff, 'id'>) =>
    request<import('../types').Staff>(`/staff/${id}`, {
      method: 'PUT',
      body: JSON.stringify(body),
    }),
  importStaff: (staff: Omit<import('../types').Staff, 'id'>[]) =>
    request<import('../types').StaffImportResponse>('/staff/import', {
      method: 'POST',
      body: JSON.stringify({ staff }),
    }),
  importStaffCsv: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return request<import('../types').StaffImportResponse>('/staff/import/csv', {
      method: 'POST',
      body: form,
    });
  },
  shifts: (weekStart: string) =>
    request<import('../types').Shift[]>(`/shifts?weekStart=${weekStart}`),
  weekBoard: (weekStart: string) =>
    request<import('../types').WeekBoard>(`/shifts/week-board?weekStart=${weekStart}`),
  deleteShift: (shiftId: number) =>
    fetch(`${API_BASE}/shifts/${shiftId}`, { method: 'DELETE' }).then((res) => {
      if (!res.ok) {
        throw new Error(res.statusText);
      }
    }),
  importShifts: (body: {
    weekStart: string;
    replaceWeek: boolean;
    shifts: {
      id?: number;
      staffId: number;
      startAt: string;
      endAt: string;
      shiftType: string;
    }[];
  }) =>
    request<import('../types').ShiftImportResponse>('/shifts/import', {
      method: 'POST',
      body: JSON.stringify({
        weekStart: body.weekStart,
        replaceWeek: body.replaceWeek,
        shifts: body.shifts.map((s) => ({
          ...(s.id != null ? { id: s.id } : {}),
          staffId: s.staffId,
          startAt: s.startAt,
          endAt: s.endAt,
          shiftType: s.shiftType,
        })),
      }),
    }),
  importRosterExcel: (file: File, weekStart: string, replaceWeek: boolean) => {
    const form = new FormData();
    form.append('file', file);
    const qs = new URLSearchParams({
      weekStart,
      replaceWeek: String(replaceWeek),
    });
    return request<import('../types').RosterImportResponse>(
      `/roster/import/excel?${qs}`,
      { method: 'POST', body: form },
    );
  },
  importShiftsCsv: (file: File, weekStart: string, replaceWeek: boolean) => {
    const form = new FormData();
    form.append('file', file);
    return request<import('../types').ShiftImportResponse>(
      `/shifts/import/csv?weekStart=${weekStart}&replaceWeek=${replaceWeek}`,
      { method: 'POST', body: form },
    );
  },
  runAnalysis: (weekStart: string, policyId: number) =>
    request<import('../types').RunAnalysisResponse>(
      `/analysis/run?weekStart=${weekStart}&policyId=${policyId}`,
      { method: 'POST' },
    ),
  report: (runId: number) =>
    request<import('../types').AnalysisReport>(`/analysis/${runId}/report`),
  policyDiff: (body: {
    weekStart: string;
    policyIdA: number;
    policyIdB: number;
  }) =>
    request<import('../types').PolicyDiffResponse>('/analysis/policy-diff', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  whatIf: (body: {
    weekStart: string;
    policyId: number;
    shiftChanges: { shiftId: number; newStaffId: number }[];
  }) =>
    request<import('../types').WhatIfResponse>('/analysis/what-if', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  suggestions: (body: {
    weekStart: string;
    policyId: number;
    runId?: number;
    targetRiskLevels?: string[];
    maxSuggestions?: number;
  }) =>
    request<import('../types').RosterSuggestionResponse>('/analysis/suggestions', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  reassignShift: (shiftId: number, newStaffId: number) =>
    request<import('../types').Shift>(`/shifts/${shiftId}/reassign`, {
      method: 'POST',
      body: JSON.stringify({ newStaffId }),
    }),
};
