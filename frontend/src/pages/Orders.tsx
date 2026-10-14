import { useEffect, useState } from 'react';
import { useApi } from '../hooks/useApi';
import { orderApi, customerApi, productApi } from '../services/api';
import { Address, CreateOrderRequest, Order } from '../types';
import { Modal } from '../components/Modal';
import { useToast } from '../context/ToastContext';
import { addressLabel } from '../components/AddressFields';

export function Orders() {
  const { data: orders, loading, refetch, refresh } = useApi(() => orderApi.getAll(), []);
  const { data: customers } = useApi(() => customerApi.getAll(), []);
  const { data: products } = useApi(() => productApi.getAllList(), []);

  const { showToast } = useToast();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState('');
  const [selectedAddress, setSelectedAddress] = useState('');
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [orderItems, setOrderItems] = useState<{ productId: number; quantity: number }[]>([]);
  const [selProduct, setSelProduct] = useState('');
  const [selQty, setSelQty] = useState(1);
  const [searchCustomer, setSearchCustomer] = useState('');
  const [searchStatus, setSearchStatus] = useState('');
  // Order whose delivery address is being changed, with the addresses its customer can choose from
  const [addressOrder, setAddressOrder] = useState<Order | null>(null);
  const [addressOptions, setAddressOptions] = useState<Address[]>([]);
  const [newAddressId, setNewAddressId] = useState('');

  // The backend ships and delivers orders on its own, so keep the table up to date
  useEffect(() => {
    const timer = setInterval(refresh, 30000);
    return () => clearInterval(timer);
  }, [refresh]);

  const activeCustomers =customers?.filter(c => c.status === 'ACTIVE') ?? [];
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

  const openAddressModal = async (order: Order) => {
    const options = await customerApi.getAddresses(order.customerId);
    setAddressOptions(options);
    // Some old orders point to an address their customer no longer has: start on the first one offered
    const current = options.find(a => a.id === order.addressId) ?? options[0];
    setNewAddressId(current ? String(current.id) : '');
    setAddressOrder(order);
  };

  const handleChangeAddress = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!addressOrder) return;
    try {
      await orderApi.changeAddress(addressOrder.id, Number(newAddressId));
      setAddressOrder(null);
      refetch();
    } catch {
      showToast('No se pudo cambiar la dirección. Si la orden ya fue enviada, ya no se puede cambiar.', 'error');
    }
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

  const handleStatus = async (id: number, action: string) => {
    switch (action) {
      case 'pay': await orderApi.pay(id); break;
      case 'cancel': await orderApi.cancel(id); break;
      case 'return': {
        const r = prompt('Motivo de la devolución (opcional):');
        if (r === null) return;
        await orderApi.return(id, r || '');
        break;
      }
      case 'confirmReturn': await orderApi.confirmReturn(id); break;
      case 'rejectReturn': {
        const r = prompt('Motivo del rechazo (opcional):');
        if (r === null) return;
        await orderApi.rejectReturn(id, r || '');
        break;
      }
    }
    refetch();
  };

  const statusColor = (status: string) => {
    const colors: Record<string, string> = {
      CREATED: '#f59e0b', PAID: '#3b82f6', SHIPPED: '#8b5cf6',
      DELIVERED: '#10b981', RETURN_REQUESTED: '#f97316', CANCELLED: '#ef4444', RETURNED: '#6b7280',
    };
    return colors[status] || '#6b7280';
  };

  // "T00:00" keeps the date in local time; a bare "2026-10-16" would be read as UTC and show the day before
  const toDate = (day: string) => new Date(`${day}T00:00`);

  // On its way: the date promised to the customer. Once delivered: the real date and how it compares
  const deliveryInfo = (order: Order) => {
    if (order.deliveredOn) {
      const delivered = toDate(order.deliveredOn);
      let note = '';
      if (order.estimatedDelivery) {
        const diff = Math.round((delivered.getTime() - toDate(order.estimatedDelivery).getTime()) / 86400000);
        const days = `${Math.abs(diff)} ${Math.abs(diff) === 1 ? 'día' : 'días'}`;
        note = diff === 0 ? ' (a tiempo)' : diff < 0 ? ` (${days} antes)` : ` (${days} tarde)`;
      }
      return `Entregada el ${delivered.toLocaleDateString()}${note}`;
    }
    if (order.estimatedDelivery && (order.status === 'PAID' || order.status === 'SHIPPED')) {
      return `Estimada: ${toDate(order.estimatedDelivery).toLocaleDateString()}`;
    }
    return '—';
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
          {['CREATED', 'PAID', 'SHIPPED', 'DELIVERED', 'RETURN_REQUESTED', 'CANCELLED', 'RETURNED'].map(s => (
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
              <th>Entrega</th>
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
                <td>{deliveryInfo(order)}</td>
                <td className="actions">
                  {order.status === 'CREATED' && (
                    <>
                      <button className="btn btn-sm btn-primary" onClick={() => handleStatus(order.id, 'pay')}>Pagar</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'cancel')}>Cancelar</button>
                    </>
                  )}
                  {(order.status === 'CREATED' || order.status === 'PAID') && (
                    <button className="btn btn-sm btn-secondary" onClick={() => openAddressModal(order)}>Cambiar dirección</button>
                  )}
                  {order.status === 'PAID' && (
                    <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'return')}>Devolver</button>
                  )}
                  {order.status === 'SHIPPED' && <span className="auto-hint">En camino</span>}
                  {order.status === 'DELIVERED' && (
                    <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'return')}>Solicitar devolución</button>
                  )}
                  {order.status === 'RETURN_REQUESTED' && (
                    <>
                      <button className="btn btn-sm btn-primary" onClick={() => handleStatus(order.id, 'confirmReturn')}>Producto recibido</button>
                      <button className="btn btn-sm btn-danger" onClick={() => handleStatus(order.id, 'rejectReturn')}>Rechazar devolución</button>
                    </>
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
                <option key={a.id} value={a.id}>{addressLabel(a)}</option>
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

      <Modal isOpen={addressOrder !== null} onClose={() => setAddressOrder(null)} title={`Cambiar dirección de la orden #${addressOrder?.id ?? ''}`}>
        <form onSubmit={handleChangeAddress}>
          <div className="form-group">
            <label>Dirección de entrega</label>
            <select value={newAddressId} onChange={e => setNewAddressId(e.target.value)} required>
              {addressOptions.map(a => <option key={a.id} value={a.id}>{addressLabel(a)}</option>)}
            </select>
          </div>
          <p className="auto-hint">Solo se puede cambiar antes de que la orden sea enviada. Las direcciones se agregan o editan en Clientes.</p>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setAddressOrder(null)}>Cancelar</button>
            <button type="submit" className="btn btn-primary">Guardar</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}