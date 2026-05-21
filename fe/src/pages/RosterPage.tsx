import { FormEvent, useCallback, useEffect, useRef, useState } from 'react';
import { api } from '../api/client';
import type { Shift, Staff } from '../types';
import { formatZonedDateTime, isMonday, toZonedDateTime } from '../utils/week';

interface Props {
  weekStart: string;
  onWeekStartChange: (week: string) => void;
  onNext: () => void;
  onShiftCountChange: (count: number) => void;
  onStaffCountChange?: (count: number) => void;
}

type ShiftForm = {
  staffId: number;
  startLocal: string;
  endLocal: string;
  shiftType: 'DAY' | 'NIGHT';
};

const emptyShift: ShiftForm = {
  staffId: 1,
  startLocal: '',
  endLocal: '',
  shiftType: 'DAY',
};

export function RosterPage({
  weekStart,
  onWeekStartChange,
  onNext,
  onShiftCountChange,
  onStaffCountChange,
}: Props) {
  const excelInputRef = useRef<HTMLInputElement>(null);
  const [staff, setStaff] = useState<Staff[]>([]);
  const [shifts, setShifts] = useState<Shift[]>([]);
  const [form, setForm] = useState(emptyShift);
  const [replaceWeek, setReplaceWeek] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const staffName = (id: number) => staff.find((s) => s.id === id)?.name ?? `#${id}`;

  const refreshStaff = useCallback(async () => {
    const page = await api.staff({ size: 500, sort: 'name', dir: 'asc' });
    const list = page.items;
    setStaff(list);
    onStaffCountChange?.(list.length);
  }, [onStaffCountChange]);

  const loadShifts = useCallback(async () => {
    if (!isMonday(weekStart)) {
      setShifts([]);
      onShiftCountChange(0);
      return;
    }
    const list = await api.shifts(weekStart);
    setShifts(list);
    onShiftCountChange(list.length);
  }, [weekStart, onShiftCountChange]);

  useEffect(() => {
    refreshStaff().catch(() => setStaff([]));
  }, [refreshStaff]);

  useEffect(() => {
    loadShifts().catch((e) => setError(String(e)));
  }, [loadShifts]);

  useEffect(() => {
    if (staff.length > 0 && form.staffId === 1) {
      setForm((f) => ({ ...f, staffId: staff[0].id }));
    }
  }, [staff, form.staffId]);

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
      const errNote =
        r.errors?.length > 0 ? `, ${r.errors.length} dòng cảnh báo` : '';
      setMessage(
        `Đã import: ${r.staffCreated} nhân sự mới, ${r.staffMatched} đã có; ` +
          `${r.shiftsCreated} ca mới, ${r.shiftsUpdated} ca cập nhật${errNote}`,
      );
      await refreshStaff();
      await loadShifts();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
      if (excelInputRef.current) {
        excelInputRef.current.value = '';
      }
    }
  };

  const onAddShift = async (e: FormEvent) => {
    e.preventDefault();
    if (!isMonday(weekStart)) {
      setError('Tuần phải bắt đầu từ thứ Hai');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const r = await api.importShifts({
        weekStart,
        replaceWeek: false,
        shifts: [
          {
            staffId: form.staffId,
            startAt: toZonedDateTime(form.startLocal),
            endAt: toZonedDateTime(form.endLocal),
            shiftType: form.shiftType,
          },
        ],
      });
      setMessage(`Đã lưu ca: +${r.created} mới, ${r.updated} cập nhật`);
      await loadShifts();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  const onCsv = async (file: File | undefined) => {
    if (!file) return;
    if (!isMonday(weekStart)) {
      setError('Tuần phải bắt đầu từ thứ Hai');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const r = await api.importShiftsCsv(file, weekStart, replaceWeek);
      const errNote =
        r.errors && r.errors.length > 0 ? `, ${r.errors.length} dòng cảnh báo` : '';
      setMessage(
        `CSV: ${r.created} ca mới, ${r.updated} ca cập nhật${errNote}`,
      );
      await loadShifts();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <h2>Bước 2 — Lịch tuần</h2>

      <div className="import-excel-box">
        <h3>Import file Excel</h3>
        <p className="loading" style={{ margin: '0 0 0.75rem' }}>
          Một sheet: họ tên, vai trò, khoa và cột T2–CN (giờ ca hoặc OFF).{' '}
          <a href="/samples/roster-weekly.xlsx" download>
            Tải file mẫu
          </a>
        </p>
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
            Thay thế ca cả tuần trước khi import
          </label>
        </div>
        <input
          ref={excelInputRef}
          type="file"
          accept=".xlsx,.xls,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
          style={{ display: 'none' }}
          onChange={(e) => onExcel(e.target.files?.[0])}
        />
        <button
          type="button"
          className="import-excel-btn"
          disabled={loading}
          onClick={() => excelInputRef.current?.click()}
        >
          {loading ? 'Đang xử lý…' : 'Chọn file Excel'}
        </button>
      </div>

      <details className="advanced-import">
        <summary>Nhập tay hoặc CSV</summary>
        <p className="loading" style={{ marginTop: '0.75rem' }}>
          CSV cần mã nhân viên (<code>staffId</code>).{' '}
          <a href="/samples/shifts-sample.csv" download>
            File mẫu CSV
          </a>
        </p>
        <div className="toolbar" style={{ marginTop: '0.5rem' }}>
          <label>
            Import CSV ca
            <input
              type="file"
              accept=".csv,text/csv"
              onChange={(e) => onCsv(e.target.files?.[0])}
            />
          </label>
        </div>

        <form className="toolbar" onSubmit={onAddShift} style={{ marginTop: '0.5rem' }}>
          <label>
            Nhân viên
            <select
              value={form.staffId}
              onChange={(e) => setForm({ ...form, staffId: Number(e.target.value) })}
            >
              {staff.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} (#{s.id})
                </option>
              ))}
            </select>
          </label>
          <label>
            Bắt đầu
            <input
              type="datetime-local"
              value={form.startLocal}
              onChange={(e) => setForm({ ...form, startLocal: e.target.value })}
              required
            />
          </label>
          <label>
            Kết thúc
            <input
              type="datetime-local"
              value={form.endLocal}
              onChange={(e) => setForm({ ...form, endLocal: e.target.value })}
              required
            />
          </label>
          <label>
            Loại ca
            <select
              value={form.shiftType}
              onChange={(e) =>
                setForm({ ...form, shiftType: e.target.value as ShiftForm['shiftType'] })
              }
            >
              <option value="DAY">DAY</option>
              <option value="NIGHT">NIGHT</option>
            </select>
          </label>
          <button type="submit" disabled={loading || staff.length === 0}>
            Thêm ca
          </button>
        </form>
      </details>

      {message && <div className="toast success">{message}</div>}
      {error && <div className="error">{error}</div>}
      {!isMonday(weekStart) && (
        <div className="error">Tuần phải bắt đầu từ thứ Hai (ISO).</div>
      )}

      <h3 style={{ marginTop: '1.25rem' }}>Lịch tuần đã nhập</h3>
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Nhân viên</th>
            <th>Bắt đầu (UTC)</th>
            <th>Kết thúc</th>
            <th>Loại</th>
            <th>Lần sửa</th>
          </tr>
        </thead>
        <tbody>
          {shifts.map((sh) => (
            <tr key={sh.id}>
              <td>{sh.id}</td>
              <td>{staffName(sh.staffId)}</td>
              <td>{formatZonedDateTime(sh.startAt)}</td>
              <td>{formatZonedDateTime(sh.endAt)}</td>
              <td>{sh.shiftType}</td>
              <td>{sh.revisionCount}</td>
            </tr>
          ))}
          {shifts.length === 0 && (
            <tr>
              <td colSpan={6} className="loading">
                Chưa có ca — import Excel hoặc thêm thủ công.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <div style={{ marginTop: '1rem' }}>
        <button type="button" onClick={onNext} disabled={shifts.length === 0}>
          Tiếp: Phân tích
        </button>
      </div>
    </div>
  );
}
