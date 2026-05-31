export type SlotKind = 'MORNING' | 'AFTERNOON' | 'NIGHT';

export const SLOT_ORDER: SlotKind[] = ['MORNING', 'AFTERNOON', 'NIGHT'];

export const SLOT_LABELS: Record<SlotKind, string> = {
  MORNING: 'Sáng',
  AFTERNOON: 'Chiều',
  NIGHT: 'Đêm',
};

function addDays(isoDate: string, days: number): string {
  const d = new Date(isoDate + 'T12:00:00');
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}

export function slotToTimes(
  date: string,
  slot: SlotKind,
): { startAt: string; endAt: string; shiftType: string } {
  switch (slot) {
    case 'MORNING':
      return {
        startAt: `${date}T06:00:00+07:00`,
        endAt: `${date}T14:00:00+07:00`,
        shiftType: 'DAY',
      };
    case 'AFTERNOON':
      return {
        startAt: `${date}T14:00:00+07:00`,
        endAt: `${date}T22:00:00+07:00`,
        shiftType: 'DAY',
      };
    case 'NIGHT':
      return {
        startAt: `${date}T22:00:00+07:00`,
        endAt: `${addDays(date, 1)}T06:00:00+07:00`,
        shiftType: 'NIGHT',
      };
  }
}

export function roleLabel(role: string): string {
  if (role === 'DOCTOR') return 'Bác sĩ';
  if (role === 'NURSE') return 'Y tá';
  return role;
}
