import { FormEvent, useCallback, useEffect, useState } from 'react';
import { api } from '../api/client';
import type { Staff, StaffImportResponse } from '../types';

interface Props {
  onNext: () => void;
  onStaffChange: (count: number) => void;
}

const emptyForm = {
  name: '',
  role: 'NURSE',
  department: 'ICU',
  sensitivityFactor: 1,
};

export function StaffPage({ onNext, onStaffChange }: Props) {
  const [staff, setStaff] = useState<Staff[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    const page = await api.staff({ size: 500, sort: 'name', dir: 'asc' });
    setStaff(page.items);
    onStaffChange(page.items.length);
  }, [onStaffChange]);

  useEffect(() => {
    load().catch((e) => setError(String(e)));
  }, [load]);

  const showImportResult = (label: string, r: StaffImportResponse) => {
    const err =
      r.errors?.length > 0
        ? ` (${r.errors.length} dòng lỗi)`
        : '';
    setMessage(`${label}: thêm ${r.created}, bỏ qua ${r.skipped}${err}`);
  };

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      if (editingId) {
        await api.updateStaff(editingId, form);
        setMessage('Đã cập nhật nhân sự');
      } else {
        await api.createStaff(form);
        setMessage('Đã thêm nhân sự');
      }
      setForm(emptyForm);
      setEditingId(null);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  const onCsv = async (file: File | undefined) => {
    if (!file) return;
    setLoading(true);
    setError(null);
    try {
      const r = await api.importStaffCsv(file);
      showImportResult('CSV', r);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  const startEdit = (s: Staff) => {
    setEditingId(s.id);
    setForm({
      name: s.name,
      role: s.role,
      department: s.department,
      sensitivityFactor: s.sensitivityFactor,
    });
  };

  return (
    <div className="card">
      <h2>Bước 1 — Nhân sự</h2>
      <p className="loading" style={{ marginTop: 0 }}>
        Có thể nhập từng người hoặc file CSV. Toàn bộ tuần có thể import Excel ở bước 2.{' '}
        <a href="/samples/staff-sample.csv" download>
          File mẫu CSV
        </a>
      </p>

      {message && <div className="toast success">{message}</div>}
      {error && <div className="error">{error}</div>}

      <form className="toolbar" onSubmit={onSubmit} style={{ marginBottom: '1rem' }}>
        <label>
          Họ tên
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            required
          />
        </label>
        <label>
          Vai trò
          <select
            value={form.role}
            onChange={(e) => setForm({ ...form, role: e.target.value })}
          >
            <option value="NURSE">NURSE</option>
            <option value="DOCTOR">DOCTOR</option>
          </select>
        </label>
        <label>
          Khoa
          <input
            value={form.department}
            onChange={(e) => setForm({ ...form, department: e.target.value })}
            required
          />
        </label>
        <label>
          Hệ số
          <input
            type="number"
            step="0.1"
            min="0.1"
            value={form.sensitivityFactor}
            onChange={(e) =>
              setForm({ ...form, sensitivityFactor: Number(e.target.value) })
            }
          />
        </label>
        <button type="submit" disabled={loading}>
          {editingId ? 'Lưu' : 'Thêm'}
        </button>
        {editingId && (
          <button
            type="button"
            className="secondary"
            onClick={() => {
              setEditingId(null);
              setForm(emptyForm);
            }}
          >
            Huỷ
          </button>
        )}
        <label>
          Nhập CSV
          <input
            type="file"
            accept=".csv,text/csv"
            onChange={(e) => onCsv(e.target.files?.[0])}
          />
        </label>
      </form>

      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên</th>
            <th>Vai trò</th>
            <th>Khoa</th>
            <th>Hệ số</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {staff.map((s) => (
            <tr key={s.id}>
              <td>{s.id}</td>
              <td>{s.name}</td>
              <td>{s.role}</td>
              <td>{s.department}</td>
              <td>{s.sensitivityFactor}</td>
              <td>
                <button type="button" className="secondary" onClick={() => startEdit(s)}>
                  Sửa
                </button>
              </td>
            </tr>
          ))}
          {staff.length === 0 && (
            <tr>
              <td colSpan={6} className="loading">
                Chưa có nhân sự — thêm hoặc import CSV.
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
        <button type="button" onClick={onNext} disabled={staff.length === 0}>
          Tiếp: Lịch tuần
        </button>
      </div>
    </div>
  );
}
