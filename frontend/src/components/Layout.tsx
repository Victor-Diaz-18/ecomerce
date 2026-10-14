import { Suspense } from 'react';
import { Outlet, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { LogOut, Moon, ShoppingCart, Sun, type LucideIcon } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useTheme } from '../hooks/useTheme';

export interface NavItem {
  name: string;
  href: string;
  icon: LucideIcon;
}

export interface NavSection {
  title: string;
  items: NavItem[];
}

interface Props {
  sections: NavSection[];
  subtitle: string;
  // Extra pages that are not in the menu but still need a name in the top bar
  extraTitles?: Record<string, string>;
  showCart?: boolean;
}

const initials = (name?: string) =>
  (name ?? '').split(' ').filter(Boolean).slice(0, 2).map(word => word[0].toUpperCase()).join('') || 'U';

// The same frame serves both portals: each one passes its own menu
export function Layout({ sections, subtitle, extraTitles = {}, showCart = false }: Props) {
  const location = useLocation();
  const navigate = useNavigate();
  const { session, logout } = useAuth();
  const { count } = useCart();
  const { theme, toggle } = useTheme();

  const matches = (href: string) => href === '/' ? location.pathname === '/' : location.pathname.startsWith(href);
  const section = sections.find(s => s.items.some(i => matches(i.href)));
  const item = section?.items.find(i => matches(i.href));
  const page = item?.name ?? Object.entries(extraTitles).find(([path]) => location.pathname.startsWith(path))?.[1];
  // "Inicio / Ventas / Órdenes"; the home page and the pages outside the menu skip the section
  const sectionCrumb = item && item.href !== '/' ? section?.title : undefined;

  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="brand-logo">U</div>
          <div>
            <h1>University Store</h1>
            <small>{subtitle}</small>
          </div>
        </div>
        <nav className="sidebar-nav">
          {sections.map(s => (
            <div key={s.title}>
              <div className="nav-section">{s.title}</div>
              {s.items.map(item => (
                <NavLink
                  key={item.href}
                  to={item.href}
                  end={item.href === '/'}
                  className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
                >
                  <span className="nav-icon"><item.icon size={18} strokeWidth={1.8} /></span>
                  {item.name}
                </NavLink>
              ))}
            </div>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="avatar">{initials(session?.name)}</div>
          <div className="user-info">
            <strong>{session?.name}</strong>
            <span>{session?.email}</span>
          </div>
          <button className="icon-btn" onClick={logout} aria-label="Cerrar sesión" title="Cerrar sesión">
            <LogOut size={18} />
          </button>
        </div>
      </aside>
      <main className="main-content">
        <header className="top-bar">
          <div className="breadcrumb">
            Inicio{sectionCrumb && ` / ${sectionCrumb}`}
            {page && <> / <strong>{page}</strong></>}
          </div>
          <div className="top-actions">
            {showCart && (
              <button className="icon-btn" onClick={() => navigate('/cart')} aria-label={`Carrito, ${count} artículos`} title="Carrito">
                <ShoppingCart size={19} />
                {count > 0 && <span className="badge-count">{count}</span>}
              </button>
            )}
            <button className="icon-btn" onClick={toggle} aria-label="Cambiar tema" title={theme === 'dark' ? 'Tema claro' : 'Tema oscuro'}>
              {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
            </button>
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
