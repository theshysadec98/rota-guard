import { useState } from 'react';
import { api } from '../api/client';
import type {
  AnalysisReport,
  RosterSuggestionResponse,
  SuggestionItem,
  WhatIfResponse,
} from '../types';

interface Props {
  weekStart: string;
  policyId: number;
  report: AnalysisReport;
  onApplied?: () => void;
}

export function SuggestionPanel({ weekStart, policyId, report, onApplied }: Props) {
  const [includeYellow, setIncludeYellow] = useState(false);
  const [result, setResult] = useState<RosterSuggestionResponse | null>(null);
  const [preview, setPreview] = useState<WhatIfResponse | null>(null);
  const [previewShiftId, setPreviewShiftId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);
  const [applyingId, setApplyingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const targetLevels = includeYellow ? ['RED', 'YELLOW'] : ['RED'];
  const needsSuggestions =
    report.summary.redCount > 0 || (includeYellow && report.summary.yellowCount > 0);

  const load = async () => {
    setLoading(true);
    setError(null);
    setPreview(null);
    setPreviewShiftId(null);
    try {
      const r = await api.suggestions({
        weekStart,
        policyId,
        runId: report.runId,
        targetRiskLevels: targetLevels,
        maxSuggestions: 10,
      });
      setResult(r);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const previewSuggestion = async (item: SuggestionItem) => {
    setError(null);
    try {
      const r = await api.whatIf({
        weekStart,
        policyId,
        shiftChanges: [{ shiftId: item.shiftId, newStaffId: item.toStaffId }],
      });
      setPreview(r);
      setPreviewShiftId(item.shiftId);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  };

  const applySuggestion = async (item: SuggestionItem) => {
    if (!window.confirm(`Áp dụng: đổi ca #${item.shiftId} sang ${item.toStaffName}?`)) {
      return;
    }
    setApplyingId(item.shiftId);
    setError(null);
    try {
      await api.reassignShift(item.shiftId, item.toStaffId);
      onApplied?.();
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      setApplyingId(null);
    }
  };

  return (
    <div className="card" style={{ marginTop: '1rem' }}>
      <h2>Gợi ý lịch</h2>
      <p style={{ color: 'var(--muted)', fontSize: '0.85rem', margin: '0 0 0.75rem' }}>
        Gợi ý hỗ trợ sau phân tích; trưởng khoa quyết định cuối cùng.
      </p>

      {error && <div className="error">{error}</div>}

      <div className="toolbar" style={{ marginBottom: '0.75rem', padding: '0.75rem' }}>
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={includeYellow}
            onChange={(e) => {
              setIncludeYellow(e.target.checked);
              setResult(null);
            }}
          />
          Gồm cả YELLOW
        </label>
        <button type="button" onClick={load} disabled={loading || !needsSuggestions}>
          {loading ? 'Đang tính…' : 'Tải gợi ý'}
        </button>
      </div>

      {!needsSuggestions && (
        <p className="loading">Không có RED — không cần gợi ý điều chỉnh.</p>
      )}

      {result && (
        <>
          {result.warning && (
            <p className="loading" style={{ color: 'var(--yellow)' }}>
              {result.warning}
            </p>
          )}
          <p style={{ fontSize: '0.85rem', margin: '0 0 0.75rem' }}>
            Hiện tại: RED {result.baseline.redCount}, YELLOW {result.baseline.yellowCount},
            GREEN {result.baseline.greenCount}
          </p>
          {result.suggestions.length === 0 ? (
            <p className="loading">Chưa tìm được gợi ý cải thiện với bộ lọc hiện tại.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>#</th>
                  <th>Ca</th>
                  <th>Đổi</th>
                  <th>Tác động</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {result.suggestions.map((item) => (
                  <tr key={`${item.shiftId}-${item.toStaffId}`}>
                    <td>{item.rank}</td>
                    <td style={{ fontSize: '0.85rem' }}>
                      #{item.shiftId}
                      <br />
                      <span style={{ color: 'var(--muted)' }}>{item.shiftSummary}</span>
                    </td>
                    <td>
                      {item.fromStaffName} → {item.toStaffName}
                    </td>
                    <td style={{ fontSize: '0.8rem' }}>
                      {item.impact.targetRiskBefore} → {item.impact.targetRiskAfter}
                      {item.impact.targetPointsDelta !== 0 && (
                        <>
                          {' '}
                          ({item.impact.targetPointsDelta > 0 ? '+' : ''}
                          {item.impact.targetPointsDelta} điểm)
                        </>
                      )}
                      <br />
                      RED: {item.impact.redCountBefore} → {item.impact.redCountAfter}
                      <p style={{ margin: '0.25rem 0 0', color: 'var(--muted)' }}>
                        {item.explanation}
                      </p>
                    </td>
                    <td style={{ whiteSpace: 'nowrap' }}>
                      <button
                        type="button"
                        className="secondary"
                        style={{ marginRight: '0.35rem' }}
                        onClick={() => previewSuggestion(item)}
                      >
                        Xem trước
                      </button>
                      {onApplied && (
                        <button
                          type="button"
                          disabled={applyingId === item.shiftId}
                          onClick={() => applySuggestion(item)}
                        >
                          {applyingId === item.shiftId ? '…' : 'Áp dụng'}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {preview && previewShiftId != null && (
        <div style={{ marginTop: '1rem' }}>
          <h3 style={{ fontSize: '0.95rem' }}>Xem trước ca #{previewShiftId}</h3>
          <table>
            <thead>
              <tr>
                <th>Nhân viên</th>
                <th>Trước</th>
                <th>Sau</th>
                <th>Vi phạm</th>
              </tr>
            </thead>
            <tbody>
              {preview.deltas
                .filter(
                  (d) =>
                    d.riskBefore !== d.riskAfter ||
                    d.violationsBefore !== d.violationsAfter,
                )
                .map((d) => (
                  <tr key={d.staffId}>
                    <td>{d.staffName}</td>
                    <td className={`risk-${d.riskBefore}`}>{d.riskBefore}</td>
                    <td className={`risk-${d.riskAfter}`}>{d.riskAfter}</td>
                    <td>
                      {d.violationsBefore} → {d.violationsAfter}
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
