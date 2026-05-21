import type { StaffRisk } from '../types';

function parseEvidence(json: string): string {
  try {
    const o = JSON.parse(json) as Record<string, unknown>;
    return Object.entries(o)
      .map(([k, v]) => `${k}: ${v}`)
      .join(', ');
  } catch {
    return json;
  }
}

interface Props {
  staff: StaffRisk | null;
}

export function ViolationPanel({ staff }: Props) {
  if (!staff) {
    return (
      <p className="loading">Chọn một nhân viên để xem vi phạm.</p>
    );
  }

  if (staff.violations.length === 0) {
    return <p className="loading">Không có vi phạm.</p>;
  }

  return (
    <>
      <p style={{ margin: '0 0 0.75rem', fontSize: '0.9rem' }}>
        <strong>{staff.staffName}</strong> — {staff.riskLevel} ({staff.totalPoints}{' '}
        điểm)
      </p>
      {staff.violations.map((v, i) => (
        <div key={`${v.ruleCode}-${i}`} className="violation">
          <strong>
            <code>{v.ruleCode}</code>
          </strong>{' '}
          <span className={`risk-${v.severity}`}>[{v.severity}]</span>
          <p style={{ margin: '0.35rem 0 0' }}>{v.message}</p>
          {v.evidenceJson ? (
            <p style={{ color: 'var(--muted)', margin: '0.25rem 0 0', fontSize: '0.8rem' }}>
              {parseEvidence(v.evidenceJson)}
            </p>
          ) : null}
        </div>
      ))}
    </>
  );
}
