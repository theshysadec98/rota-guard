import { useCallback, useEffect, useRef, useState } from 'react';
import { api } from '../api/client';
import { ShiftRegisterModal } from '../components/ShiftRegisterModal';
import type { BoardShiftCard, Staff, WeekBoard } from '../types';
import { isMonday } from '../utils/week';
import type { SlotKind } from '../utils/slotTimes';
import { SLOT_LABELS, SLOT_ORDER, roleLabel } from '../utils/slotTimes';

interface Props {
  weekStart: string;
  onWeekStartChange: (week: string) => void;
  onNext: () => void;
  onShiftCountChange: (count: number) => void;
  onStaffCountChange?: (count: number) => void;
}

type ModalState =
  | { open: false }
  | {
      open: true;
      mode: 'create' | 'edit';
      date: string;
      slot: SlotKind;
      card?: BoardShiftCard;
    };

export function ScheduleBoardPage({
  weekStart,
  onWeekStartChange,
  onNext,
  onShiftCountChange,
  onStaffCountChange,
}: Props) {
  const excelInputRef = useRef<HTMLInputElement>(null);
  const [board, setBoard] = useState<WeekBoard | null>(null);
  const [staff, setStaff] = useState<Staff[]>([]);
  const [replaceWeek, setReplaceWeek] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [modal, setModal] = useState<ModalState>({ open: false });
  const refreshStaff = useCallback(async () => {
    const page = await api.staff({ size: 500, sort: 'name', dir: 'asc' });
    setStaff(page.items);
    onStaffCountChange?.(page.totalElements);
  }, [onStaffCountChange]);

  const loadBoard = useCallback(async () => {
    if (!isMonday(weekStart)) {
      setBoard(null);
      onShiftCountChange(0);
      return;
    }
    const data = await api.weekBoard(weekStart);
    setBoard(data);
    onShiftCountChange(data.summary.totalShifts);
  }, [weekStart, onShiftCountChange]);

  useEffect(() => {
    refreshStaff().catch(() => setStaff([]));
  }, [refreshStaff]);

  useEffect(() => {
    setLoading(true);
    loadBoard()
      .catch((e) => setError(String(e)))
      .finally(() => setLoading(false));
  }, [loadBoard]);

  const onExcel = async (file: File | undefined) => {
    if (!file) return;
    if (!isMonday(weekStart)) {
      setError('Chọn thứ Hai đầu tuần trước khi import Excel');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const r = await api.importRosterExcel(file, weekStart, replaceWeek);
      if (r.weekStart && r.weekStart !== weekStart) {
        onWeekStartChange(r.weekStart);
      }
      setMessage(
        `Import: ${r.shiftsCreated} ca mới, ${r.shiftsUpdated} cập nhật`,
      );
      await refreshStaff();
      await loadBoard();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
      if (excelInputRef.current) excelInputRef.current.value = '';
    }
  };

  const saveShift = async (payload: {
    staffId: number;
    startAt: string;
    endAt: string;
    shiftType: string;
    id?: number;
  }) => {
    const r = await api.importShifts({
      weekStart,
      replaceWeek: false,
      shifts: [payload],
    });
    setMessage(`Đã lưu: +${r.created} mới, ${r.updated} cập nhật`);
    await loadBoard();
  };

  const onDelete = async (card: BoardShiftCard) => {
    if (!confirm(`Xóa ca của ${card.staffName}?`)) return;
    setLoading(true);
    try {
      await api.deleteShift(card.shiftId);
      setMessage('Đã xóa ca');
      await loadBoard();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  const upcoming =
    board?.days
      .flatMap((d) =>
        SLOT_ORDER.flatMap((slot) =>
          (d.cells[slot] ?? []).map((c) => ({ ...c, date: d.date })),
        ),
      )
      .filter((c) => new Date(c.startAt).getTime() >= Date.now())
      .sort((a, b) => new Date(a.startAt).getTime() - new Date(b.startAt).getTime())
      .slice(0, 8) ?? [];

  const fairnessHint =
    board && board.summary.staffCount > 0
      ? Math.round(board.summary.totalShifts / board.summary.staffCount)
      : 0;

  return (
    <div className="card schedule-board">
      <h2>Bước 2 — Lịch trực tuần</h2>

      <div className="board-kpi">
        <div className="kpi-tile">
          <span className="kpi-value">{board?.summary.totalShifts ?? 0}</span>
          <span className="kpi-label">Tổng ca tuần</span>
        </div>
        <div className="kpi-tile">
          <span className="kpi-value">{board?.summary.staffCount ?? 0}</span>
          <span className="kpi-label">Nhân sự có ca</span>
        </div>
        <div className="kpi-tile">
          <span className="kpi-value">~{fairnessHint}</span>
          <span className="kpi-label">Ca / người (ước)</span>
        </div>
      </div>

      <div className="toolbar">
        <label>
          Tuần (thứ Hai)
          <input
            type="date"
            value={weekStart}
            onChange={(e) => onWeekStartChange(e.target.value)}
          />
        </label>
        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={replaceWeek}
            onChange={(e) => setReplaceWeek(e.target.checked)}
          />
          Thay cả tuần khi import Excel
        </label>
        <input
          ref={excelInputRef}
          type="file"
          accept=".xlsx,.xls"
          style={{ display: 'none' }}
          onChange={(e) => onExcel(e.target.files?.[0])}
        />
        <button
          type="button"
          className="import-excel-btn"
          disabled={loading}
          onClick={() => excelInputRef.current?.click()}
        >
          Import Excel
        </button>
        <a href="/samples/roster-weekly.xlsx" download className="sample-link">
          File mẫu
        </a>
      </div>

      {message && <div className="toast success">{message}</div>}
      {error && <div className="error">{error}</div>}
      {!isMonday(weekStart) && (
        <div className="error">Tuần phải bắt đầu từ thứ Hai (ISO).</div>
      )}

      {loading && !board && <p className="loading">Đang tải lịch…</p>}

      {board && (
        <div className="board-layout">
          <div className="week-grid-wrap">
            <table className="week-grid">
              <thead>
                <tr>
                  <th className="slot-col">Khung</th>
                  {board.days.map((d) => (
                    <th key={d.date}>
                      {d.label}
                      <span className="day-date">{d.date.slice(5)}</span>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {SLOT_ORDER.map((slot) => (
                  <tr key={slot} className={`slot-row slot-${slot.toLowerCase()}`}>
                    <th className="slot-col">{SLOT_LABELS[slot]}</th>
                    {board.days.map((day) => (
                      <td key={`${day.date}-${slot}`}>
                        <div className="cell-cards">
                          {(day.cells[slot] ?? []).map((card) => (
                            <div
                              key={card.shiftId}
                              className={`shift-card role-${card.role.toLowerCase()}`}
                            >
                              <strong>{card.staffName}</strong>
                              <span className="shift-meta">
                                {roleLabel(card.role)} · {card.department}
                              </span>
                              <div className="card-actions">
                                <button
                                  type="button"
                                  className="secondary card-btn"
                                  onClick={() =>
                                    setModal({
                                      open: true,
                                      mode: 'edit',
                                      date: day.date,
                                      slot,
                                      card,
                                    })
                                  }
                                >
                                  Sửa
                                </button>
                                <button
                                  type="button"
                                  className="secondary card-btn"
                                  onClick={() => onDelete(card)}
                                >
                                  Xóa
                                </button>
                              </div>
                            </div>
                          ))}
                          <button
                            type="button"
                            className="cell-add secondary"
                            onClick={() =>
                              setModal({
                                open: true,
                                mode: 'create',
                                date: day.date,
                                slot,
                              })
                            }
                          >
                            +
                          </button>
                        </div>
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <aside className="board-side">
            <h3>Ca trực sắp tới</h3>
            {upcoming.length === 0 ? (
              <p className="loading">Chưa có ca trong tương lai.</p>
            ) : (
              <ul className="upcoming-list">
                {upcoming.map((c) => (
                  <li key={c.shiftId}>
                    <strong>{c.staffName}</strong>
                    <br />
                    <span className="shift-meta">
                      {SLOT_LABELS[c.slot]} · {c.date.slice(5)}
                    </span>
                  </li>
                ))}
              </ul>
            )}
            <p className="loading side-note">
              Cảnh báo mệt mỏi: chạy bước 3 Phân tích.
            </p>
          </aside>
        </div>
      )}

      <details className="advanced-import">
        <summary>Nhập CSV / bảng danh sách ca</summary>
        <LegacyShiftTable weekStart={weekStart} staff={staff} />
      </details>

      <div style={{ marginTop: '1rem' }}>
        <button
          type="button"
          onClick={onNext}
          disabled={(board?.summary.totalShifts ?? 0) === 0}
        >
          Tiếp: Phân tích
        </button>
      </div>

      {modal.open && (
        <ShiftRegisterModal
          open
          mode={modal.mode}
          weekStart={weekStart}
          date={modal.date}
          slot={modal.slot}
          card={modal.card}
          staff={staff}
          onClose={() => setModal({ open: false })}
          onSaved={() => {}}
          onSave={saveShift}
        />
      )}
    </div>
  );
}

function LegacyShiftTable({
  weekStart,
  staff,
}: {
  weekStart: string;
  staff: Staff[];
}) {
  const [shifts, setShifts] = useState<
    import('../types').Shift[]
  >([]);

  useEffect(() => {
    if (!isMonday(weekStart)) return;
    api.shifts(weekStart).then(setShifts).catch(() => setShifts([]));
  }, [weekStart]);

  const name = (id: number) => staff.find((s) => s.id === id)?.name ?? `#${id}`;

  return (
    <table style={{ marginTop: '0.75rem' }}>
      <thead>
        <tr>
          <th>ID</th>
          <th>Nhân viên</th>
          <th>Bắt đầu</th>
          <th>Loại</th>
        </tr>
      </thead>
      <tbody>
        {shifts.map((s) => (
          <tr key={s.id} style={{ cursor: 'default' }}>
            <td>{s.id}</td>
            <td>{name(s.staffId)}</td>
            <td>{s.startAt}</td>
            <td>{s.shiftType}</td>
          </tr>
        ))}
        {shifts.length === 0 && (
          <tr>
            <td colSpan={4} className="loading">
              Chưa có ca.
            </td>
          </tr>
        )}
      </tbody>
    </table>
  );
}
