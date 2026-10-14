import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastProvider } from './context/ToastContext';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Layout, NavItem } from './components/Layout';

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
const MyOrders = lazy(() => import('./pages/shop/MyOrders').then(m => ({ default: m.MyOrders })));
const MyAddresses = lazy(() => import('./pages/shop/MyAddresses').then(m => ({ default: m.MyAddresses })));

const ADMIN_NAV: NavItem[] = [
  { name: 'Dashboard', href: '/', icon: '📊' },
  { name: 'Categorías', href: '/categories', icon: '📁' },
  { name: 'Productos', href: '/products', icon: '📦' },
  { name: 'Clientes', href: '/customers', icon: '👥' },
  { name: 'Inventario', href: '/inventory', icon: '📋' },
  { name: 'Órdenes', href: '/orders', icon: '🧾' },
];

const CUSTOMER_NAV: NavItem[] = [
  { name: 'Tienda', href: '/', icon: '🛍️' },
  { name: 'Mis órdenes', href: '/my-orders', icon: '🧾' },
  { name: 'Mis direcciones', href: '/my-addresses', icon: '📍' },
];

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
        <Route element={<Layout navigation={ADMIN_NAV} />}>
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
      <Route element={<Layout navigation={CUSTOMER_NAV} />}>
        <Route path="/" element={<Shop />} />
        <Route path="/my-orders" element={<MyOrders />} />
        <Route path="/my-addresses" element={<MyAddresses />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <BrowserRouter>
          <Suspense fallback={<p className="loading-text">Cargando...</p>}>
            <AppRoutes />
          </Suspense>
        </BrowserRouter>
      </AuthProvider>
    </ToastProvider>
  );
}
