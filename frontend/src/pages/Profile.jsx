import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { routeApi, favoriteApi } from '../services/api';
import RouteCard from '../components/RouteCard';

export default function Profile() {
  const { user } = useAuth();
  const [routes, setRoutes] = useState([]);
  const [favorites, setFavorites] = useState([]);
  const [tab, setTab] = useState('my');

  useEffect(() => {
    routeApi.getUserRoutes().then(r => setRoutes(r.data)).catch(() => {});
    favoriteApi.getAll().then(r => setFavorites(r.data)).catch(() => {});
  }, []);

  return (
    <div className="container py-4">
      <div className="d-flex align-items-center mb-4">
        <i className="bi bi-person-circle display-4 me-3 text-success"></i>
        <div>
          <h1 className="fw-bold mb-0">{user?.username}</h1>
          <p className="text-muted mb-0">{user?.email} &middot; Role: {user?.role}</p>
        </div>
      </div>

      <ul className="nav nav-tabs mb-4">
        <li className="nav-item">
          <button className={`nav-link ${tab === 'my' ? 'active' : ''}`}
                  onClick={() => setTab('my')}>
            <i className="bi bi-map me-1"></i>My Routes ({routes.length})
          </button>
        </li>
        <li className="nav-item">
          <button className={`nav-link ${tab === 'fav' ? 'active' : ''}`}
                  onClick={() => setTab('fav')}>
            <i className="bi bi-heart me-1"></i>Favorites ({favorites.length})
          </button>
        </li>
      </ul>

      {tab === 'my' && (
        routes.length === 0
          ? <p className="text-muted">You haven't created any routes yet.</p>
          : <div className="row">{routes.map(r => <RouteCard key={r.id} route={r} />)}</div>
      )}

      {tab === 'fav' && (
        favorites.length === 0
          ? <p className="text-muted">No favorite routes yet.</p>
          : <div className="row">{favorites.map(r => <RouteCard key={r.id} route={r} />)}</div>
      )}
    </div>
  );
}
