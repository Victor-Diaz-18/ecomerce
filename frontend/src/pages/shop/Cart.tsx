import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Minus, Plus, ShoppingCart, Trash2 } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { accountApi, productApi } from '../../services/api';
import { useCart } from '../../context/CartContext';
import { useToast } from '../../context/ToastContext';
import { addressLabel } from '../../components/AddressFields';
import { UNPAID_ORDER_HOURS, isLow, money, useStock } from './shared';

/**
 * The cart: change quantities, choose where to send it and continue. Continuing creates the order
 * (setting the units aside in the inventory); the customer reviews it and pays on the next page.
 */
export function Cart() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { cart, setQuantity, remove, clear } = useCart();
  const { data: products, loading } = useApi(() => productApi.getAllList(), []);
  const { data: addresses } = useApi(() => accountApi.getAddresses(), []);
  const [addressId, setAddressId] = useState('');
  const [creating, setCreating] = useState(false);

  const lines = (products ?? [])
    .filter(p => cart[p.id])
    .map(product => ({ product, quantity: cart[product.id] }));
  const stock = useStock(lines.map(l => l.product.id));
  const total = lines.reduce((sum, line) => sum + line.product.price * line.quantity, 0);
  // Someone else may have bought units while they sat in this cart: the cart never reserves stock
  const tooMany = lines.some(l => stock[l.product.id] && l.quantity > stock[l.product.id].available);

  useEffect(() => {
    if (addresses?.length && !addressId) setAddressId(String(addresses[0].id));
  }, [addresses, addressId]);

  const createOrder = async () => {
    setCreating(true);
    try {
      const order = await accountApi.createOrder({
        addressId: Number(addressId),
        items: lines.map(l => ({ productId: l.product.id, quantity: l.quantity })),
      });
      clear();
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

      <div className="two-columns">
        <div className="card">
          {lines.map(({ product, quantity }) => {
            const units = stock[product.id]?.available;
            const low = isLow(stock[product.id]);
            return (
              <div key={product.id} className="cart-line">
                <div className="cart-line-info">
                  <strong>{product.name}</strong>
                  <span className="mono">{money(product.price)} c/u</span>
                  {units !== undefined && quantity > units && (
                    <span className="line-warning error">{units === 0 ? 'Se agotó: quítalo para continuar' : units === 1 ? 'Solo queda 1 disponible' : `Solo quedan ${units} disponibles`}</span>
                  )}
                  {units !== undefined && quantity <= units && low && (
                    <span className="line-warning">Quedan pocas unidades</span>
                  )}
                </div>
                <div className="cart-line-controls">
                  <div className="quantity">
                    <button className="btn btn-secondary btn-sm" aria-label={`Quitar una unidad de ${product.name}`} onClick={() => setQuantity(product.id, quantity - 1)}><Minus size={14} /></button>
                    <span>{quantity}</span>
                    <button className="btn btn-secondary btn-sm" aria-label={`Agregar una unidad de ${product.name}`} disabled={units !== undefined && quantity >= units} onClick={() => setQuantity(product.id, quantity + 1)}><Plus size={14} /></button>
                  </div>
                  <strong className="mono">{money(product.price * quantity)}</strong>
                  <button className="icon-btn" aria-label={`Quitar ${product.name} del carrito`} title="Quitar" onClick={() => remove(product.id)}><Trash2 size={16} /></button>
                </div>
              </div>
            );
          })}
        </div>

        <div className="card summary">
          <h3>Resumen</h3>
          <div className="summary-row"><span>Productos</span><span>{lines.reduce((n, l) => n + l.quantity, 0)}</span></div>
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
          <button className="btn btn-primary btn-block" disabled={creating || !addressId || tooMany} onClick={createOrder}>
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
