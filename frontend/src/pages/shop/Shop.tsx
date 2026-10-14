import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { CheckCircle2, ShoppingCart, Zap } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { productApi } from '../../services/api';
import { Product } from '../../types';
import { Modal } from '../../components/Modal';
import { useCart } from '../../context/CartContext';
import { isLow, money, useStock } from './shared';

export function Shop() {
  const navigate = useNavigate();
  const { cart, add } = useCart();
  const { data: products, loading } = useApi(() => productApi.getAllList(), []);
  const available = products?.filter(p => p.active) ?? [];
  const stock = useStock(available.map(p => p.id));
  // The product just added, to ask whether to keep shopping or go to the cart
  const [added, setAdded] = useState<Product | null>(null);

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

      {available.length === 0 && <div className="card empty-state"><p>Todavía no hay productos a la venta.</p></div>}

      <div className="product-grid">
        {available.map(product => {
          const units = stock[product.id]?.available ?? 0;
          const low = isLow(stock[product.id]);
          const inCart = cart[product.id] ?? 0;
          const soldOut = units === 0 || inCart >= units;
          return (
            <div key={product.id} className="product-card">
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
