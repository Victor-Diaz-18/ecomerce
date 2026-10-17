import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle2, Search, ShoppingCart, X, Zap } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { productApi } from '../../services/api';
import { Product } from '../../types';
import { Modal } from '../../components/Modal';
import { ProductPhoto } from '../../components/ProductPhoto';
import { useCart } from '../../context/CartContext';
import { isLow, money, useStock } from './shared';

type Sort = 'name' | 'price-asc' | 'price-desc';

// "Cuadérno" and "cuaderno" are the same search
const normalize = (text: string) => text.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim();

export function Shop() {
  const navigate = useNavigate();
  const { cart, add } = useCart();
  const { data: products, loading } = useApi(() => productApi.getAllList(), []);
  const available = products?.filter(p => p.active) ?? [];
  const stock = useStock(available.map(p => p.id));
  // The product just added, to ask whether to keep shopping or go to the cart
  const [added, setAdded] = useState<Product | null>(null);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [sort, setSort] = useState<Sort>('name');
  const [onlyInStock, setOnlyInStock] = useState(false);

  // Only the categories that have something for sale
  const categories = [...new Set(available.map(p => p.categoryName).filter((c): c is string => Boolean(c)))].sort();

  const shown = available
    .filter(p => !query || normalize(p.name).includes(normalize(query)))
    .filter(p => !category || p.categoryName === category)
    .filter(p => !onlyInStock || (stock[p.id]?.available ?? 0) > 0)
    .sort((a, b) => sort === 'price-asc' ? a.price - b.price
      : sort === 'price-desc' ? b.price - a.price
      : a.name.localeCompare(b.name, 'es'));
  const filtering = Boolean(query || category || onlyInStock);

  const clearFilters = () => {
    setQuery('');
    setCategory('');
    setOnlyInStock(false);
  };

  const addToCart = (product: Product) => {
    add(product.id);
    setAdded(product);
  };

  if (loading) return <p className="loading-text">Cargando...</p>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Tienda</h2>
          <p className="page-subtitle">Productos de la tienda universitaria, con envío a todo Colombia</p>
        </div>
      </div>

      {available.length === 0 ? (
        <div className="card empty-state"><p>Todavía no hay productos a la venta.</p></div>
      ) : (
        <div className="filters shop-filters">
          <div className="search-box">
            <Search size={16} />
            <input type="search" placeholder="Buscar productos" aria-label="Buscar productos" value={query} onChange={e => setQuery(e.target.value)} />
          </div>
          <select aria-label="Categoría" value={category} onChange={e => setCategory(e.target.value)}>
            <option value="">Todas las categorías</option>
            {categories.map(c => <option key={c} value={c}>{c}</option>)}
          </select>
          <select aria-label="Ordenar" value={sort} onChange={e => setSort(e.target.value as Sort)}>
            <option value="name">Nombre (A-Z)</option>
            <option value="price-asc">Precio: menor a mayor</option>
            <option value="price-desc">Precio: mayor a menor</option>
          </select>
          <label className="checkbox">
            <input type="checkbox" checked={onlyInStock} onChange={e => setOnlyInStock(e.target.checked)} />
            Solo disponibles
          </label>
          <span className="results-count">{shown.length} de {available.length}</span>
          {filtering && (
            <button className="btn btn-ghost btn-sm" onClick={clearFilters}><X size={14} /> Limpiar</button>
          )}
        </div>
      )}

      {available.length > 0 && shown.length === 0 && (
        <div className="card empty-state">
          <p>No encontramos productos{query ? ` para "${query}"` : ''} con esos filtros.</p>
          <button className="btn btn-secondary" onClick={clearFilters}>Ver todos los productos</button>
        </div>
      )}

      <div className="product-grid">
        {shown.map(product => {
          const units = stock[product.id]?.available ?? 0;
          const low = isLow(stock[product.id]);
          const inCart = cart[product.id] ?? 0;
          const soldOut = units === 0 || inCart >= units;
          return (
            <div key={product.id} className="product-card">
              <ProductPhoto product={product} />
              <span className="product-category">{product.categoryName}</span>
              <h3>{product.name}</h3>
              <p className="product-price">{money(product.price)}</p>
              <p className={units === 0 ? 'product-stock out' : low ? 'product-stock low' : 'product-stock'}>
                {units === 0 ? 'Agotado' : low ? `¡Quedan pocas! Solo ${units}` : `${units} disponibles`}
                {units > 0 && inCart ? ` · ${inCart} en tu carrito` : ''}
              </p>
              <div className="product-actions">
                <button className="btn btn-primary" disabled={units === 0} onClick={() => navigate(`/buy/${product.id}`)}>
                  <Zap size={16} /> Comprar ahora
                </button>
                <button className="btn btn-secondary" disabled={soldOut} onClick={() => addToCart(product)}>
                  <ShoppingCart size={16} /> Agregar al carrito
                </button>
              </div>
            </div>
          );
        })}
      </div>

      <Modal isOpen={added !== null} onClose={() => setAdded(null)} title="Producto agregado">
        <p style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <CheckCircle2 size={22} color="var(--success)" />
          <span><strong>{added?.name}</strong> está en tu carrito.</span>
        </p>
        <div className="dialog-actions">
          <button className="btn btn-secondary" onClick={() => setAdded(null)}>Seguir comprando</button>
          <button className="btn btn-primary" onClick={() => navigate('/cart')}>Ir al carrito</button>
        </div>
      </Modal>
    </div>
  );
}
