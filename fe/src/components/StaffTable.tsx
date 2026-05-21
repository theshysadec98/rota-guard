import type { StaffRisk } from '../types';

interface Props {
  rows: StaffRisk[];
  selectedId?: number;
  churnThreshold: number;
  onSelect: (row: StaffRisk) => void;
}

export function StaffTable({
  rows,
  selectedId,
  churnThreshold,
  onSelect,
}: Props) {
  const sorted = [...rows].sort((a, b) => b.totalPoints - a.totalPoints);

  return (
    <table>
      <thead>
        <tr>
          <th>Nhân viên</th>
          <th>Điểm</th>
          <th>Rủi ro</th>
          <th>Lần sửa</th>
        </tr>
      </thead>
      <tbody>
        {sorted.map((row) => (
          <tr
            key={row.staffId}
            className={row.staffId === selectedId ? 'selected' : ''}
            onClick={() => onSelect(row)}
          >
            <td>{row.staffName}</td>
            <td>{row.totalPoints}</td>
            <td className={`risk-${row.riskLevel}`}>{row.riskLevel}</td>
            <td>
              {row.churnIndex}
              {row.churnIndex >= churnThreshold && (
                <span className="badge churn" title="Lịch sửa nhiều">
                  Cao
                </span>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
