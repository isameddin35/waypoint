import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card shadow">
            <div className="card-body p-4">
              <h2 className="text-center mb-4 fw-bold">
                <i className="bi bi-tree-fill text-success me-2"></i>Login
              </h2>
              {error && <div className="alert alert-danger">{error}</div>}
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label">Username</label>
                  <input type="text" className="form-control" required
                         value={form.username}
                         onChange={e => setForm(f => ({ ...f, username: e.target.value }))} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Password</label>
                  <input type="password" className="form-control" required
                         value={form.password}
                         onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
                </div>
                <button type="submit" className="btn btn-success w-100 py-2" disabled={loading}>
                  {loading ? 'Logging in...' : 'Login'}
                </button>
              </form>
              <p className="text-center mt-3 mb-0">
                Don't have an account? <Link to="/register">Register</Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
