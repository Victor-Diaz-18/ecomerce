import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApi } from '../../hooks/useApi';
import { accountApi, inventoryApi, productApi } from '../../services/api';
import { useToast } from '../../context/ToastContext';
import { addressLabel } from '../../components/AddressFields';
import { useAuth } from '../../context/AuthContext';

const money = (value: number) => `$${value.toLocaleString('es-CO')}`;

// The cart survives moving to another page (for example to add an address) until the tab is closed
type Cart = Record<number, number>;
const cartKey = (customerId?: number) => `university-store-cart-${customerId ?? 'anon'}`;

function loadCart(customerId?: number): Cart {
  try {
    return JSON.parse(sessionStorage.getItem(cartKey(customerId)) ?? '{}') as Cart;
  } catch {
    return {};
  }
}

function saveCart(customerId: number | undefined, cart: Cart) {
  try {
    if (Object.keys(cart).length) sessionStorage.setItem(cartKey(customerId), JSON.stringify(cart));
    else sessionStorage.removeItem(cartKey(customerId));
  } catch {
    // storage unavailable (private window): the cart just lives while the page is open
  }
}

export function Shop() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { session } = useAuth();
  const { data: products, loading } = useApi(() => productApi.getAllList(), []);
  const { data: addresses } = useApi(() => accountApi.getAddresses(), []);

  // productId -> units available; a product missing here has no inventory yet
  const [stock, setStock] = useState<Record<number, number>>({});
  // productId -> units in the cart
  const [cart, setCart] = useState<Cart>(() => loadCart(session?.customerId));
  const [addressId, setAddressId] = useState('');
  const [buying, setBuying] = useState(false);

  const available = products?.filter(p => p.active) ?? [];

  useEffect(() => {
    if (!products) return;
    Promise.all(products.filter(p => p.active).map(p =>
      inventoryApi.getByProductId(p.id)
        .then(inventory => [p.id, inventory.availableStock] as const)
        .catch(() => [p.id, 0] as const)
    )).then(entries => setStock(Object.fromEntries(entries)));
  }, [products]);

  useEffect(() => saveCart(session?.customerId, cart), [session?.customerId, cart]);

  useEffect(() => {
    if (addresses?.length && !addressId) setAddressId(String(addresses[0].id));
  }, [addresses, addressId]);

  const setQuantity = (productId: number, quantity: number) => {
    setCart(prev => {
      const next = { ...prev };
      if (quantity <= 0) delete next[productId];
      else next[productId] = Math.min(quantity, stock[productId] ?? 0);
      return next;
    });
  };

  const cartItems = available.filter(p => cart[p.id]).map(p => ({ product: p, quantity: cart[p.id] }));
  const total = cartItems.reduce((sum, item) => sum + item.product.price * item.quantity, 0);

  const buy = async () => {
    setBuying(true);
    try {
      const order = await accountApi.createOrder({
        addressId: Number(addressId),
        items: cartItems.map(item => ({ productId: item.product.id, quantity: item.quantity })),
      });
      setCart({});
      saveCart(session?.customerId, {});
      showToast(`Orden #${order.id} creada. Págala para que la despachemos.`, 'success');
      navigate('/my-orders');
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo crear la orden', 'error');
      setBuying(false);
    }
  };

  if (loading) return <p>Cargando...</p>;

  return (
    <div className="page shop">
      <div className="product-grid">
        {available.length === 0 && <p>Todavía no hay productos a la venta.</p>}
        {available.map(product => {
          const units = stock[product.id] ?? 0;
          const inCart = cart[product.id] ?? 0;
          return (
            <div key={product.id} className="product-card">
              <span className="product-category">{product.categoryName}</span>
              <h3>{product.name}</h3>
              <p className="product-price">{money(product.price)}</p>
              <p className={units > 0 ? 'product-stock' : 'product-stock out'}>
                {units > 0 ? `${units} disponibles` : 'Agotado'}
              </p>
              {inCart === 0 ? (
                <button className="btn btn-primary btn-sm" disabled={units === 0} onClick={() => setQuantity(product.id, 1)}>
                  Agregar al carrito
                </button>
              ) : (
                <div className="quantity">
                  <button className="btn btn-secondary btn-sm" aria-label={`Quitar una unidad de ${product.name}`} onClick={() => setQuantity(product.id, inCart - 1)}>−</button>
                  <span>{inCart}</span>
                  <button className="btn btn-secondary btn-sm" aria-label={`Agregar una unidad de ${product.name}`} disabled={inCart >= units} onClick={() => setQuantity(product.id, inCart + 1)}>+</button>
                </div>
              )}
            </div>
          );
        })}
      </div>

      <aside className="cart">
        <h3>Tu carrito</h3>
        {cartItems.length === 0 ? (
          <p className="auto-hint">Agrega productos para hacer tu compra.</p>
        ) : (
          <>
            {cartItems.map(item => (
              <div key={item.product.id} className="cart-line">
                <span>{item.product.name} × {item.quantity}</span>
                <span>{money(item.product.price * item.quantity)}</span>
              </div>
            ))}
            <div className="cart-line cart-total">
              <span>Total</span>
              <span>{money(total)}</span>
            </div>
            <div className="form-group">
              <label htmlFor="shipping-address">Enviar a</label>
              {addresses?.length ? (
                <select id="shipping-address" value={addressId} onChange={e => setAddressId(e.target.value)}>
                  {addresses.map(a => <option key={a.id} value={a.id}>{addressLabel(a)}</option>)}
                </select>
              ) : (
                <p className="auto-hint">Primero agrega una dirección en <Link to="/my-addresses">Mis direcciones</Link>.</p>
              )}
            </div>
            <button className="btn btn-primary" disabled={buying || !addressId} onClick={buy}>
              {buying ? 'Creando orden...' : 'Comprar'}
            </button>
          </>
        )}
      </aside>
    </div>
  );
}
