import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { CreditCard, Minus, Plus } from 'lucide-react';
import { useApi } from '../../hooks/useApi';
import { accountApi, productApi } from '../../services/api';
import { useToast } from '../../context/ToastContext';
import { addressLabel } from '../../components/AddressFields';
import { ProductPhoto } from '../../components/ProductPhoto';
import { isLow, money, useStock } from './shared';

/**
 * "Comprar ahora": straight to payment for a single product, without touching the cart.
 * The order is created already paid, so the units leave the inventory at that moment.
 */
export function BuyNow() {
  const { productId } = useParams();
  const id = Number(productId);
  const navigate = useNavigate();
  const { showToast } = useToast();
  const { data: product, loading } = useApi(() => productApi.getById(id), [id]);
  const { data: addresses } = useApi(() => accountApi.getAddresses(), []);
  const stock = useStock([id]);
  const [quantity, setQuantity] = useState(1);
  const [addressId, setAddressId] = useState('');
  const [paying, setPaying] = useState(false);

  const units = stock[id]?.available;
  const low = isLow(stock[id]);

  useEffect(() => {
    if (addresses?.length && !addressId) setAddressId(String(addresses[0].id));
  }, [addresses, addressId]);

  const pay = async () => {
    setPaying(true);
    try {
      const order = await accountApi.buyNow({ addressId: Number(addressId), productId: id, quantity });
      // The order exists even if the charge did not go through: it waits in My orders to retry the payment
      if (order.status === 'PAID') showToast(`Pago recibido. Tu orden #${order.id} está confirmada.`, 'success');
      else showToast(`No se pudo procesar el pago. Tu orden #${order.id} quedó pendiente en Mis órdenes para que lo reintentes.`, 'error');
      navigate('/my-orders');
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo completar la compra', 'error');
      setPaying(false);
    }
  };

  if (loading) return <p className="loading-text">Cargando...</p>;
  if (!product || !product.active) return <div className="card empty-state"><p>Ese producto no está disponible.</p><Link to="/" className="btn btn-primary">Volver a la tienda</Link></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Comprar ahora</h2>
          <p className="page-subtitle">Paga este producto de inmediato; tu carrito no cambia</p>
        </div>
      </div>

      <div className="two-columns">
        <div className="card buy-now-product">
          <ProductPhoto product={product} />
          <div>
          <span className="product-category">{product.categoryName}</span>
          <h3 style={{ fontSize: '1.2rem', margin: '4px 0' }}>{product.name}</h3>
          <p className="product-price">{money(product.price)}</p>
          <p className={!units ? 'product-stock out' : low ? 'product-stock low' : 'product-stock'}>
            {units === undefined ? '' : units === 0 ? 'Agotado' : low ? `¡Quedan pocas! Solo ${units}` : `${units} disponibles`}
          </p>
          <span className="field-label">Cantidad</span>
          <div className="quantity">
            <button className="btn btn-secondary btn-sm" aria-label="Quitar una unidad" disabled={quantity <= 1} onClick={() => setQuantity(quantity - 1)}><Minus size={14} /></button>
            <span>{quantity}</span>
            <button className="btn btn-secondary btn-sm" aria-label="Agregar una unidad" disabled={units !== undefined && quantity >= units} onClick={() => setQuantity(quantity + 1)}><Plus size={14} /></button>
          </div>
          </div>
        </div>

        <div className="card summary">
          <h3>Pago</h3>
          <div className="summary-row"><span>{quantity} × {money(product.price)}</span><span>{money(product.price * quantity)}</span></div>
          <div className="summary-row summary-total"><span>Total</span><span>{money(product.price * quantity)}</span></div>
          <div className="form-group">
            <label htmlFor="buy-address">Enviar a</label>
            {addresses?.length ? (
              <select id="buy-address" value={addressId} onChange={e => setAddressId(e.target.value)}>
                {addresses.map(a => <option key={a.id} value={a.id}>{addressLabel(a)}</option>)}
              </select>
            ) : (
              <p className="auto-hint">Primero agrega una dirección en <Link to="/my-addresses">Mis direcciones</Link>.</p>
            )}
          </div>
          <button className="btn btn-primary btn-block" disabled={paying || !addressId || !units} onClick={pay}>
            <CreditCard size={16} /> {paying ? 'Procesando pago...' : `Pagar ${money(product.price * quantity)}`}
          </button>
          <p className="auto-hint" style={{ marginTop: 10 }}>Pago simulado: no se pide ni se guarda ningún dato de tarjeta.</p>
        </div>
      </div>
    </div>
  );
}
