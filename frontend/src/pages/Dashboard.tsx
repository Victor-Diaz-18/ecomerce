import { useApi } from '../hooks/useApi';
import { orderApi, customerApi, productApi, inventoryApi, dashboardApi } from '../services/api';
import type { OrderStatus } from '../types';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line } from 'recharts';

const MONTH_NAMES = ['', 'Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'];
// Mismos estados que el backend cuenta como ingreso real (ver PurchaseOrderRepository)
const INCOME_STATUSES: OrderStatus[] = ['PAID', 'SHIPPED', 'DELIVERED', 'RETURN_REQUESTED'];
const PIE_COLORS = ['#10b981', '#3b82f6', '#8b5cf6', '#ef4444', '#f59e0b'];

export function Dashboard() {
  const { data: orders, loading: ordersLoading } = useApi(() => orderApi.getAll(), []);
  const { data: customers } = useApi(() => customerApi.getAll(), []);
  const { data: products } = useApi(() => productApi.getAllList(), []);
  const { data: inventories } = useApi(() => inventoryApi.getAll(), []);
  const { data: monthlyIncome } = useApi(() => dashboardApi.getMonthlyIncome(), []);
  const { data: bestSelling } = useApi(() => dashboardApi.getBestSellingProducts(), []);
  const { data: incomeByStatus } = useApi(() => dashboardApi.getIncomeByStatus(), []);

  const activeCustomers = customers?.filter(c => c.status === 'ACTIVE').length ?? 0;
  const activeProducts = products?.filter(p => p.active).length ?? 0;
  const lowStockItems = inventories?.filter(i => i.availableStock < i.minimumStock) ?? [];
  const pendingOrders = orders?.filter(o => o.status === 'CREATED').length ?? 0;
  const totalIncome = orders
    ?.filter(o => INCOME_STATUSES.includes(o.status))
    .reduce((sum, o) => sum + (o.total ?? 0), 0) ?? 0;
  const now = new Date();
  const thisMonthIncome = monthlyIncome?.find(m => m.year === now.getFullYear() && m.month === now.getMonth() + 1)?.totalIncome ?? 0;

  const monthlyData = monthlyIncome?.map(m => ({
    name: `${MONTH_NAMES[m.month] || `Mes ${m.month}`} ${String(m.year).slice(-2)}`,
    income: m.totalIncome,
  })) ?? [];

  const pieData = incomeByStatus?.map(s => ({
    name: s.status,
    value: s.totalIncome,
  })) ?? [];

  const criticalAlerts = lowStockItems.slice(0, 5);

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h2>Dashboard</h2>
        <p className="dashboard-subtitle">Resumen general de la tienda</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <h3>Ventas del mes</h3>
          <div className="value">${thisMonthIncome.toLocaleString()}</div>
        </div>
        <div className="stat-card">
          <h3>Ingresos acumulados</h3>
          <div className="value">${totalIncome.toLocaleString()}</div>
        </div>
        <div className="stat-card">
          <h3>Clientes activos</h3>
          <div className="value">{activeCustomers}</div>
        </div>
        <div className="stat-card">
          <h3>Alertas de stock</h3>
          <div className="value">{lowStockItems.length}</div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="dash-card">
          <h3>Ventas por mes</h3>
          {monthlyData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={monthlyData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value) => [`$${Number(value).toLocaleString()}`, 'Ingresos']} />
                <Line type="monotone" dataKey="income" stroke="#3b82f6" strokeWidth={2} dot={{ fill: '#3b82f6' }} />
              </LineChart>
            </ResponsiveContainer>
          ) : <p className="loading-text">Sin datos de ventas</p>}
        </div>

        <div className="dash-card">
          <h3>Ingresos por estado</h3>
          {pieData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%" cy="50%"
                  innerRadius={60} outerRadius={100}
                  paddingAngle={3}
                  dataKey="value"
                  label={({ name, percent }) => `${name} ${((percent ?? 0) * 100).toFixed(0)}%`}
                >
                  {pieData.map((_entry, index) => (
                    <Cell key={index} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => [`$${Number(value).toLocaleString()}`, 'Ingresos']} />
              </PieChart>
            </ResponsiveContainer>
          ) : <p className="loading-text">Sin datos de ingresos</p>}
        </div>

        <div className="dash-card">
          <h3>Productos mas vendidos</h3>
          {bestSelling && bestSelling.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={bestSelling.slice(0, 6)}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="productName" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip formatter={(value) => [value, 'Unidades']} />
                <Bar dataKey="totalSold" fill="#f59e0b" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <p className="loading-text">Sin datos de ventas</p>}
        </div>

        <div className="dash-card">
          <h3>Ordenes recientes</h3>
          {ordersLoading ? (
            <p className="loading-text">Cargando...</p>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Cliente</th>
                  <th>Estado</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {orders?.slice(0, 5).map(order => (
                  <tr key={order.id}>
                    <td>{order.id}</td>
                    <td>{order.customerName}</td>
                    <td><span className={`status-badge status-${order.status.toLowerCase()}`}>{order.status}</span></td>
                    <td>${order.total?.toLocaleString() ?? '0'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {criticalAlerts.length > 0 && (
        <div className="dash-card alerts-card">
          <h3>Alertas prioritarias - Stock bajo</h3>
          <div className="alerts-list">
            {criticalAlerts.map(inv => (
              <div key={inv.id} className="alert-item">
                <span className="alert-product">{inv.productName}</span>
                <span className="alert-stock">Stock: <strong>{inv.availableStock}</strong> / Min: {inv.minimumStock}</span>
                <span className="alert-urgent">Urgente</span>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="dash-card">
        <h3>Rendimiento rapido</h3>
        <div className="performance-grid">
          <div className="perf-item">
            <span className="perf-label">Total Ordenes</span>
            <span className="perf-value">{orders?.length ?? 0}</span>
          </div>
          <div className="perf-item">
            <span className="perf-label">Productos activos</span>
            <span className="perf-value">{activeProducts}</span>
          </div>
          <div className="perf-item">
            <span className="perf-label">Ordenes pendientes</span>
            <span className="perf-value pending">{pendingOrders}</span>
          </div>
          <div className="perf-item">
            <span className="perf-label">Stock bajo</span>
            <span className="perf-value alert">{lowStockItems.length}</span>
          </div>
        </div>
      </div>
    </div>
  );
}