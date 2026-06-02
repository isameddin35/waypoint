import { Link } from 'react-router-dom';

export default function RouteCard({ route }) {
  const stars = Math.round(route.averageRating || 0);

  return (
    <div className="col-md-6 col-lg-4 mb-4">
      <div className="card route-card h-100 shadow-sm">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-start mb-2">
            <h5 className="card-title mb-0">{route.name}</h5>
            <span className={`difficulty-badge difficulty-${route.difficulty}`}>
              {route.difficulty}
            </span>
          </div>
          <p className="card-text text-muted small">
            {route.description?.substring(0, 120)}
            {route.description?.length > 120 ? '...' : ''}
          </p>
          <div className="d-flex justify-content-between align-items-center mt-3">
            <div className="text-muted small">
              <i className="bi bi-geo-alt me-1"></i>{route.distanceKm} km
            </div>
            <div className="rating-stars">
              {[...Array(5)].map((_, i) => (
                <i key={i} className={`bi ${i < stars ? 'bi-star-fill' : 'bi-star'} me-1`}></i>
              ))}
              <span className="text-muted ms-1 small">({route.reviewCount})</span>
            </div>
          </div>
          {route.elevationGain && (
            <div className="text-muted small mt-1">
              <i className="bi bi-arrow-up me-1"></i>{route.elevationGain}m elevation
            </div>
          )}
        </div>
        <div className="card-footer bg-transparent border-top-0">
          <Link to={`/routes/${route.id}`} className="btn btn-outline-success btn-sm w-100">
            View Details
          </Link>
        </div>
      </div>
    </div>
  );
}
