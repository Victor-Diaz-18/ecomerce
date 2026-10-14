import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { useAuth } from './AuthContext';

// productId -> units
export type Cart = Record<number, number>;

interface CartContextType {
  cart: Cart;
  count: number;
  add: (productId: number, units?: number) => void;
  setQuantity: (productId: number, units: number) => void;
  remove: (productId: number) => void;
  clear: () => void;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

// Each customer has their own cart, kept until the browser tab is closed
const keyFor = (customerId?: number) => `university-store-cart-${customerId ?? 'none'}`;

function load(key: string): Cart {
  try {
    return JSON.parse(sessionStorage.getItem(key) ?? '{}') as Cart;
  } catch {
    return {};
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const { session } = useAuth();
  const key = keyFor(session?.customerId);
  // The cart remembers whose it is, so logging in as someone else never mixes two carts
  const [state, setState] = useState(() => ({ key, cart: load(key) }));
  if (state.key !== key) setState({ key, cart: load(key) });
  const cart = state.key === key ? state.cart : {};

  useEffect(() => {
    try {
      if (Object.keys(state.cart).length) sessionStorage.setItem(state.key, JSON.stringify(state.cart));
      else sessionStorage.removeItem(state.key);
    } catch {
      // storage unavailable (private window): the cart only lives while the page is open
    }
  }, [state]);

  const setCart = (update: (prev: Cart) => Cart) => setState(prev => ({ key: prev.key, cart: update(prev.cart) }));

  const setQuantity = (productId: number, units: number) =>
    setCart(prev => {
      const next = { ...prev };
      if (units <= 0) delete next[productId];
      else next[productId] = units;
      return next;
    });

  const value: CartContextType = {
    cart,
    count: Object.values(cart).reduce((sum, units) => sum + units, 0),
    add: (productId, units = 1) => setQuantity(productId, (cart[productId] ?? 0) + units),
    setQuantity,
    remove: productId => setQuantity(productId, 0),
    clear: () => setCart(() => ({})),
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart must be used within a CartProvider');
  return context;
}
