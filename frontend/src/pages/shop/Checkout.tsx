import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { CreditCard } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { accountApi } from '../../services/api';
import { useToast } from '../../context/ToastContext';
import { OrderStatusPill } from '../../components/StatusPill';
import { money } from './shared';

/** Review of an order created from the cart: the customer pays it, leaves it for later, or cancels it. */
export function Checkout() {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { data: order, loading, error } = useApi(() => accountApi.getOrder(Number(orderId)), [orderId]);
  const [busy, setBusy] = useState(false);

  const act = async (action: () => Promise<unknown>, done: string, goTo: string) => {
    setBusy(true);
    try {
      await action();
      showToast(done, 'success');
      navigate(goTo);
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo completar la acción', 'error');
      setBusy(false);
    }
  };

  if (loading) return <p className="loading-text">Cargando...</p>;
  if (error || !order) return <div className="card empty-state"><p>No encontramos esa orden.</p><Link to="/my-orders" className="btn btn-primary">Ver mis órdenes</Link></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Revisa tu orden #{order.id}</h2>
          <p className="page-subtitle">Los productos ya están separados para ti. Págala para que la despachemos.</p>
        </div>
        <OrderStatusPill status={order.status} />
      </div>

      <div className="two-columns">
        <div className="card">
          <h3>Productos</h3>
          {order.items.map(item => (
            <div key={item.id} className="cart-line">
              <div className="cart-line-info">
                <strong>{item.productName}</strong>
                <span className="mono">{item.quantity} × {money(item.unitPrice)}</span>
              </div>
              <strong className="mono">{money(item.subtotal ?? item.unitPrice * item.quantity)}</strong>
            </div>
          ))}
          <h3 style={{ marginTop: 18 }}>Envío</h3>
          <p>{order.addressLine}</p>
          <p className="auto-hint">Si necesitas otra dirección, cámbiala desde Mis órdenes antes de que la despachemos.</p>
        </div>

        <div className="card summary">
          <h3>Pago</h3>
          <div className="summary-row summary-total"><span>Total</span><span>{money(order.total ?? 0)}</span></div>
          {order.status === 'CREATED' ? (
            <>
              <button className="btn btn-primary btn-block" disabled={busy}
                onClick={() => act(() => accountApi.pay(order.id), 'Pago recibido. Pronto despacharemos tu orden.', '/my-orders')}>
                <CreditCard size={16} /> Pagar {money(order.total ?? 0)}
              </button>
              <p className="auto-hint" style={{ margin: '10px 0' }}>Pago simulado: no se pide ni se guarda ningún dato de tarjeta.</p>
              <button className="btn btn-secondary btn-block" disabled={busy} onClick={() => navigate('/my-orders')}>Pagar después</button>
              <button className="btn btn-ghost btn-block" style={{ marginTop: 6 }} disabled={busy}
                onClick={() => act(() => accountApi.cancel(order.id), 'Orden cancelada.', '/')}>Cancelar orden</button>
            </>
          ) : (
            <Link to="/my-orders" className="btn btn-secondary btn-block">Ver mis órdenes</Link>
          )}
        </div>
      </div>
    </div>
  );
}
