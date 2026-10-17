import type { OrderStatusHistory } from '../types';
import { OrderStatusPill } from './StatusPill';

/** The life of an order, oldest first: each status with when it happened and why, if there was a reason. */
export function OrderTimeline({ history }: { history: OrderStatusHistory[] | null }) {
  if (!history) return <p className="loading-text">Cargando historial...</p>;
  if (history.length === 0) return <p className="auto-hint">Sin historial.</p>;

  return (
    <ol className="timeline">
      {history.map(entry => (
        <li key={entry.id}>
          <span className="timeline-dot" />
          <div>
            <OrderStatusPill status={entry.status} />
            <span className="timeline-date">
              {new Date(entry.changedAt).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' })}
            </span>
            {entry.reason && <p className="timeline-reason">{entry.reason}</p>}
          </div>
        </li>
      ))}
    </ol>
  );
}
