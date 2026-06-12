import { useState, useEffect } from 'react';
import { useApi, useMutation } from '../hooks/useApi';
import { productApi, categoryApi } from '../services/api';
import { Product, CreateProductRequest, UpdateProductRequest, Category } from '../types';
import { Modal } from '../components/Modal';

export function Products() {
  const { data: productsPage, loading, refetch } = useApi(() => productApi.getAll(), []);
  const { data: categories } = useApi(() => categoryApi.getAll(), []);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [formData, setFormData] = useState<CreateProductRequest>({
    name: '', sku: '', price: 0, categoryId: 0
  });

  const { execute: createProduct, loading: creating } = useMutation(productApi.create);
  const { execute: updateProduct, loading: updating } = useMutation(productApi.update);
  const { execute: deleteProduct } = useMutation(productApi.delete);

  const openCreateModal = () => {
    setEditingProduct(null);
    setFormData({ name: '', sku: '', price: 0, categoryId: categories?.[0]?.id ?? 0 });
    setIsModalOpen(true);
  };

  const openEditModal = (product: Product) => {
    setEditingProduct(product);
    setFormData({
      name: product.name,
      sku: product.sku,
      price: product.price,
      categoryId: product.categoryId,
    });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (editingProduct) {
      const updateData: UpdateProductRequest = { ...formData, active: editingProduct.active };
      await updateProduct(editingProduct.id, updateData);
    } else {
      await createProduct(formData);
    }
    setIsModalOpen(false);
    refetch();
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('¿Eliminar este producto?')) {
      await deleteProduct(id);
      refetch();
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>Productos</h2>
        <button className="btn btn-primary" onClick={openCreateModal}>Nuevo producto</button>
      </div>

      {loading ? (
        <p>Cargando...</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>SKU</th>
              <th>Precio</th>
              <th>Categoría</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {productsPage?.content.map(product => (
              <tr key={product.id}>
                <td>{product.id}</td>
                <td>{product.name}</td>
                <td>{product.sku}</td>
                <td>${product.price.toFixed(2)}</td>
                <td>{product.categoryName}</td>
                <td><span className={`status-badge ${product.active ? 'status-active' : 'status-inactive'}`}>
                  {product.active ? 'Activo' : 'Inactivo'}
                </span></td>
                <td className="actions">
                  <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(product)}>Editar</button>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(product.id)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingProduct ? 'Editar producto' : 'Nuevo producto'}
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Nombre</label>
            <input id="name" type="text" value={formData.name} onChange={e => setFormData({ ...formData, name: e.target.value })} required />
          </div>
          <div className="form-group">
            <label htmlFor="sku">SKU</label>
            <input id="sku" type="text" value={formData.sku} onChange={e => setFormData({ ...formData, sku: e.target.value })} required />
          </div>
          <div className="form-group">
            <label htmlFor="price">Precio</label>
            <input id="price" type="number" step="0.01" min="0" value={formData.price} onChange={e => setFormData({ ...formData, price: Number(e.target.value) })} required />
          </div>
          <div className="form-group">
            <label htmlFor="categoryId">Categoría</label>
            <select id="categoryId" value={formData.categoryId} onChange={e => setFormData({ ...formData, categoryId: Number(e.target.value) })} required>
              {categories?.map(cat => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
            </select>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={creating || updating}>
              {editingProduct ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}