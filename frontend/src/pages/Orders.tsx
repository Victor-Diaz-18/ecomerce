import { useState, useEffect } from 'react';
import { useApi } from '../hooks/useApi';
import { orderApi, customerApi, addressApi, productApi } from '../services/api';
import { Order, Customer, Address, Product, CreateOrderRequest } from '../types';
import { Modal } from '../components/Modal';

export function Orders() {
  const { data: orders, loading, refetch } = useApi(() => orderApi.getAll(), []);
  const { data: customers } = useApi(() => customerApi.getAll(), []);
  const { data: products } = useApi(() => productApi.getAllList(), []);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [selectedAddress, setSelectedAddress] = useState('');
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [orderItems, setOrderItems] = useState<{ productId: number; quantity: number }[]>([]);
  const [selProduct, setSelProduct] = useState('');
  const [selQty, setSelQty] = useState(1);
  const [searchCustomer, setSearchCustomer] = useState('');
  const [searchStatus, setSearchStatus] = useState('');

  const activeCustomers = customers?.filter(c => c.status === 'ACTIVE') ?? [];
  const activeProducts = products?.filter(p => p.active) ?? [];

  const loadAddresses = async (customerId: number) => {
    const data = await customerApi.getAddresses(customerId);
    setAddresses(data);
  };

  const openCreateModal = () => {
    setSelectedCustomer('');
    setSelectedAddress('');
    setOrderItems([]);
    setSelProduct('');
    setSelQty(1);
    setIsModalOpen(true);
  };

  const handleCustomerChange = (customerId: string) => {
    setSelectedCustomer(customerId);
    setSelectedAddress('');
    if (customerId) loadAddresses(Number(customerId));
    else setAddresses([]);
  };

  const addItem = () => {
    if (!selProduct) return;
    const productId = Number(selProduct);
    const existing = orderItems.find(i => i.productId === productId);
    if (existing) {
      setOrderItems(orderItems.map(i =>
        i.productId === productId ? { ...i, quantity: i.quantity + selQty } : i
      ));
    } else {
      setOrderItems([...orderItems, { productId, quantity: selQty }]);
    }
    setSelProduct('');
    setSelQty(1);
  };

  const removeItem = (productId: number) => {
    setOrderItems(orderItems.filter(i => i.productId !== productId));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const data: CreateOrderRequest = {
      customerId: Number(selectedCustomer),
      addressId: Number(selectedAddress),
      items: orderItems,
    };
    await orderApi.create(data);
    setIsModalOpen(false);
    refetch();
  };

  const handleStatus = async (id: number, action: string, reason?: string) => {
    switch (action) {
      case 'pay': await orderApi.pay(id); break;
      case 'ship': await orderApi.ship(id); break;
      case 'deliver': await orderApi.deliver(id); break;
      case 'cancel': await orderApi.cancel(id); break;
      case 'return':
        const r = prompt('Motivo de la devolución (opcional):');
        if (r === null) return;
        await orderApi.return(id, r || '');
        break;
    }
    refetch();
  };

  const statusColor = (status: string) => {
    const colors: Record<string, string> = {
      CREATED: '#f59e0b', PAID: '#3b82f6', SHIPPED: '#8b5cf6',
      DELIVERED: '#10b981', CANCELLED: '#ef4444', RETURNED: '#6b7280',
    };
    return colors[status] || '#6b7280';
  };

  const filteredOrders = orders?.filter(o => {
    if (searchCustomer && o.customerId !== Number(searchCustomer)) return false;
    if (searchStatus && o.status !== searchStatus) return false;
    return true;
  });

  return (
    <div className="page">
      <div className="page-header">
        <h2>Órdenes</h2>
        <button className="btn btn-primary" onClick={openCreateModal}>Nueva orden</button>
      </div>

      <div className="filters">
        <select value={searchCustomer} onChange={e => setSearchCustomer(e.target.value)}>
          <option value="">Cliente</option>
          {activeCustomers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select value={searchStatus} onChange={e => setSearchStatus(e.target.value)}>
          <option value="">Estado</option>
          {['CREATED', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED', 'RETURNED'].map(s => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
        <button className="btn btn-secondary" onClick={refetch}>Actualizar</button>
      </div>

      {loading ? (
        <p>Cargando...</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Cliente</th>
              <th>Dirección</th>
              <th>Items</th>
              <th>Total</th>
              <th>Estado</th>
              <th>Fecha</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredOrders?.map(order => (
              <tr key={order.id}>
                <td>#{order.id}</td>
                <td>{order.customerName}</td>
                <td>{order.addressLine}</td>
                <td>{order.items?.length ?? 0} items</td>
                <td>${order.total?.toFixed(2) ?? '0.00'}</td>
                <td><span className="status-badge" style={{ background: statusColor(order.status) }}>{order.status}</span></td>
                <td>{new Date(order.createdAt).toLocaleDateString()}</td>
                <td className="actions">
                  {order.status === 'CREATED' && (
                    <>
                      <button className="btn btn-sm btn-primary" onClick={() => handleStatus(order.id, 'pay')}>Pagar</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'cancel')}>Cancelar</button>
                    </>
                  )}
                  {order.status === 'PAID' && (
                    <>
                      <button className="btn btn-sm btn-primary" onClick={() => handleStatus(order.id, 'ship')}>Enviar</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'cancel')}>Cancelar</button>
                    </>
                  )}
                  {order.status === 'SHIPPED' && (
                    <>
                      <button className="btn btn-sm btn-primary" onClick={() => handleStatus(order.id, 'deliver')}>Entregar</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'return')}>Devolver</button>
                    </>
                  )}
                  {order.status === 'DELIVERED' && (
                    <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'return')}>Devolver</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Nueva orden">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Cliente</label>
            <select value={selectedCustomer} onChange={e => handleCustomerChange(e.target.value)} required>
              <option value="">Selecciona</option>
              {activeCustomers.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Dirección</label>
            <select value={selectedAddress} onChange={e => setSelectedAddress(e.target.value)} required>
              <option value="">Selecciona</option>
              {addresses.map(a => (
                <option key={a.id} value={a.id}>{a.street}, {a.city}, {a.country}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Producto</label>
            <div className="inline-form">
              <select value={selProduct} onChange={e => setSelProduct(e.target.value)}>
                <option value="">Selecciona</option>
                {activeProducts.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
              <input type="number" min="1" value={selQty} onChange={e => setSelQty(Number(e.target.value))} />
              <button type="button" className="btn btn-secondary btn-sm" onClick={addItem}>Agregar</button>
            </div>
          </div>
          {orderItems.length > 0 && (
            <div className="order-items-list">
              <h4>Items ({orderItems.length})</h4>
              {orderItems.map((item, i) => {
                const product = activeProducts.find(p => p.id === item.productId);
                return (
                  <div key={i} className="order-item">
                    <span>{product?.name} x{item.quantity}</span>
                    <button type="button" className="btn btn-sm btn-danger" onClick={() => removeItem(item.productId)}>X</button>
                  </div>
                );
              })}
            </div>
          )}
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={orderItems.length === 0}>Crear orden</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}