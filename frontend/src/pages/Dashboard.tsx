import { useApi } from '../hooks/useApi';
import { orderApi, customerApi, productApi, inventoryApi } from '../services/api';
import { Order, Customer, Product, Inventory } from '../types';

export function Dashboard() {
  const { data: orders, loading: ordersLoading } = useApi(() => orderApi.getAll(), []);
  const { data: customers } = useApi(() => customerApi.getAll(), []);
  const { data: products } = useApi(() => productApi.getAllList(), []);
  const { data: inventories } = useApi(() => inventoryApi.getAll(), []);

  const activeCustomers = customers?.filter(c => c.status === 'ACTIVE').length ?? 0;
  const activeProducts = products?.filter(p => p.active).length ?? 0;
  const lowStockCount = inventories?.filter(i => i.availableStock < i.minimumStock).length ?? 0;
  const pendingOrders = orders?.filter(o => o.status === 'CREATED').length ?? 0;

  const stats = [
    { label: 'Órdenes pendientes', value: pendingOrders, color: '#f59e0b' },
    { label: 'Clientes activos', value: activeCustomers, color: '#3b82f6' },
    { label: 'Productos activos', value: activeProducts, color: '#10b981' },
    { label: 'Stock bajo', value: lowStockCount, color: '#ef4444' },
  ];

  const recentOrders = orders?.slice(0, 5) ?? [];

  return (
    <div className="dashboard">
      <div className="stats-grid">
        {stats.map((stat, i) => (
          <div key={i} className="stat-card" style={{ borderLeftColor: stat.color }}>
            <div className="stat-value">{stat.value}</div>
            <div className="stat-label">{stat.label}</div>
          </div>
        ))}
      </div>

      <section className="section">
        <h3>Órdenes recientes</h3>
        {ordersLoading ? (
          <p>Cargando...</p>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Cliente</th>
                <th>Estado</th>
                <th>Total</th>
                <th>Fecha</th>
              </tr>
            </thead>
            <tbody>
              {recentOrders.map(order => (
                <tr key={order.id}>
                  <td>#{order.id}</td>
                  <td>{order.customerName}</td>
                  <td><span className={`status-badge status-${order.status.toLowerCase()}`}>{order.status}</span></td>
                  <td>${order.total?.toFixed(2) ?? '0.00'}</td>
                  <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}