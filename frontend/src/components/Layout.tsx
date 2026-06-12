import { Outlet, NavLink, useLocation } from 'react-router-dom';

const navigation = [
  { name: 'Dashboard', href: '/', icon: '📊' },
  { name: 'Categorías', href: '/categories', icon: '📁' },
  { name: 'Productos', href: '/products', icon: '📦' },
  { name: 'Clientes', href: '/customers', icon: '👥' },
  { name: 'Inventario', href: '/inventory', icon: '📋' },
  { name: 'Órdenes', href: '/orders', icon: '🧾' },
];

export function Layout() {
  const location = useLocation();

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <h1>University Store</h1>
        </div>
        <nav className="sidebar-nav">
          {navigation.map(item => (
            <NavLink
              key={item.href}
              to={item.href}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              <span className="nav-icon">{item.icon}</span>
              {item.name}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="main-content">
        <header className="top-bar">
          <h2>{navigation.find(n => location.pathname.startsWith(n.href))?.name || 'University Store'}</h2>
        </header>
        <div className="content-area">
          <Outlet />
        </div>
      </main>
    </div>
  );
}