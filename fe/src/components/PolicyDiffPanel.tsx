import { useState } from 'react';
import { api } from '../api/client';
import type { Policy, PolicyDiffResponse } from '../types';

interface Props {
  weekStart: string;
  policies: Policy[];
}

export function PolicyDiffPanel({ weekStart, policies }: Props) {
  const [policyA, setPolicyA] = useState(1);
  const [policyB, setPolicyB] = useState(2);
  const [result, setResult] = useState<PolicyDiffResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const run = async () => {
    setLoading(true);
    setError(null);
    try {
      const r = await api.policyDiff({
        weekStart,
        policyIdA: policyA,
        policyIdB: policyB,
      });
      setResult(r);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
      setResult(null);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>So sánh quy định</h2>
      <p style={{ color: 'var(--muted)', fontSize: '0.85rem', margin: '0 0 0.75rem' }}>
        Xem thêm bao nhiêu người bị RED khi siết quy định.
      </p>
      {error && <div className="error">{error}</div>}
      <div className="toolbar" style={{ marginBottom: '0.75rem', padding: '0.75rem' }}>
        <label>
          Quy định A
          <select value={policyA} onChange={(e) => setPolicyA(Number(e.target.value))}>
            {policies.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Quy định B
          <select value={policyB} onChange={(e) => setPolicyB(Number(e.target.value))}>
            {policies.map((p) => (
              <option key={p.id} value={p.id}>
                {p.name}
              </option>
            ))}
          </select>
        </label>
        <button type="button" onClick={run} disabled={loading}>
          {loading ? '…' : 'So sánh'}
        </button>
      </div>
      {result && (
        <>
          <p>
            <span className="badge red">
              +{result.additionalRedCount} RED
            </span>{' '}
            khi chuyển từ quy định {result.policyIdA} sang {result.policyIdB}
          </p>
          <table>
            <thead>
              <tr>
                <th>Nhân viên</th>
                <th>A</th>
                <th>B</th>
                <th>Δ điểm</th>
              </tr>
            </thead>
            <tbody>
              {result.diffs
                .filter((d) => d.riskA !== d.riskB || d.pointsDelta !== 0)
                .map((d) => (
                  <tr
                    key={d.staffId}
                    className={
                      d.riskB === 'RED' && d.riskA !== 'RED'
                        ? 'diff-row-worse'
                        : ''
                    }
                  >
                    <td>{d.staffName}</td>
                    <td className={`risk-${d.riskA}`}>{d.riskA}</td>
                    <td className={`risk-${d.riskB}`}>{d.riskB}</td>
                    <td>{d.pointsDelta > 0 ? `+${d.pointsDelta}` : d.pointsDelta}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}
