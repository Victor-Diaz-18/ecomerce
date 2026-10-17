import { Link, useParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { accountApi } from '../../services/api';
import { OrderStatusPill } from '../../components/StatusPill';
import { OrderTimeline } from '../../components/OrderTimeline';
import { day, money, paymentDeadline, returnDeadline } from './shared';

/** Everything about one of the customer's orders: products, shipping, dates and the history of its statuses. */
export function OrderDetail() {
  const { orderId } = useParams();
  const id = Number(orderId);
  const { data: order, loading, error } = useApi(() => accountApi.getOrder(id), [id]);
  const { data: history } = useApi(() => accountApi.getOrderHistory(id), [id]);

  if (loading) return <p className="loading-text">Cargando...</p>;
  if (error || !order) {
    return (
      <div className="card empty-state">
        <p>No encontramos esa orden.</p>
        <Link to="/my-orders" className="btn btn-primary">Ver mis órdenes</Link>
      </div>
    );
  }

  const deadline = returnDeadline(order);

  return (
    <div className="page">
      <Link to="/my-orders" className="back-link"><ArrowLeft size={16} /> Mis órdenes</Link>
      <div className="page-header">
        <div>
          <h2>Orden #{order.id}</h2>
          <p className="page-subtitle">Comprada el {new Date(order.createdAt).toLocaleDateString()}</p>
        </div>
        <OrderStatusPill status={order.status} />
      </div>

      <div className="two-columns">
        <div>
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
            <div className="summary-row summary-total"><span>Total</span><span>{money(order.total ?? 0)}</span></div>
          </div>

          <div className="card">
            <h3>Historial</h3>
            <OrderTimeline history={history} />
          </div>
        </div>

        <div className="card summary">
          <h3>Envío</h3>
          <p>{order.addressLine}</p>
          <div className="detail-dates">
            {order.status === 'CREATED' && (
              <div className="summary-row"><span>Pagar antes de</span><span>{paymentDeadline(order)}</span></div>
            )}
            {order.estimatedDelivery && (
              <div className="summary-row"><span>Entrega estimada</span><span>{day(order.estimatedDelivery)}</span></div>
            )}
            {order.deliveredOn && (
              <div className="summary-row"><span>Entregada el</span><span>{day(order.deliveredOn)}</span></div>
            )}
            {deadline && order.status === 'DELIVERED' && (
              <div className="summary-row"><span>Devolución hasta</span><span>{deadline.toLocaleDateString()}</span></div>
            )}
          </div>
          <Link to="/my-orders" className="btn btn-secondary btn-block" style={{ marginTop: 14 }}>Volver a mis órdenes</Link>
        </div>
      </div>
    </div>
  );
}
