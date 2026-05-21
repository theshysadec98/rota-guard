import { useCallback, useEffect, useState } from 'react';
import { api } from '../api/client';
import { PolicyDiffPanel } from '../components/PolicyDiffPanel';
import { StaffTable } from '../components/StaffTable';
import { ViolationPanel } from '../components/ViolationPanel';
import { SuggestionPanel } from '../components/SuggestionPanel';
import { WhatIfPanel } from '../components/WhatIfPanel';
import type { AnalysisReport, Policy, Shift, StaffRisk } from '../types';

interface Props {
  weekStart: string;
  policyId: number;
  onPolicyIdChange: (id: number) => void;
  canAnalyze: boolean;
}

export function AnalysisPage({
  weekStart,
  policyId,
  onPolicyIdChange,
  canAnalyze,
}: Props) {
  const [policies, setPolicies] = useState<Policy[]>([]);
  const [report, setReport] = useState<AnalysisReport | null>(null);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [selected, setSelected] = useState<StaffRisk | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.policies().then(setPolicies).catch((e) => setError(String(e)));
  }, []);

  useEffect(() => {
    api.shifts(weekStart).then(setShifts).catch(() => setShifts([]));
  }, [weekStart]);

  const analyze = useCallback(async () => {
    setLoading(true);
    setError(null);
    setSelected(null);
    try {
      const run = await api.runAnalysis(weekStart, policyId);
      const r = await api.report(run.runId);
      setReport(r);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      setReport(null);
    } finally {
      setLoading(false);
    }
  }, [weekStart, policyId]);

  const refreshAfterApply = useCallback(async () => {
    const list = await api.shifts(weekStart);
    setShifts(list);
    await analyze();
  }, [weekStart, analyze]);

  const churnThreshold =
    policies.find((p) => p.id === policyId)?.churnThreshold ?? 60;

  return (
    <div>
      <div className="card" style={{ marginBottom: '1rem' }}>
        <h2>Bước 3 — Phân tích</h2>
        <p className="loading" style={{ marginTop: 0 }}>
          Tuần bắt đầu <strong>{weekStart}</strong>
        </p>

        {error && <div className="error">{error}</div>}

        <div className="toolbar">
          <label>
            Quy định
            <select
              value={policyId}
              onChange={(e) => onPolicyIdChange(Number(e.target.value))}
            >
              {policies.map((p) => (
                <option key={p.id} value={p.id}>
                  {p.name}
                </option>
              ))}
              {policies.length === 0 && <option value={1}>Quy định mặc định</option>}
            </select>
          </label>
          <button
            type="button"
            onClick={analyze}
            disabled={loading || !canAnalyze}
            title={!canAnalyze ? 'Cần nhân sự và ca trong tuần' : undefined}
          >
            {loading ? 'Đang phân tích…' : 'Chạy phân tích'}
          </button>
        </div>
        {!canAnalyze && (
          <p className="loading">Hoàn thành bước 1–2 trước khi phân tích.</p>
        )}
      </div>

      {report && (
        <div className="summary">
          <span className="badge green">GREEN {report.summary.greenCount}</span>
          <span className="badge yellow">YELLOW {report.summary.yellowCount}</span>
          <span className="badge red">RED {report.summary.redCount}</span>
          <span style={{ color: 'var(--muted)', fontSize: '0.85rem' }}>
            Lần chạy #{report.runId}, {report.policyName}
          </span>
        </div>
      )}

      <div className="grid-2">
        <div className="card">
          <h2>Nhân sự &amp; rủi ro</h2>
          {report ? (
            <StaffTable
              rows={report.staffRisks}
              selectedId={selected?.staffId}
              churnThreshold={churnThreshold}
              onSelect={setSelected}
            />
          ) : (
            <p className="loading">Chạy phân tích để xem bảng rủi ro.</p>
          )}
        </div>
        <div className="card">
          <h2>Vi phạm</h2>
          <ViolationPanel staff={selected} />
        </div>
      </div>

      {report && (
        <SuggestionPanel
          weekStart={weekStart}
          policyId={policyId}
          report={report}
          onApplied={refreshAfterApply}
        />
      )}

      <div className="grid-2" style={{ marginTop: '1rem' }}>
        <PolicyDiffPanel weekStart={weekStart} policies={policies} />
        <WhatIfPanel weekStart={weekStart} policyId={policyId} shifts={shifts} />
      </div>
    </div>
  );
}
