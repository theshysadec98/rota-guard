export function toMonday(date: Date = new Date()): string {
  const d = new Date(date);
  const day = d.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  d.setDate(d.getDate() + diff);
  return d.toISOString().slice(0, 10);
}

export function isMonday(weekStart: string): boolean {
  const d = new Date(weekStart + 'T12:00:00');
  return d.getDay() === 1;
}

export const APP_TIME_ZONE = 'Asia/Ho_Chi_Minh';

export function toZonedDateTime(local: string): string {
  if (!local) return '';
  const normalized = local.length === 16 ? `${local}:00` : local;
  return `${normalized}+07:00`;
}

export function formatZonedDateTime(iso: string): string {
  try {
    return new Date(iso).toLocaleString('vi-VN', { timeZone: APP_TIME_ZONE });
  } catch {
    return iso;
  }
}
