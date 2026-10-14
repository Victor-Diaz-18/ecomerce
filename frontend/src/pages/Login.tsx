import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function Login() {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSending(true);
    setError(null);
    try {
      // On success the session changes and App shows the portal of that user
      await login(email, password);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo iniciar sesión');
      setSending(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="auth-brand"><div className="brand-logo">U</div></div>
        <h1>University Store</h1>
        <p className="auth-subtitle">Inicia sesión para continuar</p>
        {error && <div className="alert alert-warning">{error}</div>}
        <div className="form-group">
          <label htmlFor="email">Correo</label>
          <input id="email" type="email" autoComplete="username" value={email} onChange={e => setEmail(e.target.value)} required />
        </div>
        <div className="form-group">
          <label htmlFor="password">Contraseña</label>
          <input id="password" type="password" autoComplete="current-password" value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <button type="submit" className="btn btn-primary auth-submit" disabled={sending}>
          {sending ? 'Entrando...' : 'Entrar'}
        </button>
        <p className="auth-switch">¿No tienes cuenta? <Link to="/register">Regístrate</Link></p>
      </form>
    </div>
  );
}
