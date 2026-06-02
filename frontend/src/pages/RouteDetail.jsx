import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Polyline, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import { routeApi, routePointApi, reviewApi, photoApi, wildlifeApi, favoriteApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import ElevationProfile from '../components/ElevationProfile';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

function haversineDistance(p1, p2) {
  const R = 6371;
  const dLat = (p2.latitude - p1.latitude) * Math.PI / 180;
  const dLon = (p2.longitude - p1.longitude) * Math.PI / 180;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(p1.latitude * Math.PI / 180) * Math.cos(p2.latitude * Math.PI / 180) *
    Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function ClickableTrack({ points }) {
  const map = useMap();
  const positions = points.map(p => [p.latitude, p.longitude]);

  if (positions.length < 2) return null;

  return (
    <Polyline
      positions={positions}
      color="#28a745"
      weight={4}
      eventHandlers={{
        click: (e) => {
          const { lat, lng } = e.latlng;
          let nearestIdx = 0;
          let minDist = Infinity;
          points.forEach((p, i) => {
            const d = (p.latitude - lat) ** 2 + (p.longitude - lng) ** 2;
            if (d < minDist) { minDist = d; nearestIdx = i; }
          });
          const pt = points[nearestIdx];

          let cumDist = 0;
          for (let i = 1; i <= nearestIdx; i++) {
            cumDist += haversineDistance(points[i - 1], points[i]);
          }

          const elev = (pt.elevation != null)
            ? `${Math.round(pt.elevation)} m`
            : 'No data';

          L.popup()
            .setLatLng([lat, lng])
            .setContent(`
              <div style="font-size:13px;min-width:160px">
                <strong>Elevation: ${elev}</strong><br/>
                <span style="color:#666">${pt.latitude.toFixed(4)}, ${pt.longitude.toFixed(4)}</span><br/>
                <span style="color:#666">${cumDist.toFixed(2)} km from start</span>
              </div>
            `)
            .openOn(map);
        },
      }}
    />
  );
}

export default function RouteDetail() {
  const { id } = useParams();
  const { user } = useAuth();
  const [route, setRoute] = useState(null);
  const [points, setPoints] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [photos, setPhotos] = useState([]);
  const [wildlife, setWildlife] = useState([]);
  const [isFav, setIsFav] = useState(false);
  const [loading, setLoading] = useState(true);
  const [fetchingElevation, setFetchingElevation] = useState(false);
  const [newReview, setNewReview] = useState({ rating: 5, comment: '' });
  const [newPhoto, setNewPhoto] = useState(null);

  useEffect(() => {
    Promise.all([
      routeApi.getById(id),
      routePointApi.getByRoute(id),
      reviewApi.getByRoute(id),
      photoApi.getByRoute(id),
      wildlifeApi.getByRoute(id),
    ]).then(([r, p, rev, ph, w]) => {
      setRoute(r.data);
      setPoints(p.data);
      setReviews(rev.data);
      setPhotos(ph.data);
      setWildlife(w.data);
    }).catch(() => {}).finally(() => setLoading(false));

    if (user) {
      favoriteApi.check(id).then(r => setIsFav(r.data.favorite)).catch(() => {});
    }
  }, [id, user]);

  const handleFavorite = async () => {
    try {
      await favoriteApi.toggle(id);
      setIsFav(!isFav);
    } catch (e) {}
  };

  const handleReview = async (e) => {
    e.preventDefault();
    try {
      const res = await reviewApi.create(id, newReview);
      setReviews(prev => [res.data, ...prev]);
      setNewReview({ rating: 5, comment: '' });
    } catch (e) {}
  };

  const handleFetchElevation = async () => {
    setFetchingElevation(true);
    try {
      const res = await routePointApi.fetchElevation(id);
      setPoints(res.data);
    } catch (e) {}
    setFetchingElevation(false);
  };

  const handlePhotoUpload = async (e) => {
    e.preventDefault();
    if (!newPhoto) return;
    try {
      const res = await photoApi.upload(id, newPhoto);
      setPhotos(prev => [res.data, ...prev]);
      setNewPhoto(null);
      e.target.reset();
    } catch (e) {}
  };

  if (loading) return <div className="text-center py-5"><div className="spinner-border" /></div>;
  if (!route) return <div className="container py-5"><h3>Route not found</h3></div>;

  const hasCoords = route.startLatitude && route.startLongitude;
  const mapCenter = hasCoords ? [route.startLatitude, route.startLongitude] : [20, 0];

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-start mb-4">
        <div>
          <Link to="/routes" className="text-decoration-none text-muted small">
            <i className="bi bi-arrow-left me-1"></i>Back to routes
          </Link>
          <h1 className="fw-bold mt-2">{route.name}</h1>
          <span className={`difficulty-badge difficulty-${route.difficulty} me-2`}>
            {route.difficulty}
          </span>
          <small className="text-muted">by {route.createdByUsername}</small>
        </div>
        {user && (
          <button className={`btn ${isFav ? 'btn-danger' : 'btn-outline-danger'}`}
                  onClick={handleFavorite}>
            <i className={`bi ${isFav ? 'bi-heart-fill' : 'bi-heart'} me-1`}></i>
            {isFav ? 'Saved' : 'Save'}
          </button>
        )}
      </div>

      <div className="row">
        <div className="col-lg-8">
          <div className="map-container mb-4">
            <MapContainer center={mapCenter} zoom={12} className="h-100 w-100">
              <TileLayer url="https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png"
                         attribution="&copy; <a href='https://opentopomap.org'>OpenTopoMap</a> contributors" />
              {hasCoords && (
                <Marker position={[route.startLatitude, route.startLongitude]}>
                  <Popup>Start: {route.name}</Popup>
                </Marker>
              )}
              {route.endLatitude && route.endLongitude && (
                <Marker position={[route.endLatitude, route.endLongitude]}>
                  <Popup>End: {route.name}</Popup>
                </Marker>
              )}
              {points.length > 1 && <ClickableTrack points={points} />}
            </MapContainer>
          </div>

          <ElevationProfile points={points} />

          {points.length > 1 && points.every(p => p.elevation === null || p.elevation === undefined) && (
            <div className="mb-3">
              <button className="btn btn-outline-success btn-sm"
                      onClick={handleFetchElevation} disabled={fetchingElevation}>
                {fetchingElevation ? (
                  <><span className="spinner-border spinner-border-sm me-1" />Fetching elevation...</>
                ) : (
                  <><i className="bi bi-graph-up me-1"></i>Fetch Elevation Data</>
                )}
              </button>
              <small className="text-muted ms-2">Free from Open-Meteo API</small>
            </div>
          )}

          <div className="card shadow-sm mb-4">
            <div className="card-body">
              <h5 className="card-title">Description</h5>
              <p className="card-text">{route.description || 'No description provided.'}</p>
            </div>
          </div>

          {photos.length > 0 && (
            <div className="mb-4">
              <h5><i className="bi bi-images me-2"></i>Photos</h5>
              <div className="photo-grid row">
                {photos.map(photo => (
                  <div key={photo.id} className="col-md-4 mb-3">
                    <img src={photo.fileUrl} alt="Route" className="img-fluid rounded shadow-sm" />
                  </div>
                ))}
              </div>
            </div>
          )}

          {user && (
            <div className="card shadow-sm mb-4">
              <div className="card-body">
                <h5 className="card-title">Upload Photo</h5>
                <form onSubmit={handlePhotoUpload}>
                  <input type="file" className="form-control mb-2" accept="image/*"
                         onChange={e => setNewPhoto(e.target.files[0])} required />
                  <button type="submit" className="btn btn-success btn-sm">
                    <i className="bi bi-upload me-1"></i>Upload
                  </button>
                </form>
              </div>
            </div>
          )}

          <div className="card shadow-sm mb-4">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-chat-dots me-2"></i>Reviews ({reviews.length})
              </h5>
              {user && (
                <form onSubmit={handleReview} className="mb-4 p-3 bg-light rounded">
                  <div className="mb-2">
                    <label className="form-label">Rating</label>
                    <div className="rating-stars">
                      {[1, 2, 3, 4, 5].map(s => (
                        <i key={s} className={`bi ${s <= newReview.rating ? 'bi-star-fill' : 'bi-star'} fs-4 me-1 cursor-pointer`}
                           style={{ cursor: 'pointer' }} onClick={() => setNewReview(r => ({ ...r, rating: s }))}></i>
                      ))}
                    </div>
                  </div>
                  <div className="mb-2">
                    <textarea className="form-control" rows="2" placeholder="Write a review..."
                              value={newReview.comment}
                              onChange={e => setNewReview(r => ({ ...r, comment: e.target.value }))} />
                  </div>
                  <button type="submit" className="btn btn-success btn-sm">
                    <i className="bi bi-send me-1"></i>Submit Review
                  </button>
                </form>
              )}
              {reviews.length === 0 ? (
                <p className="text-muted">No reviews yet.</p>
              ) : (
                reviews.map(review => (
                  <div key={review.id} className="border-bottom pb-3 mb-3">
                    <div className="d-flex justify-content-between">
                      <strong>{review.username}</strong>
                      <div className="rating-stars">
                        {[...Array(5)].map((_, i) => (
                          <i key={i} className={`bi ${i < review.rating ? 'bi-star-fill' : 'bi-star'} small`}></i>
                        ))}
                      </div>
                    </div>
                    {review.comment && <p className="mb-0 mt-1">{review.comment}</p>}
                    <small className="text-muted">{new Date(review.createdAt).toLocaleDateString()}</small>
                  </div>
                ))
              )}
            </div>
          </div>

          {wildlife.length > 0 && (
            <div className="card shadow-sm mb-4">
              <div className="card-body">
                <h5 className="card-title">
                  <i className="bi bi-bug me-2"></i>Wildlife Reports
                </h5>
                {wildlife.map(w => (
                  <div key={w.id} className="border-bottom pb-2 mb-2">
                    <strong>{w.species}</strong>
                    <p className="mb-0 small">{w.description}</p>
                    <small className="text-muted">Reported by {w.username} on {new Date(w.createdAt).toLocaleDateString()}</small>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="col-lg-4">
          <div className="card shadow-sm mb-4">
            <div className="card-body">
              <h5 className="card-title">Route Details</h5>
              <table className="table table-borderless mb-0">
                <tbody>
                  <tr>
                    <td><i className="bi bi-rulers me-2"></i>Distance</td>
                    <td className="text-end fw-bold">{route.distanceKm} km</td>
                  </tr>
                  {route.elevationGain && (
                    <tr>
                      <td><i className="bi bi-arrow-up me-2"></i>Elevation Gain</td>
                      <td className="text-end fw-bold">{route.elevationGain} m</td>
                    </tr>
                  )}
                  <tr>
                    <td><i className="bi bi-bar-chart me-2"></i>Difficulty</td>
                    <td className="text-end">
                      <span className={`difficulty-badge difficulty-${route.difficulty}`}>
                        {route.difficulty}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <td><i className="bi bi-star me-2"></i>Rating</td>
                    <td className="text-end">
                      <span className="rating-stars">
                        {[...Array(5)].map((_, i) => (
                          <i key={i} className={`bi ${i < Math.round(route.averageRating) ? 'bi-star-fill' : 'bi-star'} small`}></i>
                        ))}
                      </span>
                      <span className="ms-1">({route.reviewCount})</span>
                    </td>
                  </tr>
                  <tr>
                    <td><i className="bi bi-person me-2"></i>Created by</td>
                    <td className="text-end">{route.createdByUsername}</td>
                  </tr>
                  <tr>
                    <td><i className="bi bi-calendar me-2"></i>Date</td>
                    <td className="text-end">{new Date(route.createdAt).toLocaleDateString()}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
