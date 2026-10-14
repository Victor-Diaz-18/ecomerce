import { Suspense } from 'react';
import { Outlet, NavLink, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export interface NavItem {
  name: string;
  href: string;
  icon: string;
}

// The same frame serves both portals: each one passes its own menu
export function Layout({ navigation }: { navigation: NavItem[] }) {
  const location = useLocation();
  const { session, logout } = useAuth();

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
              end={item.href === '/'}
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
          <h2>{navigation.find(n => n.href === '/' ? location.pathname === '/' : location.pathname.startsWith(n.href))?.name || 'University Store'}</h2>
          <div className="user-box">
            <span>{session?.name}</span>
            <button className="btn btn-secondary btn-sm" onClick={logout}>Cerrar sesión</button>
          </div>
        </header>
        <div className="content-area">
          <Suspense fallback={<p className="loading-text">Cargando...</p>}>
            <Outlet />
          </Suspense>
        </div>
      </main>
    </div>
  );
}
