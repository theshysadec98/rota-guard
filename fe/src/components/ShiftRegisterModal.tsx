import { FormEvent, useEffect, useState } from 'react';
import type { BoardShiftCard, Staff } from '../types';
import type { SlotKind } from '../utils/slotTimes';
import { SLOT_LABELS, roleLabel, slotToTimes } from '../utils/slotTimes';

interface Props {
  open: boolean;
  mode: 'create' | 'edit';
  weekStart: string;
  date: string;
  slot: SlotKind;
  card?: BoardShiftCard;
  staff: Staff[];
  onClose: () => void;
  onSaved: () => void;
  onSave: (payload: {
    staffId: number;
    startAt: string;
    endAt: string;
    shiftType: string;
    id?: number;
  }) => Promise<void>;
}

export function ShiftRegisterModal({
  open,
  mode,
  weekStart,
  date,
  slot,
  card,
  staff,
  onClose,
  onSaved,
  onSave,
}: Props) {
  const [staffId, setStaffId] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    setError(null);
    if (mode === 'edit' && card) {
      setStaffId(card.staffId);
    } else if (staff.length > 0) {
      setStaffId(staff[0].id);
    }
  }, [open, mode, card, staff]);

  if (!open) return null;

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    if (!staffId) return;
    setLoading(true);
    setError(null);
    try {
      const times = slotToTimes(date, slot);
      await onSave({
        ...times,
        staffId,
        ...(mode === 'edit' && card ? { id: card.shiftId } : {}),
      });
      onSaved();
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="modal-panel"
        role="dialog"
        aria-labelledby="shift-modal-title"
        onClick={(e) => e.stopPropagation()}
      >
        <h3 id="shift-modal-title">
          {mode === 'create' ? 'Đăng ký ca' : 'Sửa ca'} — {SLOT_LABELS[slot]}
        </h3>
        <p className="loading">
          Ngày {date} · Tuần {weekStart}
        </p>
        <form onSubmit={onSubmit}>
          <label>
            Nhân viên
            <select
              value={staffId || ''}
              onChange={(e) => setStaffId(Number(e.target.value))}
              required
            >
              {staff.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} — {roleLabel(s.role)} · {s.department}
                </option>
              ))}
            </select>
          </label>
          {error && <div className="error">{error}</div>}
          <div className="modal-actions">
            <button type="button" className="secondary" onClick={onClose}>
              Hủy
            </button>
            <button type="submit" disabled={loading || staff.length === 0}>
              {loading ? 'Đang lưu…' : 'Lưu'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
