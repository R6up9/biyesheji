export function formatTime(timeStr: string): string {
  if (!timeStr) return '';
  const t = new Date(timeStr);
  return `${t.getMonth() + 1}月${t.getDate()}日 ${String(t.getHours()).padStart(2, '0')}:${String(t.getMinutes()).padStart(2, '0')}`;
}

export function formatDateTime(timeStr: string): string {
  if (!timeStr) return '';
  const t = new Date(timeStr);
  const year = t.getFullYear();
  const month = String(t.getMonth() + 1).padStart(2, '0');
  const day = String(t.getDate()).padStart(2, '0');
  const hour = String(t.getHours()).padStart(2, '0');
  const minute = String(t.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hour}:${minute}`;
}
