import { useState } from 'react';
import { api } from '../api/client';
import type { Shift, WhatIfResponse } from '../types';

const SAMPLE_SHIFT_ID = 2;
const SAMPLE_STAFF_ID = 2;

interface Props {
  weekStart: string;
  policyId: number;
  shifts: Shift[];
}

export function WhatIfPanel({ weekStart, policyId, shifts }: Props) {
  const [shiftId, setShiftId] = useState(SAMPLE_SHIFT_ID);
  const [newStaffId, setNewStaffId] = useState(SAMPLE_STAFF_ID);
  const [result, setResult] = useState<WhatIfResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const run = async () => {
    setLoading(true);
    setError(null);
    try {
      const r = await api.whatIf({
        weekStart,
        policyId,
        shiftChanges: [{ shiftId, newStaffId }],
      });
      setResult(r);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  const fillSample = () => {
    setShiftId(SAMPLE_SHIFT_ID);
    setNewStaffId(SAMPLE_STAFF_ID);
  };

  return (
    <div className="card">
      <h2>Thử đổi ca</h2>
      <p style={{ color: 'var(--muted)', fontSize: '0.85rem', margin: '0 0 0.75rem' }}>
        Xem trước rủi ro nếu đổi người trực, chưa lưu lịch.
      </p>
      {error && <div className="error">{error}</div>}
      <div className="toolbar" style={{ marginBottom: '0.75rem', padding: '0.75rem' }}>
        <label>
          Ca
          <select value={shiftId} onChange={(e) => setShiftId(Number(e.target.value))}>
            {shifts.map((s) => (
              <option key={s.id} value={s.id}>
                Ca {s.id}, NV {s.staffId}, {s.shiftType}
              </option>
            ))}
            {shifts.length === 0 && (
              <option value={SAMPLE_SHIFT_ID}>Ca {SAMPLE_SHIFT_ID}</option>
            )}
          </select>
        </label>
        <label>
          Nhân viên mới
          <input
            type="number"
            min={1}
            value={newStaffId}
            onChange={(e) => setNewStaffId(Number(e.target.value))}
          />
        </label>
        <button type="button" className="secondary" onClick={fillSample}>
          Điền mẫu
        </button>
        <button type="button" onClick={run} disabled={loading}>
          {loading ? '…' : 'Xem trước'}
        </button>
      </div>
      {result && (
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
            {result.deltas
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
                    {d.changes.length > 0 && (
                      <ul style={{ margin: '0.25rem 0 0', paddingLeft: '1rem' }}>
                        {d.changes.map((c, i) => (
                          <li key={i} style={{ fontSize: '0.75rem' }}>
                            {c}
                          </li>
                        ))}
                      </ul>
                    )}
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
