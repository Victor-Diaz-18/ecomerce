import { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ToastProvider } from './context/ToastContext';
import { Layout } from './components/Layout';

// Cada página se descarga solo cuando se visita (code splitting)
const Dashboard = lazy(() => import('./pages/Dashboard').then(m => ({ default: m.Dashboard })));
const Categories = lazy(() => import('./pages/Categories').then(m => ({ default: m.Categories })));
const Products = lazy(() => import('./pages/Products').then(m => ({ default: m.Products })));
const Customers = lazy(() => import('./pages/Customers').then(m => ({ default: m.Customers })));
const InventoryPage = lazy(() => import('./pages/Inventory').then(m => ({ default: m.InventoryPage })));
const Orders = lazy(() => import('./pages/Orders').then(m => ({ default: m.Orders })));

export function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <Suspense fallback={<p className="loading-text">Cargando...</p>}>
          <Routes>
            <Route element={<Layout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/categories" element={<Categories />} />
              <Route path="/products" element={<Products />} />
              <Route path="/customers" element={<Customers />} />
              <Route path="/inventory" element={<InventoryPage />} />
              <Route path="/orders" element={<Orders />} />
            </Route>
          </Routes>
        </Suspense>
      </BrowserRouter>
    </ToastProvider>
  );
}
