import { useState, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Marker, Polyline, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import { routeApi, routePointApi } from '../services/api';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

function haversineDistance(p1, p2) {
  const R = 6371;
  const dLat = (p2.lat - p1.lat) * Math.PI / 180;
  const dLon = (p2.lng - p1.lng) * Math.PI / 180;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(p1.lat * Math.PI / 180) * Math.cos(p2.lat * Math.PI / 180) *
    Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function ClickHandler({ onMapClick }) {
  useMapEvents({
    click(e) {
      onMapClick(e.latlng);
    },
  });
  return null;
}

function parseGPX(xmlText) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlText, 'text/xml');
  const trkpts = doc.querySelectorAll('trkpt');
  if (!trkpts.length) throw new Error('No track points found in GPX file');

  const points = [];
  trkpts.forEach((pt, i) => {
    const lat = parseFloat(pt.getAttribute('lat'));
    const lon = parseFloat(pt.getAttribute('lon'));
    if (!isNaN(lat) && !isNaN(lon)) {
      points.push({ latitude: lat, longitude: lon, sequenceNumber: i });
    }
  });

  let totalDist = 0;
  for (let i = 1; i < points.length; i++) {
    totalDist += haversineDistance(
      { lat: points[i - 1].latitude, lng: points[i - 1].longitude },
      { lat: points[i].latitude, lng: points[i].longitude },
    );
  }

  return { points, distance: Math.round(totalDist * 100) / 100 };
}

export default function CreateRoute() {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  const [form, setForm] = useState({
    name: '', description: '', difficulty: 'EASY',
    distanceKm: '', elevationGain: '',
  });
  const [routePoints, setRoutePoints] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [gpxFile, setGpxFile] = useState(null);

  const handleMapClick = useCallback((latlng) => {
    setRoutePoints(prev => [...prev, {
      latitude: latlng.lat,
      longitude: latlng.lng,
      sequenceNumber: prev.length,
    }]);
  }, []);

  const undoPoint = () => {
    setRoutePoints(prev => prev.slice(0, -1));
  };

  const clearPoints = () => {
    setRoutePoints([]);
    setGpxFile(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleGPXUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    setError('');
    const reader = new FileReader();
    reader.onload = (evt) => {
      try {
        const result = parseGPX(evt.target.result);
        setRoutePoints(result.points);
        setGpxFile(file.name);
        if (!form.distanceKm || form.distanceKm === '') {
          setForm(f => ({ ...f, distanceKm: result.distance }));
        }
      } catch (err) {
        setError('Failed to parse GPX file: ' + err.message);
      }
    };
    reader.readAsText(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (routePoints.length < 2) {
      setError('Please add at least 2 points on the map (click or upload GPX).');
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        ...form,
        distanceKm: parseFloat(form.distanceKm),
        elevationGain: form.elevationGain ? parseFloat(form.elevationGain) : null,
        startLatitude: routePoints[0].latitude,
        startLongitude: routePoints[0].longitude,
        endLatitude: routePoints[routePoints.length - 1].latitude,
        endLongitude: routePoints[routePoints.length - 1].longitude,
      };

      const routeRes = await routeApi.create(payload);
      const routeId = routeRes.data.id;

      const pointsPayload = routePoints.map((p, i) => ({
        latitude: p.latitude,
        longitude: p.longitude,
        sequenceNumber: i,
      }));

      await routePointApi.addBatch(routeId, pointsPayload);
      navigate(`/routes/${routeId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create route');
    } finally {
      setSubmitting(false);
    }
  };

  const polylinePositions = routePoints.map(p => [p.latitude, p.longitude]);

  return (
    <div className="container py-4">
      <h1 className="fw-bold mb-4">
        <i className="bi bi-plus-circle me-2"></i>Create New Route
      </h1>

      <div className="row">
        <div className="col-lg-5">
          <form onSubmit={handleSubmit}>
            {error && <div className="alert alert-danger">{error}</div>}

            <div className="card shadow-sm mb-3">
              <div className="card-body">
                <h6 className="card-title">
                  <i className="bi bi-upload me-1"></i>Import GPX File
                </h6>
                <input type="file" ref={fileInputRef}
                       className="form-control form-control-sm"
                       accept=".gpx" onChange={handleGPXUpload} />
                {gpxFile && (
                  <small className="text-success">
                    <i className="bi bi-check-circle me-1"></i>{gpxFile}
                  </small>
                )}
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label">Route Name *</label>
              <input type="text" className="form-control" required
                     value={form.name}
                     onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
            </div>

            <div className="mb-3">
              <label className="form-label">Description</label>
              <textarea className="form-control" rows="3"
                        value={form.description}
                        onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
            </div>

            <div className="row mb-3">
              <div className="col-md-6">
                <label className="form-label">Difficulty *</label>
                <select className="form-select" value={form.difficulty}
                        onChange={e => setForm(f => ({ ...f, difficulty: e.target.value }))}>
                  <option value="EASY">Easy</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HARD">Hard</option>
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Distance (km) *</label>
                <input type="number" step="0.1" className="form-control" required
                       value={form.distanceKm}
                       onChange={e => setForm(f => ({ ...f, distanceKm: e.target.value }))} />
              </div>
            </div>

            <div className="mb-3">
              <label className="form-label">Elevation Gain (m)</label>
              <input type="number" step="1" className="form-control"
                     value={form.elevationGain}
                     onChange={e => setForm(f => ({ ...f, elevationGain: e.target.value }))} />
            </div>

            <div className="mb-3">
              <label className="form-label">
                Route Points ({routePoints.length})
              </label>
              <div className="d-flex gap-2">
                <button type="button" className="btn btn-sm btn-outline-danger"
                        onClick={undoPoint} disabled={routePoints.length === 0}>
                  <i className="bi bi-arrow-counterclockwise"></i> Undo
                </button>
                <button type="button" className="btn btn-sm btn-outline-secondary"
                        onClick={clearPoints} disabled={routePoints.length === 0}>
                  <i className="bi bi-x-circle"></i> Clear
                </button>
              </div>
              <p className="text-muted small mt-1">
                Click on the map to add points, or upload a GPX file above.
              </p>
            </div>

            <button type="submit" className="btn btn-success w-100 py-2"
                    disabled={submitting}>
              {submitting ? (
                <><span className="spinner-border spinner-border-sm me-2" />Creating...</>
              ) : (
                <><i className="bi bi-check-lg me-2"></i>Create Route</>
              )}
            </button>
          </form>
        </div>

        <div className="col-lg-7">
          <div className="map-container-create shadow-sm">
            <MapContainer center={[46.5, 11.5]} zoom={8} className="h-100 w-100">
              <TileLayer url="https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png"
                         attribution="&copy; <a href='https://opentopomap.org'>OpenTopoMap</a> contributors" />
              <ClickHandler onMapClick={handleMapClick} />
              {routePoints.length > 0 && (
                <Marker position={[routePoints[0].latitude, routePoints[0].longitude]} />
              )}
              {routePoints.length > 1 && (
                <Marker position={[routePoints[routePoints.length - 1].latitude, routePoints[routePoints.length - 1].longitude]} />
              )}
              {polylinePositions.length > 1 && (
                <Polyline positions={polylinePositions} color="#28a745" weight={4} />
              )}
            </MapContainer>
          </div>
        </div>
      </div>
    </div>
  );
}
