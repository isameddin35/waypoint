import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { routeApi } from '../services/api';
import RouteCard from '../components/RouteCard';

export default function RoutesPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [routes, setRoutes] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState(searchParams.get('search') || '');
  const [difficulty, setDifficulty] = useState('');

  useEffect(() => {
    setLoading(true);
    const params = { page, size: 9, sortBy: 'createdAt', sortDir: 'desc' };
    if (search) params.search = search;
    if (difficulty) params.difficulty = difficulty;

    routeApi.getAll(params)
      .then(res => {
        setRoutes(res.data.content);
        setTotalPages(res.data.totalPages);
        setTotalElements(res.data.totalElements);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page, search, difficulty]);

  useEffect(() => {
    const s = searchParams.get('search');
    if (s) setSearch(s);
  }, [searchParams]);

  return (
    <div className="container py-4">
      <h1 className="fw-bold mb-4">
        <i className="bi bi-signpost-2 me-2"></i>Trekking Routes
      </h1>

      <div className="row mb-4">
        <div className="col-md-6 mb-2 mb-md-0">
          <div className="input-group">
            <input type="text" className="form-control" placeholder="Search by name..."
                   value={search} onChange={e => setSearch(e.target.value)} />
            <button className="btn btn-success" onClick={() => setPage(0)}>
              <i className="bi bi-search"></i>
            </button>
            {search && (
              <button className="btn btn-outline-secondary" onClick={() => { setSearch(''); setPage(0); }}>
                Clear
              </button>
            )}
          </div>
        </div>
        <div className="col-md-3">
          <select className="form-select" value={difficulty}
                  onChange={e => { setDifficulty(e.target.value); setPage(0); }}>
            <option value="">All Difficulties</option>
            <option value="EASY">Easy</option>
            <option value="MEDIUM">Medium</option>
            <option value="HARD">Hard</option>
          </select>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-success" role="status" />
        </div>
      ) : routes.length === 0 ? (
        <div className="text-center py-5">
          <i className="bi bi-map display-1 text-muted"></i>
          <p className="lead mt-3">No routes found. Be the first to create one!</p>
        </div>
      ) : (
        <>
          <p className="text-muted mb-3">{totalElements} route{totalElements !== 1 ? 's' : ''} found</p>
          <div className="row">
            {routes.map(route => <RouteCard key={route.id} route={route} />)}
          </div>

          <nav className="mt-4">
            <ul className="pagination justify-content-center">
              <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
                <button className="page-link" onClick={() => setPage(p => p - 1)}>Previous</button>
              </li>
              {[...Array(totalPages)].map((_, i) => (
                <li key={i} className={`page-item ${page === i ? 'active' : ''}`}>
                  <button className="page-link" onClick={() => setPage(i)}>{i + 1}</button>
                </li>
              ))}
              <li className={`page-item ${page >= totalPages - 1 ? 'disabled' : ''}`}>
                <button className="page-link" onClick={() => setPage(p => p + 1)}>Next</button>
              </li>
            </ul>
          </nav>
        </>
      )}
    </div>
  );
}
