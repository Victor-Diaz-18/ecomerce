import { useState } from 'react';
import { useApi, useMutation } from '../hooks/useApi';
import { customerApi, addressApi } from '../services/api';
import { Customer, Address, CreateCustomerRequest, UpdateCustomerRequest, CreateAddressRequest } from '../types';
import { Modal } from '../components/Modal';

export function Customers() {
  const { data: customers, loading, refetch } = useApi(() => customerApi.getAll(), []);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [formData, setFormData] = useState<CreateCustomerRequest>({ name: '', email: '' });
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [newAddress, setNewAddress] = useState('');
  const [pendingAddresses, setPendingAddresses] = useState<string[]>([]);
  const [saving, setSaving] = useState(false);

  const { execute: createCustomer } = useMutation(customerApi.create);
  const { execute: updateCustomer } = useMutation(customerApi.update);
  const { execute: deleteCustomer } = useMutation(customerApi.delete);
  const { execute: createAddress } = useMutation(addressApi.create);

  const openCreateModal = () => {
    setEditingCustomer(null);
    setFormData({ name: '', email: '' });
    setAddresses([]);
    setPendingAddresses([]);
    setNewAddress('');
    setIsModalOpen(true);
  };

  const openEditModal = async (customer: Customer) => {
    setEditingCustomer(customer);
    setFormData({ name: customer.name, email: customer.email });
    const addrs = await customerApi.getAddresses(customer.id);
    setAddresses(addrs);
    setPendingAddresses([]);
    setNewAddress('');
    setIsModalOpen(true);
  };

  const addAddress = async () => {
    if (!newAddress.trim()) return;
    const parts = newAddress.split(',').map(p => p.trim()).filter(Boolean);
    if (parts.length < 3) {
      alert('Formato: Calle, Ciudad, País');
      return;
    }
    const [street, city, country] = parts;
    if (editingCustomer) {
      const addr = await createAddress({ street, city, country, customerId: editingCustomer.id });
      if (addr) setAddresses(prev => [...prev, addr]);
    } else {
      setPendingAddresses(prev => [...prev, newAddress]);
    }
    setNewAddress('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    let customerId = editingCustomer?.id;
    if (customerId) {
      await updateCustomer(customerId, { ...formData, status: editingCustomer.status });
    } else {
      const created = await createCustomer(formData);
      customerId = created?.id;
    }
    if (customerId) {
      for (const addr of pendingAddresses) {
        const parts = addr.split(',').map(p => p.trim()).filter(Boolean);
        if (parts.length === 3) {
          await createAddress({ street: parts[0], city: parts[1], country: parts[2], customerId: customerId! });
        }
      }
    }
    setIsModalOpen(false);
    setSaving(false);
    refetch();
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('¿Eliminar este cliente y sus direcciones?')) {
      await deleteCustomer(id);
      refetch();
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>Clientes</h2>
        <button className="btn btn-primary" onClick={openCreateModal}>Nuevo cliente</button>
      </div>

      {loading ? (
        <p>Cargando...</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Email</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {customers?.map(customer => (
              <tr key={customer.id}>
                <td>{customer.id}</td>
                <td>{customer.name}</td>
                <td>{customer.email}</td>
                <td><span className={`status-badge status-${customer.status.toLowerCase()}`}>{customer.status}</span></td>
                <td className="actions">
                  <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(customer)}>Editar</button>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(customer.id)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingCustomer ? 'Editar cliente' : 'Nuevo cliente'}
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Nombre</label>
            <input id="name" type="text" value={formData.name} onChange={e => setFormData({ ...formData, name: e.target.value })} required />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" type="email" value={formData.email} onChange={e => setFormData({ ...formData, email: e.target.value })} required />
          </div>
          <div className="form-group">
            <label>Direcciones guardadas</label>
            <div className="addresses-list">
              {addresses.map(addr => (
                <div key={addr.id} className="address-item">
                  <span>{addr.street}, {addr.city}, {addr.country}</span>
                </div>
              ))}
              {pendingAddresses.map((addr, i) => (
                <div key={i} className="address-item pending">
                  <span>{addr}</span>
                  <span className="pending-badge">Pendiente</span>
                </div>
              ))}
            </div>
          </div>
          <div className="form-group">
            <label>Nueva dirección</label>
            <input
              type="text"
              value={newAddress}
              onChange={e => setNewAddress(e.target.value)}
              placeholder="Calle 30 #3-90, Santa Marta, Colombia"
            />
            <button type="button" className="btn btn-secondary btn-sm" onClick={addAddress}>Agregar dirección</button>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {editingCustomer ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}