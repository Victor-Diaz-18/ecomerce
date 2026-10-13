import { useState } from 'react';
import { useApi, useMutation } from '../hooks/useApi';
import { categoryApi } from '../services/api';
import { Category, CreateCategoryRequest } from '../types';
import { Modal } from '../components/Modal';

export function Categories() {
  const { data: categories, loading, refetch } = useApi(() => categoryApi.getAll(), []);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [formData, setFormData] = useState<CreateCategoryRequest>({ name: '' });

  const { execute: createCategory, loading: creating } = useMutation(categoryApi.create);
  const { execute: updateCategory, loading: updating } = useMutation(categoryApi.update);
  const { execute: deleteCategory } = useMutation(categoryApi.delete);

  const openCreateModal = () => {
    setEditingCategory(null);
    setFormData({ name: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (category: Category) => {
    setEditingCategory(category);
    setFormData({ name: category.name });
    setIsModalOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (editingCategory) {
      await updateCategory(editingCategory.id, formData);
    } else {
      await createCategory(formData);
    }
    setIsModalOpen(false);
    refetch();
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('¿Eliminar esta categoría?')) {
      await deleteCategory(id);
      refetch();
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h2>Categorías</h2>
        <button className="btn btn-primary" onClick={openCreateModal}>Nueva categoría</button>
      </div>

      {loading ? (
        <p>Cargando...</p>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Nombre</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {categories?.map(category => (
              <tr key={category.id}>
                <td>{category.id}</td>
                <td>{category.name}</td>
                <td className="actions">
                  <button className="btn btn-secondary btn-sm" onClick={() => openEditModal(category)}>Editar</button>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(category.id)}>Eliminar</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingCategory ? 'Editar categoría' : 'Nueva categoría'}
      >
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">Nombre</label>
            <input
              id="name"
              type="text"
              value={formData.name}
              onChange={e => setFormData({ name: e.target.value })}
              required
            />
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-secondary" onClick={() => setIsModalOpen(false)}>Cancelar</button>
            <button type="submit" className="btn btn-primary" disabled={creating || updating}>
              {editingCategory ? 'Actualizar' : 'Crear'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}