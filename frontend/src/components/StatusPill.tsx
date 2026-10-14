import type { OrderStatus } from '../types';

type Tone = 'blue' | 'green' | 'purple' | 'amber' | 'red' | 'gray';

const ORDER_STATUS: Record<OrderStatus, { label: string; tone: Tone }> = {
  CREATED: { label: 'Creada', tone: 'blue' },
  PAID: { label: 'Pagada', tone: 'green' },
  SHIPPED: { label: 'Despachada', tone: 'purple' },
  DELIVERED: { label: 'Entregada', tone: 'green' },
  RETURN_REQUESTED: { label: 'Devolución solicitada', tone: 'amber' },
  CANCELLED: { label: 'Cancelada', tone: 'red' },
  RETURNED: { label: 'Devuelta', tone: 'gray' },
};

export const orderStatusLabel = (status: OrderStatus) => ORDER_STATUS[status]?.label ?? status;

export function OrderStatusPill({ status, label }: { status: OrderStatus; label?: string }) {
  const info = ORDER_STATUS[status] ?? { label: status, tone: 'gray' as Tone };
  return <span className={`status-badge tone-${info.tone}`}>{label ?? info.label}</span>;
}

export function Pill({ tone, children }: { tone: Tone; children: React.ReactNode }) {
  return <span className={`status-badge tone-${tone}`}>{children}</span>;
}
