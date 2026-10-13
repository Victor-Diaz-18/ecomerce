import { useState } from 'react';
import { useApi, useMutation } from '../hooks/useApi';
import { customerApi, addressApi, locationApi } from '../services/api';
import { Customer, Address } from '../types';
import { Modal } from '../components/Modal';
import { useToast } from '../context/ToastContext';

// The store only ships inside the country, so the country is not something the user types
const SHIPPING_COUNTRY = 'Colombia';

const STREET_TYPES = ['Calle', 'Carrera', 'Avenida', 'Avenida Calle', 'Avenida Carrera', 'Diagonal', 'Transversal', 'Circular', 'Autopista', 'Kilómetro', 'Manzana'];

// The parts of a Colombian address: "Calle 13 # 3-84, Centro, Santa Marta, Magdalena"
interface AddressForm {
  streetType: string;
  streetNumber: string;
  crossNumber: string;
  plateNumber: string;
  neighborhood: string;
  department: string;
  city: string;
}

const EMPTY_ADDRESS: AddressForm = {
  streetType: STREET_TYPES[0], streetNumber: '', crossNumber: '', plateNumber: '', neighborhood: '', department: '', city: '',
};

const streetLine = (a: AddressForm) =>
  `${a.streetType} ${a.streetNumber.trim()} # ${a.crossNumber.trim()}-${a.plateNumber.trim()}`;

const toRequest = (a: AddressForm, customerId: number) => ({
  street: streetLine(a), neighborhood: a.neighborhood.trim(), city: a.city, country: SHIPPING_COUNTRY, customerId,
});

// Puts a saved address back into the form. Old addresses may lack cross number, plate or
// neighbourhood: whatever is missing is left blank for the user to complete
function toForm(address: Address): AddressForm {
  const full = address.street.match(/^(.+?)\s+(\w+)\s*#\s*(\w+)\s*-\s*(\w+)$/);
  const partial = address.street.match(/^(.+?)\s+(\w+)$/);
  const [streetType, streetNumber, crossNumber, plateNumber] = full
    ? full.slice(1)
    : partial ? [partial[1], partial[2], '', ''] : ['', '', '', ''];
  const knownType = STREET_TYPES.find(t => t.toLowerCase() === streetType.toLowerCase());
  return {
    streetType: knownType ?? STREET_TYPES[0],
    streetNumber, crossNumber, plateNumber,
    neighborhood: address.neighborhood ?? '',
    department: address.department ?? '',
    city: address.city,
  };
}

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

  const citiesOfDepartment = departments?.find(d => d.name === newAddress.department)?.cities ?? [];

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
    if (!a.streetNumber.trim() || !a.crossNumber.trim() || !a.plateNumber.trim() || !a.neighborhood.trim() || !a.department || !a.city) {
      alert('Completa todos los campos de la dirección');
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
                  <span>{[addr.street, addr.neighborhood, addr.city, addr.department].filter(Boolean).join(', ')}</span>
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
              <div className="address-row">
                <select aria-label="Tipo de vía" value={newAddress.streetType} onChange={e => setNewAddress({ ...newAddress, streetType: e.target.value })}>
                  {STREET_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
                <input aria-label="Número de la vía" type="text" placeholder="13" maxLength={10} value={newAddress.streetNumber} onChange={e => setNewAddress({ ...newAddress, streetNumber: e.target.value })} />
                <span>#</span>
                <input aria-label="Número del cruce" type="text" placeholder="3" maxLength={10} value={newAddress.crossNumber} onChange={e => setNewAddress({ ...newAddress, crossNumber: e.target.value })} />
                <span>-</span>
                <input aria-label="Placa" type="text" placeholder="84" maxLength={10} value={newAddress.plateNumber} onChange={e => setNewAddress({ ...newAddress, plateNumber: e.target.value })} />
              </div>
              <input aria-label="Barrio" type="text" placeholder="Barrio" maxLength={100} value={newAddress.neighborhood} onChange={e => setNewAddress({ ...newAddress, neighborhood: e.target.value })} />
              <div className="address-row">
                <select aria-label="Departamento" value={newAddress.department} onChange={e => setNewAddress({ ...newAddress, department: e.target.value, city: '' })}>
                  <option value="">Departamento</option>
                  {departments?.map(d => <option key={d.name} value={d.name}>{d.name}</option>)}
                </select>
                <select aria-label="Ciudad" value={newAddress.city} disabled={!newAddress.department} onChange={e => setNewAddress({ ...newAddress, city: e.target.value })}>
                  <option value="">Ciudad</option>
                  {citiesOfDepartment.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
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
