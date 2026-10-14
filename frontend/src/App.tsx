import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { LayoutDashboard, Layers, MapPin, Package, ReceiptText, Store, Tag, UserRound, Users } from 'lucide-react';
import { ToastProvider } from './context/ToastContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { Layout, NavSection } from './components/Layout';

// Cada página se descarga solo cuando se visita (code splitting)
const Dashboard = lazy(() => import('./pages/Dashboard').then(m => ({ default: m.Dashboard })));
const Categories = lazy(() => import('./pages/Categories').then(m => ({ default: m.Categories })));
const Products = lazy(() => import('./pages/Products').then(m => ({ default: m.Products })));
const Customers = lazy(() => import('./pages/Customers').then(m => ({ default: m.Customers })));
const InventoryPage = lazy(() => import('./pages/Inventory').then(m => ({ default: m.InventoryPage })));
const Orders = lazy(() => import('./pages/Orders').then(m => ({ default: m.Orders })));
const Login = lazy(() => import('./pages/Login').then(m => ({ default: m.Login })));
const Register = lazy(() => import('./pages/Register').then(m => ({ default: m.Register })));
const Shop = lazy(() => import('./pages/shop/Shop').then(m => ({ default: m.Shop })));
const Cart = lazy(() => import('./pages/shop/Cart').then(m => ({ default: m.Cart })));
const Checkout = lazy(() => import('./pages/shop/Checkout').then(m => ({ default: m.Checkout })));
const BuyNow = lazy(() => import('./pages/shop/BuyNow').then(m => ({ default: m.BuyNow })));
const MyOrders = lazy(() => import('./pages/shop/MyOrders').then(m => ({ default: m.MyOrders })));
const MyAddresses = lazy(() => import('./pages/shop/MyAddresses').then(m => ({ default: m.MyAddresses })));
const Profile = lazy(() => import('./pages/shop/Profile').then(m => ({ default: m.Profile })));

const ADMIN_MENU: NavSection[] = [
  { title: 'Operaciones', items: [{ name: 'Dashboard', href: '/', icon: LayoutDashboard }] },
  {
    title: 'Catálogo', items: [
      { name: 'Clientes', href: '/customers', icon: Users },
      { name: 'Categorías', href: '/categories', icon: Tag },
      { name: 'Productos', href: '/products', icon: Package },
      { name: 'Inventario', href: '/inventory', icon: Layers },
    ],
  },
  { title: 'Ventas', items: [{ name: 'Órdenes', href: '/orders', icon: ReceiptText }] },
];

const CUSTOMER_MENU: NavSection[] = [
  { title: 'Comprar', items: [{ name: 'Tienda', href: '/', icon: Store }] },
  {
    title: 'Mi cuenta', items: [
      { name: 'Mis órdenes', href: '/my-orders', icon: ReceiptText },
      { name: 'Mis direcciones', href: '/my-addresses', icon: MapPin },
      { name: 'Mi perfil', href: '/profile', icon: UserRound },
    ],
  },
];

const CUSTOMER_PAGES = { '/cart': 'Carrito', '/checkout': 'Revisar orden', '/buy': 'Comprar ahora' };

// Which pages exist depends on who is logged in; any other address goes back to the start
function AppRoutes() {
  const { session } = useAuth();

  if (!session) {
    return (
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  if (session.role === 'ADMIN') {
    return (
      <Routes>
        <Route element={<Layout sections={ADMIN_MENU} subtitle="Panel administrativo" />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/categories" element={<Categories />} />
          <Route path="/products" element={<Products />} />
          <Route path="/customers" element={<Customers />} />
          <Route path="/inventory" element={<InventoryPage />} />
          <Route path="/orders" element={<Orders />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route element={<Layout sections={CUSTOMER_MENU} subtitle="Tienda" extraTitles={CUSTOMER_PAGES} showCart />}>
        <Route path="/" element={<Shop />} />
        <Route path="/cart" element={<Cart />} />
        <Route path="/checkout/:orderId" element={<Checkout />} />
        <Route path="/buy/:productId" element={<BuyNow />} />
        <Route path="/my-orders" element={<MyOrders />} />
        <Route path="/my-addresses" element={<MyAddresses />} />
        <Route path="/profile" element={<Profile />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <CartProvider>
          <BrowserRouter>
            <Suspense fallback={<p className="loading-text">Cargando...</p>}>
              <AppRoutes />
            </Suspense>
          </BrowserRouter>
        </CartProvider>
      </AuthProvider>
    </ToastProvider>
  );
}
