import { useState } from 'react';
import { accountApi } from '../../services/api';
import { useAuth } from '../../context/AuthContext';
import { useToast } from '../../context/ToastContext';
import { loadSession, saveSession } from '../../services/session';

const MIN_PASSWORD = 8;

export function Profile() {
  const { session } = useAuth();
  const { showToast } = useToast();
  const [name, setName] = useState(session?.name ?? '');
  const [savingName, setSavingName] = useState(false);
  const [passwords, setPasswords] = useState({ current: '', next: '', repeat: '' });
  const [savingPassword, setSavingPassword] = useState(false);

  const saveName = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingName(true);
    try {
      const profile = await accountApi.updateProfile(name);
      // The menu shows the name from the saved session, so keep it in step
      const current = loadSession();
      if (current) saveSession({ ...current, name: profile.name });
      showToast('Nombre actualizado', 'success');
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo guardar', 'error');
    } finally {
      setSavingName(false);
    }
  };

  const savePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (passwords.next !== passwords.repeat) {
      showToast('La nueva contraseña y su confirmación no coinciden', 'error');
      return;
    }
    setSavingPassword(true);
    try {
      await accountApi.changePassword(passwords.current, passwords.next);
      setPasswords({ current: '', next: '', repeat: '' });
      showToast('Contraseña actualizada', 'success');
    } catch (err) {
      showToast(err instanceof Error ? err.message : 'No se pudo cambiar la contraseña', 'error');
    } finally {
      setSavingPassword(false);
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h2>Mi perfil</h2>
          <p className="page-subtitle">Tus datos y tu contraseña</p>
        </div>
      </div>

      <form className="card narrow" onSubmit={saveName}>
        <h3>Datos</h3>
        <div className="form-group">
          <label htmlFor="profile-name">Nombre</label>
          <input id="profile-name" type="text" maxLength={100} value={name} onChange={e => setName(e.target.value)} required />
        </div>
        <div className="form-group">
          <label htmlFor="profile-email">Correo</label>
          <input id="profile-email" type="email" value={session?.email ?? ''} disabled />
          <p className="auto-hint">Es con el que inicias sesión; por ahora no se puede cambiar.</p>
        </div>
        <button type="submit" className="btn btn-primary" disabled={savingName || !name.trim()}>
          {savingName ? 'Guardando...' : 'Guardar'}
        </button>
      </form>

      <form className="card narrow" onSubmit={savePassword}>
        <h3>Cambiar contraseña</h3>
        <div className="form-group">
          <label htmlFor="current-password">Contraseña actual</label>
          <input id="current-password" type="password" autoComplete="current-password" value={passwords.current}
            onChange={e => setPasswords({ ...passwords, current: e.target.value })} required />
        </div>
        <div className="form-group">
          <label htmlFor="new-password">Nueva contraseña (mínimo {MIN_PASSWORD} caracteres)</label>
          <input id="new-password" type="password" autoComplete="new-password" minLength={MIN_PASSWORD} value={passwords.next}
            onChange={e => setPasswords({ ...passwords, next: e.target.value })} required />
        </div>
        <div className="form-group">
          <label htmlFor="repeat-password">Repite la nueva contraseña</label>
          <input id="repeat-password" type="password" autoComplete="new-password" minLength={MIN_PASSWORD} value={passwords.repeat}
            onChange={e => setPasswords({ ...passwords, repeat: e.target.value })} required />
        </div>
        <button type="submit" className="btn btn-primary" disabled={savingPassword}>
          {savingPassword ? 'Guardando...' : 'Cambiar contraseña'}
        </button>
      </form>
    </div>
  );
}
