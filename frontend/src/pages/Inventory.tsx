import { useState } from 'react';
import { useApi, useMutation } from '../hooks/useApi';
import { inventoryApi, productApi } from '../services/api';
import { Inventory, CreateInventoryRequest, UpdateInventoryRequest, Product } from '../types';
import { Modal } from '../components/Modal';

export function InventoryPage() {
  const { data: inventories, loading, refetch } = useApi(() => inventoryApi.getAll(), []);
  const { data: products } = useApi(() => productApi.getAllList(), []);
  const { data: lowStock } = useApi(() => inventoryApi.lowStock(), []);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingInventory, setEditingInventory] = useState<Inventory | null>(null);
  const [formData, setFormData] = useState<CreateInventoryRequest>({
    availableStock: 0, minimumStock: 0, productId: 0
  });

  const { execute: createInventory } = useMutation(inventoryApi.create);
  const { execute: updateInventory } = useMutation(inventoryApi.update);

  const openCreateModal = () => {
    setEditingInventory(null);
    setFormData({ availableStock: 0, minimumStock: 0, productId: products?.[0]?.id ?? 0 });
    setIsModalOpen(true);
  };

  const openEditModal = (inv: Inventory) => {
    setEditingInventory(inv);
    setFormData({ availableStock: inv.availableStock, minimumStock: inv.minimumStock, productId: inv.productId });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (editingInventory) {
      const updateData: UpdateInventoryRequest = { availableStock: formData.availableStock, minimumStock: formData.minimumStock };
      await updateInventory(editingInventory.id, updateData);
    } else {
      await createInventory(formData);
    }
    setIsModalOpen(false);
    refetch();
  };

  const lowStockIds = new Set(lowStock?.map(i => i.id) ?? []);

  return (
    <div className="page">
      <div className="page-header">
        <h2>Inventario</h2>
        <button className="btn btn-primary" onClick={openCreateModal}>Nuevo registro</button>
      </div>

      {loading ? (
        <p>Cargando...</p>
      ) : (
        <>
          {lowStock.length > 0 && (
            <div className="alert alert-warning">
              <h4>⚠️ Productos con stock bajo ({lowStock.length})</h4>
              <ul>
                {lowStock.map(item => (
                  <li key={item.id}>{item.productName}: {item.availableStock} unidades (mín: {item.minimumStock})</li>
                ))}
              </ul>
            </div>
          )}

          <table className="data-table">
            <thead>
              <tr>
                <th>Producto</th>
                <th>Stock disponible</th>
                <th>Stock mínimo</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {inventories?.map(inv => (
                <tr key={inv.id} className={lowStockIds.has(inv.id) ? 'low-stock' : ''}>
                  <td>{inv.productName}</td>
                  <td>{inv.availableStock}</td>
                  <td>{inv.minimumStock}</td>
                  <td>
                    <span className={`status-badge ${inv.availableStock < inv.minimumStock ? 'status-low' : 'status-ok'}`}>
                      {inv.availableStock < inv.minimumStock ? 'Bajo' : 'OK'}
                    </span>
                  </td>
                  <td className="actions">
                    <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(inv)}>Editar</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingInventory ? 'Editar inventario' : 'Nuevo registro'}
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="productId">Producto</label>
            <select id="productId" value={formData.productId} onChange={e => setFormData({ ...formData, productId: Number(e.target.value) })} required>
              {products?.map(p => <option key={p.id} value={p.id}>{p.name} ({p.sku})</option>)}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="availableStock">Stock disponible</label>
            <input id="availableStock" type="number" min="0" value={formData.availableStock} onChange={e => setFormData({ ...formData, availableStock: Number(e.target.value) })} required />
          </div>
          <div className="form-group">
            <label htmlFor="minimumStock">Stock mínimo</label>
            <input id="minimumStock" type="number" min="0" value={formData.minimumStock} onChange={e => setFormData({ ...formData, minimumStock: Number(e.target.value) })} required />
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primary">{editingInventory ? 'Actualizar' : 'Crear'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}