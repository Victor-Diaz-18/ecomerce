import { useState } from 'react';
import { useApi } from '../../hooks/useApi';
import { accountApi, locationApi } from '../../services/api';
import { Address } from '../../types';
import { useToast } from '../../context/ToastContext';
import { AddressFields, AddressForm, EMPTY_ADDRESS, SHIPPING_COUNTRY, addressLabel, isComplete, streetLine, toForm } from '../../components/AddressFields';

export function MyAddresses() {
  const { showToast } = useToast();
  const { data: addresses, loading, refetch } = useApi(() => accountApi.getAddresses(), []);
  const { data: departments } = useApi(() => locationApi.getAll(), []);
  const [form, setForm] = useState<AddressForm>(EMPTY_ADDRESS);
  // null while adding a new address
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);

  const reset = () => {
    setEditingId(null);
    setForm(EMPTY_ADDRESS);
  };

  const startEditing = (address: Address) => {
    setEditingId(address.id);
    setForm(toForm(address));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isComplete(form)) {
      showToast('Completa todos los campos de la dirección', 'error');
      return;
    }
    const data = { street: streetLine(form), neighborhood: form.neighborhood.trim(), city: form.city };
    setSaving(true);
    try {
      if (editingId !== null) await accountApi.updateAddress(editingId, data);
      else await accountApi.addAddress(data);
      showToast('Dirección guardada', 'success');
      reset();
      refetch();
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo guardar la dirección', 'error');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <p>Cargando...</p>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Mis direcciones</h2>
          <p className="page-subtitle">A dónde te enviamos tus compras</p>
        </div>
      </div>
      <div className="card">
        <h3>Direcciones guardadas</h3>
        {addresses?.length === 0 && <p className="auto-hint">Aún no tienes direcciones. Agrega una para poder comprar.</p>}
        <div className="addresses-list">
          {addresses?.map(address => (
            <div key={address.id} className="address-item">
              <span>{addressLabel(address)}</span>
              <button className="btn btn-secondary btn-sm" onClick={() => startEditing(address)}>Editar</button>
            </div>
          ))}
        </div>
      </div>

      <form className="card" onSubmit={handleSubmit}>
        <h3>{editingId !== null ? 'Editar dirección' : 'Nueva dirección'}</h3>
        <p className="auto-hint">Solo hacemos envíos dentro de {SHIPPING_COUNTRY}.</p>
        <div className="form-group">
          <div className="address-form">
            <AddressFields value={form} onChange={setForm} departments={departments} />
            <div className="address-row">
              <button type="submit" className="btn btn-primary btn-sm" disabled={saving}>
                {editingId !== null ? 'Guardar cambios' : 'Agregar dirección'}
              </button>
              {editingId !== null && (
                <button type="button" className="btn btn-secondary btn-sm" onClick={reset}>Cancelar</button>
              )}
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
