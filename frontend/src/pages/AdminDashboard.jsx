import { useState, useEffect } from 'react';
import { adminApi } from '../services/api';

export default function AdminDashboard() {
  const [tab, setTab] = useState('stats');
  const [users, setUsers] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [reports, setReports] = useState([]);
  const [stats, setStats] = useState({});

  useEffect(() => {
    adminApi.getStats().then(r => setStats(r.data)).catch(() => {});
    adminApi.getUsers().then(r => setUsers(r.data)).catch(() => {});
    adminApi.getRoutes().then(r => setRoutes(r.data)).catch(() => {});
    adminApi.getReports().then(r => setReports(r.data)).catch(() => {});
  }, []);

  return (
    <div className="container py-4">
      <h1 className="fw-bold mb-4">
        <i className="bi bi-shield-lock me-2"></i>Admin Dashboard
      </h1>

      <div className="row mb-4">
        <div className="col-md-4 mb-3">
          <div className="stat-card">
            <div className="stat-number">{stats.users}</div>
            <div className="stat-label">Users</div>
          </div>
        </div>
        <div className="col-md-4 mb-3">
          <div className="stat-card">
            <div className="stat-number">{stats.routes}</div>
            <div className="stat-label">Routes</div>
          </div>
        </div>
        <div className="col-md-4 mb-3">
          <div className="stat-card">
            <div className="stat-number">{stats.reports}</div>
            <div className="stat-label">Wildlife Reports</div>
          </div>
        </div>
      </div>

      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button className={`nav-link ${tab === 'users' ? 'active' : ''}`}
                  onClick={() => setTab('users')}>Users ({users.length})</button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${tab === 'routes' ? 'active' : ''}`}
                  onClick={() => setTab('routes')}>Routes ({routes.length})</button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${tab === 'reports' ? 'active' : ''}`}
                  onClick={() => setTab('reports')}>Reports ({reports.length})</button>
        </li>
      </ul>

      {tab === 'users' && (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Username</th>
                <th>Email</th>
                <th>Role</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {users.map(u => (
                <tr key={u.id}>
                  <td>{u.id}</td>
                  <td>{u.username}</td>
                  <td>{u.email}</td>
                  <td><span className="badge bg-secondary">{u.role}</span></td>
                  <td>{new Date(u.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'routes' && (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Difficulty</th>
                <th>Distance</th>
                <th>Created By</th>
                <th>Rating</th>
              </tr>
            </thead>
            <tbody>
              {routes.map(r => (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td>{r.name}</td>
                  <td><span className={`difficulty-badge difficulty-${r.difficulty}`}>{r.difficulty}</span></td>
                  <td>{r.distanceKm} km</td>
                  <td>{r.createdByUsername}</td>
                  <td>{r.averageRating?.toFixed(1)} ({r.reviewCount})</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {tab === 'reports' && (
        <div className="table-responsive">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>ID</th>
                <th>Species</th>
                <th>Route</th>
                <th>User</th>
                <th>Description</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {reports.map(r => (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td>{r.species}</td>
                  <td>{r.routeName}</td>
                  <td>{r.username}</td>
                  <td>{r.description?.substring(0, 50)}</td>
                  <td>{new Date(r.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
