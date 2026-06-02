import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { routeApi } from '../services/api';
import RouteCard from '../components/RouteCard';

export default function Home() {
  const [featured, setFeatured] = useState([]);
  const [stats, setStats] = useState({ routes: 0, users: 0 });
  const [search, setSearch] = useState('');

  useEffect(() => {
    routeApi.getFeatured().then(r => setFeatured(r.data)).catch(() => {});
    routeApi.getCount().then(r => setStats(s => ({ ...s, routes: r.data }))).catch(() => {});
  }, []);

  return (
    <div>
      <section className="hero-section">
        <div className="container text-center">
          <h1 className="display-4 fw-bold mb-3">
            <i className="bi bi-tree-fill me-2"></i>Discover Trekking Routes
          </h1>
          <p className="lead mb-4">
            Explore, create, and share the best hiking trails. Your adventure starts here.
          </p>
          <div className="row justify-content-center mb-4">
            <div className="col-md-6">
              <div className="input-group input-group-lg">
                <input type="text" className="form-control" placeholder="Search routes..."
                       value={search} onChange={e => setSearch(e.target.value)}
                       onKeyDown={e => e.key === 'Enter' && (window.location.href = `/routes?search=${search}`)} />
                <Link to={`/routes?search=${search}`} className="btn btn-light">
                  <i className="bi bi-search"></i>
                </Link>
              </div>
            </div>
          </div>
          <div className="d-flex gap-3 justify-content-center">
            <Link to="/routes" className="btn btn-light btn-lg px-4">
              Browse Routes
            </Link>
            <Link to="/create-route" className="btn btn-outline-light btn-lg px-4">
              Create Route
            </Link>
          </div>
        </div>
      </section>

      <section className="container py-5">
        <div className="row">
          <div className="col-md-4 mb-3">
            <div className="stat-card">
              <div className="stat-number">{stats.routes}</div>
              <div className="stat-label">Routes Available</div>
            </div>
          </div>
          <div className="col-md-4 mb-3">
            <div className="stat-card">
              <div className="stat-number">
                <i className="bi bi-people-fill text-success"></i>
              </div>
              <div className="stat-label">Active Hikers Community</div>
            </div>
          </div>
          <div className="col-md-4 mb-3">
            <div className="stat-card">
              <div className="stat-number">
                <i className="bi bi-map-fill text-success"></i>
              </div>
              <div className="stat-label">Interactive Maps</div>
            </div>
          </div>
        </div>
      </section>

      {featured.length > 0 && (
        <section className="container py-4">
          <h2 className="mb-4 fw-bold">
            <i className="bi bi-star-fill text-warning me-2"></i>Featured Routes
          </h2>
          <div className="row">
            {featured.map(route => <RouteCard key={route.id} route={route} />)}
          </div>
        </section>
      )}
    </div>
  );
}
