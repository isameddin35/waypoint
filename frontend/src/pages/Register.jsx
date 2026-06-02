import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const navigate = useNavigate();
  const { register } = useAuth();
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      await register({ username: form.username, email: form.email, password: form.password });
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
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
                <i className="bi bi-tree-fill text-success me-2"></i>Register
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
                  <label className="form-label">Email</label>
                  <input type="email" className="form-control" required
                         value={form.email}
                         onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Password (min 6 chars)</label>
                  <input type="password" className="form-control" required minLength={6}
                         value={form.password}
                         onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
                </div>
                <div className="mb-3">
                  <label className="form-label">Confirm Password</label>
                  <input type="password" className="form-control" required
                         value={form.confirmPassword}
                         onChange={e => setForm(f => ({ ...f, confirmPassword: e.target.value }))} />
                </div>
                <button type="submit" className="btn btn-success w-100 py-2" disabled={loading}>
                  {loading ? 'Creating account...' : 'Register'}
                </button>
              </form>
              <p className="text-center mt-3 mb-0">
                Already have an account? <Link to="/login">Login</Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
