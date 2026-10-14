import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const MIN_PASSWORD = 8;

export function Register() {
  const { register } = useAuth();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [sending, setSending] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSending(true);
    setError(null);
    try {
      // A new account is always a customer; on success App shows the shop
      await register(name, email, password);
    } catch (err) {
      const message = err instanceof Error ? err.message : '';
      setError(message.startsWith('Email already registered')
        ? 'Ese correo ya está registrado'
        : message || 'No se pudo crear la cuenta');
      setSending(false);
    }
  };

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>University Store</h1>
        <p className="auth-subtitle">Crea tu cuenta de cliente</p>
        {error && <div className="alert alert-warning">{error}</div>}
        <div className="form-group">
          <label htmlFor="name">Nombre</label>
          <input id="name" type="text" autoComplete="name" value={name} onChange={e => setName(e.target.value)} required />
        </div>
        <div className="form-group">
          <label htmlFor="email">Correo</label>
          <input id="email" type="email" autoComplete="username" value={email} onChange={e => setEmail(e.target.value)} required />
        </div>
        <div className="form-group">
          <label htmlFor="password">Contraseña (mínimo {MIN_PASSWORD} caracteres)</label>
          <input id="password" type="password" autoComplete="new-password" minLength={MIN_PASSWORD} value={password} onChange={e => setPassword(e.target.value)} required />
        </div>
        <button type="submit" className="btn btn-primary auth-submit" disabled={sending}>
          {sending ? 'Creando cuenta...' : 'Crear cuenta'}
        </button>
        <p className="auth-switch">¿Ya tienes cuenta? <Link to="/login">Inicia sesión</Link></p>
      </form>
    </div>
  );
}
