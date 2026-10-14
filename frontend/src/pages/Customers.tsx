import { useState } from 'react';
import { useApi, useMutation } from '../hooks/useApi';
import { customerApi, addressApi, locationApi } from '../services/api';
import { Customer, Address } from '../types';
import { Modal } from '../components/Modal';
import { useToast } from '../context/ToastContext';
import { AddressFields, AddressForm, EMPTY_ADDRESS, SHIPPING_COUNTRY, addressLabel, isComplete, streetLine, toForm } from '../components/AddressFields';

const toRequest = (a: AddressForm, customerId: number) => ({
  street: streetLine(a), neighborhood: a.neighborhood.trim(), city: a.city, country: SHIPPING_COUNTRY, customerId,
});

export function Customers() {
  const { data: customers, loading, refetch } = useApi(() => customerApi.getAll(), []);
  const { data: departments } = useApi(() => locationApi.getAll(), []);
  const { showToast } = useToast();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [formData, setFormData] = useState<{ name: string; email: string; status?: 'ACTIVE' | 'INACTIVE' }>({ name: '', email: '' });
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [newAddress, setNewAddress] = useState<AddressForm>(EMPTY_ADDRESS);
  const [pendingAddresses, setPendingAddresses] = useState<AddressForm[]>([]);
  const [editingAddressId, setEditingAddressId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  const { execute: createCustomer } = useMutation(customerApi.create);
  const { execute: updateCustomer } = useMutation(customerApi.update);
  const { execute: deleteCustomer } = useMutation(customerApi.delete);
  const { execute: createAddress } = useMutation(addressApi.create);
  const { execute: updateAddress } = useMutation(addressApi.update);

  const openCreateModal = () => {
    setEditingCustomer(null);
    setFormData({ name: '', email: '' });
    setAddresses([]);
    setPendingAddresses([]);
    setEditingAddressId(null);
    setNewAddress(EMPTY_ADDRESS);
    setIsModalOpen(true);
  };

  const openEditModal = async (customer: Customer) => {
    setEditingCustomer(customer);
    setFormData({ name: customer.name, email: customer.email, status: customer.status });
    const addrs = await customerApi.getAddresses(customer.id);
    setAddresses(addrs);
    setPendingAddresses([]);
    setEditingAddressId(null);
    setNewAddress(EMPTY_ADDRESS);
    setIsModalOpen(true);
  };

  const addAddress = async () => {
    const a = newAddress;
    if (!isComplete(a)) {
      showToast('Completa todos los campos de la dirección', 'error');
      return;
    }
    if (editingAddressId !== null) {
      const { street, neighborhood, city } = toRequest(a, 0);
      const addr = await updateAddress(editingAddressId, { street, neighborhood, city });
      if (!addr) {
        showToast('No se pudo guardar la dirección', 'error');
        return;
      }
      setAddresses(prev => prev.map(x => (x.id === addr.id ? addr : x)));
    } else if (editingCustomer) {
      const addr = await createAddress(toRequest(a, editingCustomer.id));
      if (addr) setAddresses(prev => [...prev, addr]);
      else showToast('No se pudo guardar la dirección', 'error');
    } else {
      setPendingAddresses(prev => [...prev, a]);
    }
    resetAddressForm();
  };

  const resetAddressForm = () => {
    setEditingAddressId(null);
    setNewAddress(EMPTY_ADDRESS);
  };

  const startEditingAddress = (address: Address) => {
    setEditingAddressId(address.id);
    setNewAddress(toForm(address));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    let customerId = editingCustomer?.id;
    try {
      if (customerId) {
        const result = await updateCustomer(customerId, { name: formData.name, email: formData.email, status: formData.status ?? 'ACTIVE' });
        if (result) {
          showToast('Cliente actualizado correctamente', 'success');
        } else {
          showToast('Error al actualizar cliente', 'error');
        }
      } else {
        const created = await createCustomer({ name: formData.name, email: formData.email });
        customerId = created?.id;
        if (created) showToast('Cliente creado correctamente', 'success');
      }
      if (customerId) {
        for (const addr of pendingAddresses) {
          await createAddress(toRequest(addr, customerId));
        }
      }
      setIsModalOpen(false);
      refetch();
    } catch {
      showToast('Error al guardar el cliente', 'error');
    } finally {
      setSaving(false);
    }
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
          {editingCustomer && (
            <div className="form-group">
              <label htmlFor="status">Estado</label>
              <select id="status" value={formData.status} onChange={e => setFormData({ ...formData, status: e.target.value as 'ACTIVE' | 'INACTIVE' })}>
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
          )}
          <div className="form-group">
            <label>Direcciones guardadas</label>
            <div className="addresses-list">
              {addresses.map(addr => (
                <div key={addr.id} className="address-item">
                  <span>{addressLabel(addr)}</span>
                  <button type="button" className="btn btn-secondary btn-sm" onClick={() => startEditingAddress(addr)}>Editar</button>
                </div>
              ))}
              {pendingAddresses.map((addr, i) => (
                <div key={i} className="address-item pending">
                  <span>{streetLine(addr)}, {addr.neighborhood}, {addr.city}, {addr.department}</span>
                  <span className="pending-badge">Pendiente</span>
                </div>
              ))}
            </div>
          </div>
          <div className="form-group">
            <label>{editingAddressId !== null ? 'Editar dirección' : 'Nueva dirección'} (solo envíos dentro de {SHIPPING_COUNTRY})</label>
            <div className="address-form">
              <AddressFields value={newAddress} onChange={setNewAddress} departments={departments} />
              <div className="address-row">
                <button type="button" className="btn btn-secondary btn-sm" onClick={addAddress}>
                  {editingAddressId !== null ? 'Guardar dirección' : 'Agregar dirección'}
                </button>
                {editingAddressId !== null && (
                  <button type="button" className="btn btn-secondary btn-sm" onClick={resetAddressForm}>Cancelar edición</button>
                )}
              </div>
            </div>
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
