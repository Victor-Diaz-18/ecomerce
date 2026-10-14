import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { AlertTriangle, Minus, Plus, ShoppingCart, Trash2 } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { accountApi, productApi } from '../../services/api';
import { Product } from '../../types';
import { useCart } from '../../context/CartContext';
import { useToast } from '../../context/ToastContext';
import { addressLabel } from '../../components/AddressFields';
import { Pill } from '../../components/StatusPill';
import { UNPAID_ORDER_HOURS, isLow, money, useStock } from './shared';

interface Line {
  productId: number;
  product?: Product;
  quantity: number;
}

/**
 * The cart: change quantities, choose where to send it and continue. Continuing creates the order
 * (setting the units aside in the inventory); the customer reviews it and pays on the next page.
 * The cart itself never reserves stock and never removes anything on its own: products that sold
 * out (or left the store) wait in their own section until the customer deletes them.
 */
export function Cart() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { cart, setQuantity, remove } = useCart();
  const { data: products, loading } = useApi(() => productApi.getAllList(), []);
  const { data: addresses } = useApi(() => accountApi.getAddresses(), []);
  const [addressId, setAddressId] = useState('');
  const [creating, setCreating] = useState(false);

  const lines: Line[] = Object.entries(cart).map(([id, quantity]) => ({
    productId: Number(id),
    product: products?.find(p => p.id === Number(id)),
    quantity,
  }));
  const stock = useStock(lines.map(l => l.productId));

  // Until the stock arrives a product counts as available, so nothing jumps between sections
  const isUnavailable = (line: Line) =>
    !line.product || !line.product.active || stock[line.productId]?.available === 0;
  const available = lines.filter(l => !isUnavailable(l));
  const unavailable = lines.filter(isUnavailable);

  const total = available.reduce((sum, l) => sum + l.product!.price * l.quantity, 0);
  const units = available.reduce((n, l) => n + l.quantity, 0);
  const lowStock = available.filter(l => isLow(stock[l.productId]));
  // Someone else may have bought units while they sat in this cart
  const tooMany = available.some(l => stock[l.productId] && l.quantity > stock[l.productId].available);

  useEffect(() => {
    if (addresses?.length && !addressId) setAddressId(String(addresses[0].id));
  }, [addresses, addressId]);

  const createOrder = async () => {
    setCreating(true);
    try {
      const order = await accountApi.createOrder({
        addressId: Number(addressId),
        items: available.map(l => ({ productId: l.productId, quantity: l.quantity })),
      });
      // Only what was bought leaves the cart; sold-out products stay until the customer removes them
      available.forEach(l => remove(l.productId));
      navigate(`/checkout/${order.id}`);
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo crear la orden', 'error');
      setCreating(false);
    }
  };

  if (loading) return <p className="loading-text">Cargando...</p>;

  if (lines.length === 0) {
    return (
      <div className="card empty-state">
        <ShoppingCart size={40} strokeWidth={1.5} />
        <p>Tu carrito está vacío.</p>
        <Link to="/" className="btn btn-primary">Ir a la tienda</Link>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Carrito</h2>
          <p className="page-subtitle">Revisa los productos antes de continuar</p>
        </div>
      </div>

      {lowStock.length > 0 && (
        <div className="alert alert-warning cart-banner">
          <AlertTriangle size={18} />
          <span>
            {lowStock.length === 1
              ? <><strong>{lowStock[0].product!.name}</strong> tiene pocas unidades.</>
              : <>{lowStock.length} productos de tu carrito tienen pocas unidades.</>}
            {lowStock.length === 1
              ? ' El carrito no lo separa: si lo quieres, no esperes mucho.'
              : ' El carrito no los separa: si los quieres, no esperes mucho.'}
          </span>
        </div>
      )}

      <div className="two-columns">
        <div>
          <div className="card">
            <h3>Disponibles ({available.length})</h3>
            {available.length === 0 && <p className="auto-hint">Ningún producto de tu carrito está disponible ahora.</p>}
            {available.map(({ productId, product, quantity }) => {
              const left = stock[productId]?.available;
              return (
                <div key={productId} className="cart-line">
                  <div className="cart-line-info">
                    <strong>{product!.name}</strong>
                    <span className="mono">{money(product!.price)} c/u</span>
                    {left !== undefined && quantity > left && (
                      <span className="line-warning error">{left === 1 ? 'Solo queda 1 disponible' : `Solo quedan ${left} disponibles`}</span>
                    )}
                    {left !== undefined && quantity <= left && isLow(stock[productId]) && (
                      <span className="line-warning">¡Quedan pocas! Solo {left}</span>
                    )}
                  </div>
                  <div className="cart-line-controls">
                    <div className="quantity">
                      <button className="btn btn-secondary btn-sm" aria-label={`Quitar una unidad de ${product!.name}`} onClick={() => setQuantity(productId, quantity - 1)}><Minus size={14} /></button>
                      <span>{quantity}</span>
                      <button className="btn btn-secondary btn-sm" aria-label={`Agregar una unidad de ${product!.name}`} disabled={left !== undefined && quantity >= left} onClick={() => setQuantity(productId, quantity + 1)}><Plus size={14} /></button>
                    </div>
                    <strong className="mono">{money(product!.price * quantity)}</strong>
                    <button className="icon-btn" aria-label={`Quitar ${product!.name} del carrito`} title="Quitar" onClick={() => remove(productId)}><Trash2 size={16} /></button>
                  </div>
                </div>
              );
            })}
          </div>

          {unavailable.length > 0 && (
            <div className="card sold-out">
              <div className="card-title-row">
                <h3>Productos agotados ({unavailable.length})</h3>
                <button className="btn btn-ghost btn-sm" onClick={() => unavailable.forEach(l => remove(l.productId))}>
                  <Trash2 size={14} /> Eliminar todos
                </button>
              </div>
              <p className="auto-hint">No entran en tu compra. Puedes dejarlos aquí por si vuelven o eliminarlos.</p>
              {unavailable.map(({ productId, product, quantity }) => (
                <div key={productId} className="cart-line">
                  <div className="cart-line-info">
                    <strong>{product?.name ?? 'Producto que ya no está en la tienda'}</strong>
                    {product && <span className="mono">{quantity} × {money(product.price)}</span>}
                  </div>
                  <div className="cart-line-controls">
                    <Pill tone="gray">{product?.active ? 'Agotado' : 'No disponible'}</Pill>
                    <button className="btn btn-secondary btn-sm" onClick={() => remove(productId)}>Eliminar</button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="card summary">
          <h3>Resumen</h3>
          <div className="summary-row"><span>Productos</span><span>{units}</span></div>
          <div className="summary-row summary-total"><span>Total</span><span>{money(total)}</span></div>
          <div className="form-group">
            <label htmlFor="cart-address">Enviar a</label>
            {addresses?.length ? (
              <select id="cart-address" value={addressId} onChange={e => setAddressId(e.target.value)}>
                {addresses.map(a => <option key={a.id} value={a.id}>{addressLabel(a)}</option>)}
              </select>
            ) : (
              <p className="auto-hint">Agrega una dirección en <Link to="/my-addresses">Mis direcciones</Link>; tu carrito te espera.</p>
            )}
          </div>
          {tooMany && <p className="alert alert-warning">Ajusta las cantidades marcadas: ya no hay tantas unidades.</p>}
          <button className="btn btn-primary btn-block" disabled={creating || !addressId || tooMany || available.length === 0} onClick={createOrder}>
            {creating ? 'Creando orden...' : 'Continuar con la compra'}
          </button>
          <p className="auto-hint" style={{ marginTop: 10 }}>
            Los productos se separan para ti al continuar; tendrás {UNPAID_ORDER_HOURS} horas para pagar la orden.
          </p>
        </div>
      </div>
    </div>
  );
}
