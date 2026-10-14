import { useEffect, useState } from 'react';
import { useApi } from '../../hooks/useApi';
import { accountApi } from '../../services/api';
import { Address, Order, OrderStatus } from '../../types';
import { Modal } from '../../components/Modal';
import { useToast } from '../../context/ToastContext';
import { addressLabel } from '../../components/AddressFields';

const STATUS: Record<OrderStatus, { label: string; color: string }> = {
  CREATED: { label: 'Pendiente de pago', color: '#f59e0b' },
  PAID: { label: 'Pagada', color: '#3b82f6' },
  SHIPPED: { label: 'En camino', color: '#8b5cf6' },
  DELIVERED: { label: 'Entregada', color: '#10b981' },
  RETURN_REQUESTED: { label: 'Devolución en revisión', color: '#f97316' },
  CANCELLED: { label: 'Cancelada', color: '#ef4444' },
  RETURNED: { label: 'Devuelta', color: '#6b7280' },
};

const money = (value: number) => `$${value.toLocaleString('es-CO')}`;

// "T00:00" keeps the date in local time; a bare "2026-10-16" would be read as UTC and show the day before
const day = (date: string) => new Date(`${date}T00:00`).toLocaleDateString();

function deliveryInfo(order: Order) {
  if (order.deliveredOn) return `Entregada el ${day(order.deliveredOn)}`;
  if (order.estimatedDelivery && (order.status === 'PAID' || order.status === 'SHIPPED')) {
    return `Llega aproximadamente el ${day(order.estimatedDelivery)}`;
  }
  return '';
}

export function MyOrders() {
  const { showToast } = useToast();
  const { data: orders, loading, refetch, refresh } = useApi(() => accountApi.getOrders(), []);

  // Order whose return is being requested, with the reason being typed
  const [returning, setReturning] = useState<Order | null>(null);
  const [reason, setReason] = useState('');
  // Order whose delivery address is being changed
  const [moving, setMoving] = useState<Order | null>(null);
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [newAddressId, setNewAddressId] = useState('');

  // Orders are shipped and delivered by the store on its own, so keep the list up to date
  useEffect(() => {
    const timer = setInterval(refresh, 30000);
    return () => clearInterval(timer);
  }, [refresh]);

  const run = async (action: () => Promise<unknown>, done: string) => {
    try {
      await action();
      showToast(done, 'success');
      refetch();
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo completar la acción', 'error');
    }
  };

  const openReturn = (order: Order) => {
    setReason('');
    setReturning(order);
  };

  const submitReturn = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!returning) return;
    const order = returning;
    setReturning(null);
    await run(() => accountApi.return(order.id, reason),
      order.status === 'PAID' ? 'Compra devuelta: te reembolsamos el dinero.' : 'Solicitud de devolución enviada.');
  };

  const openMove = async (order: Order) => {
    const options = await accountApi.getAddresses();
    setAddresses(options);
    setNewAddressId(String((options.find(a => a.id === order.addressId) ?? options[0])?.id ?? ''));
    setMoving(order);
  };

  const submitMove = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!moving) return;
    const order = moving;
    setMoving(null);
    await run(() => accountApi.changeAddress(order.id, Number(newAddressId)), 'Dirección de entrega actualizada.');
  };

  if (loading) return <p>Cargando...</p>;

  const sorted = [...(orders ?? [])].sort((a, b) => b.id - a.id);

  return (
    <div className="page">
      {sorted.length === 0 && <p>Todavía no has hecho ninguna compra.</p>}

      {sorted.map(order => (
        <div key={order.id} className="order-card">
          <div className="order-card-header">
            <strong>Orden #{order.id}</strong>
            <span className="status-badge" style={{ background: STATUS[order.status].color }}>{STATUS[order.status].label}</span>
          </div>
          <p className="auto-hint">
            Comprada el {new Date(order.createdAt).toLocaleDateString()}
            {deliveryInfo(order) && ` · ${deliveryInfo(order)}`}
          </p>
          <ul className="order-card-items">
            {order.items.map(item => (
              <li key={item.id}>{item.productName} × {item.quantity}</li>
            ))}
          </ul>
          <p>Enviar a: {order.addressLine}</p>
          <div className="order-card-footer">
            <strong>{money(order.total ?? 0)}</strong>
            <div className="actions">
              {order.status === 'CREATED' && (
                <>
                  <button className="btn btn-sm btn-primary" onClick={() => run(() => accountApi.pay(order.id), 'Pago recibido. Pronto despacharemos tu orden.')}>Pagar</button>
                  <button className="btn btn-sm btn-danger" onClick={() => run(() => accountApi.cancel(order.id), 'Orden cancelada.')}>Cancelar</button>
                </>
              )}
              {(order.status === 'CREATED' || order.status === 'PAID') && (
                <button className="btn btn-sm btn-secondary" onClick={() => openMove(order)}>Cambiar dirección</button>
              )}
              {order.status === 'PAID' && (
                <button className="btn btn-sm btn-danger" onClick={() => openReturn(order)}>Devolver compra</button>
              )}
              {order.status === 'DELIVERED' && (
                <button className="btn btn-sm btn-danger" onClick={() => openReturn(order)}>Solicitar devolución</button>
              )}
            </div>
          </div>
        </div>
      ))}

      <Modal isOpen={returning !== null} onClose={() => setReturning(null)} title={`Devolver la orden #${returning?.id ?? ''}`}>
        <form onSubmit={submitReturn}>
          <p className="auto-hint">
            {returning?.status === 'PAID'
              ? 'Tu orden aún no ha salido de la tienda: se cancela y te devolvemos el dinero.'
              : 'Revisaremos el producto cuando lo recibamos de vuelta; el reembolso se hace si se acepta la devolución.'}
          </p>
          <div className="form-group">
            <label htmlFor="return-reason">Motivo (opcional)</label>
            <textarea id="return-reason" rows={3} maxLength={250} value={reason} onChange={e => setReason(e.target.value)} />
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setReturning(null)}>Volver</button>
            <button type="submit" className="btn btn-danger">Confirmar devolución</button>
          </div>
        </form>
      </Modal>

      <Modal isOpen={moving !== null} onClose={() => setMoving(null)} title={`Cambiar dirección de la orden #${moving?.id ?? ''}`}>
        <form onSubmit={submitMove}>
          <div className="form-group">
            <label htmlFor="new-address">Dirección de entrega</label>
            <select id="new-address" value={newAddressId} onChange={e => setNewAddressId(e.target.value)} required>
              {addresses.map(a => <option key={a.id} value={a.id}>{addressLabel(a)}</option>)}
            </select>
          </div>
          <p className="auto-hint">Solo se puede cambiar antes de que la orden sea enviada.</p>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setMoving(null)}>Volver</button>
            <button type="submit" className="btn btn-primary">Guardar</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
