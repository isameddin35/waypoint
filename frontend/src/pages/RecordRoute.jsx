import { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapContainer, TileLayer, Polyline, CircleMarker, useMap } from 'react-leaflet';
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
  const dLat = (p2.latitude - p1.latitude) * Math.PI / 180;
  const dLon = (p2.longitude - p1.longitude) * Math.PI / 180;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(p1.latitude * Math.PI / 180) * Math.cos(p2.latitude * Math.PI / 180) *
    Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function MapController({ center }) {
  const map = useMap();
  useEffect(() => {
    if (center) map.panTo(center, { animate: true, duration: 0.5 });
  }, [center, map]);
  return null;
}

function LocateButton() {
  const map = useMap();
  const handleClick = () => {
    map.locate({ setView: true, maxZoom: 16 });
  };
  return (
    <div className="leaflet-bottom leaflet-left" style={{ zIndex: 1000, marginBottom: 20, marginLeft: 10 }}>
      <button onClick={handleClick}
              className="btn btn-light btn-sm shadow"
              title="Locate me"
              style={{ width: 34, height: 34, borderRadius: 4, border: '2px solid rgba(0,0,0,0.2)', padding: 0 }}>
        <i className="bi bi-crosshair" style={{ fontSize: 18 }}></i>
      </button>
    </div>
  );
}

export default function RecordRoute() {
  const navigate = useNavigate();
  const [tracking, setTracking] = useState(false);
  const [paused, setPaused] = useState(false);
  const [recordedPoints, setRecordedPoints] = useState([]);
  const [currentPos, setCurrentPos] = useState(null);
  const [locating, setLocating] = useState(true);
  const [elapsed, setElapsed] = useState(0);
  const [distance, setDistance] = useState(0);
  const [speed, setSpeed] = useState(0);
  const [accuracy, setAccuracy] = useState(null);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState({
    name: '', description: '', difficulty: 'EASY', elevationGain: '',
  });

  const watchIdRef = useRef(null);
  const timerRef = useRef(null);
  const lastPointRef = useRef(null);
  const trackingRef = useRef(false);
  const pausedRef = useRef(false);
  const pointsAccumulatedRef = useRef(0);
  const MIN_DISTANCE = 5;

  // Acquire GPS with continuous refinement via watchPosition
  const initialWatchIdRef = useRef(null);
  useEffect(() => {
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser.');
      setLocating(false);
      return;
    }
    const timeoutId = setTimeout(() => {
      setLocating(false);
    }, 30000);
    initialWatchIdRef.current = navigator.geolocation.watchPosition(
      (pos) => {
        const { latitude, longitude, accuracy: acc } = pos.coords;
        setCurrentPos({ latitude, longitude });
        setAccuracy(Math.round(acc));
        if (acc <= 100) {
          setLocating(false);
          clearTimeout(timeoutId);
          if (initialWatchIdRef.current !== null) {
            navigator.geolocation.clearWatch(initialWatchIdRef.current);
            initialWatchIdRef.current = null;
          }
        }
      },
      (err) => {
        if (err.code === err.PERMISSION_DENIED) {
          setError('Location access denied. Map shows default area.');
        }
        setLocating(false);
        clearTimeout(timeoutId);
        if (initialWatchIdRef.current !== null) {
          navigator.geolocation.clearWatch(initialWatchIdRef.current);
          initialWatchIdRef.current = null;
        }
      },
      { enableHighAccuracy: true, timeout: 30000, maximumAge: 0 }
    );
    return () => {
      clearTimeout(timeoutId);
      if (initialWatchIdRef.current !== null) {
        navigator.geolocation.clearWatch(initialWatchIdRef.current);
      }
    };
  }, []);

  const clearTracking = useCallback(() => {
    if (watchIdRef.current !== null) {
      navigator.geolocation.clearWatch(watchIdRef.current);
      watchIdRef.current = null;
    }
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  useEffect(() => {
    return () => clearTracking();
  }, [clearTracking]);

  useEffect(() => {
    if (tracking && !paused) {
      timerRef.current = setInterval(() => {
        setElapsed(prev => prev + 1);
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
      timerRef.current = null;
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [tracking, paused]);

  // Stable ref-based handlers avoid stale closure in watchPosition
  const handleSuccessRef = useRef(null);
  const handleErrorRef = useRef(null);

  handleSuccessRef.current = (pos) => {
    const { latitude, longitude, accuracy: acc } = pos.coords;
    setCurrentPos({ latitude, longitude });
    setAccuracy(Math.round(acc));

    if (pausedRef.current) return;

    const pt = { latitude, longitude, sequenceNumber: pointsAccumulatedRef.current };
    setRecordedPoints(prev => {
      if (!lastPointRef.current) {
        lastPointRef.current = pt;
        pointsAccumulatedRef.current = prev.length + 1;
        return [...prev, pt];
      }
      const seg = haversineDistance(lastPointRef.current, pt);
      if (seg >= MIN_DISTANCE / 1000) {
        setDistance(d => d + seg);
        setSpeed(seg > 0 ? seg / (1 / 3600) : 0);
        lastPointRef.current = pt;
        pointsAccumulatedRef.current = prev.length + 1;
        return [...prev, pt];
      }
      return prev;
    });
  };

  handleErrorRef.current = (err) => {
    switch (err.code) {
      case err.PERMISSION_DENIED:
        setError('Location permission denied. Please enable GPS access.');
        break;
      case err.POSITION_UNAVAILABLE:
        setError('GPS signal unavailable. Try moving outdoors.');
        break;
      case err.TIMEOUT:
        setError('GPS request timed out.');
        break;
      default:
        setError('An unknown GPS error occurred.');
    }
  };

  const startTracking = () => {
    setError('');
    setShowForm(false);
    setRecordedPoints([]);
    setDistance(0);
    setSpeed(0);
    setElapsed(0);
    setAccuracy(null);
    setCurrentPos(null);
    lastPointRef.current = null;
    pointsAccumulatedRef.current = 0;

    // Stop the initial passive watch before starting the recording watch
    if (initialWatchIdRef.current !== null) {
      navigator.geolocation.clearWatch(initialWatchIdRef.current);
      initialWatchIdRef.current = null;
    }

    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser.');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const { latitude, longitude, accuracy: acc } = pos.coords;
        setCurrentPos({ latitude, longitude });
        setAccuracy(Math.round(acc));
        const startPt = { latitude, longitude, sequenceNumber: 0 };
        setRecordedPoints([startPt]);
        lastPointRef.current = startPt;
        pointsAccumulatedRef.current = 1;

        trackingRef.current = true;
        pausedRef.current = false;
        setTracking(true);
        setPaused(false);

        watchIdRef.current = navigator.geolocation.watchPosition(
          (p) => handleSuccessRef.current(p),
          (e) => handleErrorRef.current(e),
          { enableHighAccuracy: true, timeout: 30000, maximumAge: 0 }
        );
      },
      (e) => handleErrorRef.current(e),
      { enableHighAccuracy: true, timeout: 30000, maximumAge: 0 }
    );
  };

  const pauseTracking = () => {
    pausedRef.current = true;
    setPaused(true);
  };

  const resumeTracking = () => {
    pausedRef.current = false;
    setPaused(false);
  };

  const stopTracking = () => {
    clearTracking();
    trackingRef.current = false;
    pausedRef.current = false;
    setTracking(false);
    setPaused(false);
    if (recordedPoints.length >= 2) {
      setShowForm(true);
    }
  };

  const handleDiscard = () => {
    setRecordedPoints([]);
    setDistance(0);
    setSpeed(0);
    setElapsed(0);
    setShowForm(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (recordedPoints.length < 2) {
      setError('Need at least 2 GPS points. Walk a bit more.');
      return;
    }
    setSubmitting(true);
    try {
      const totalDist = distance > 0 ? distance : recordedPoints.reduce((acc, p, i) => {
        if (i === 0) return 0;
        return acc + haversineDistance(recordedPoints[i - 1], p);
      }, 0);

      const payload = {
        name: form.name || `Recorded Route ${new Date().toLocaleDateString()}`,
        description: form.description,
        difficulty: form.difficulty,
        distanceKm: Math.round(totalDist * 100) / 100,
        elevationGain: form.elevationGain ? parseFloat(form.elevationGain) : null,
        startLatitude: recordedPoints[0].latitude,
        startLongitude: recordedPoints[0].longitude,
        endLatitude: recordedPoints[recordedPoints.length - 1].latitude,
        endLongitude: recordedPoints[recordedPoints.length - 1].longitude,
      };

      const routeRes = await routeApi.create(payload);
      const routeId = routeRes.data.id;

      const pointsPayload = recordedPoints.map((p, i) => ({
        latitude: p.latitude,
        longitude: p.longitude,
        sequenceNumber: i,
      }));
      await routePointApi.addBatch(routeId, pointsPayload);
      navigate(`/routes/${routeId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save route');
    } finally {
      setSubmitting(false);
    }
  };

  const formatTime = (sec) => {
    const h = Math.floor(sec / 3600);
    const m = Math.floor((sec % 3600) / 60);
    const s = sec % 60;
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
  };

  const polylinePositions = recordedPoints.map(p => [p.latitude, p.longitude]);
  const mapCenter = currentPos
    ? [currentPos.latitude, currentPos.longitude]
    : [46.5, 11.5];

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h1 className="fw-bold mb-0">
          <i className="bi bi-record-circle me-2 text-danger"></i>Record Route
        </h1>
        <div>
          {locating && (
            <span className="badge bg-secondary">
              <span className="spinner-border spinner-border-sm me-1" role="status" />
              Locating...
            </span>
          )}
          {currentPos && !locating && !tracking && accuracy !== null && (
            <span className={`badge ${accuracy < 30 ? 'bg-success' : accuracy < 100 ? 'bg-warning text-dark' : 'bg-danger'} ${accuracy >= 100 ? 'recording-pulse' : ''}`}
                  title={`Accuracy: ${accuracy}m`}>
              <i className="bi bi-check-circle me-1"></i>
              {accuracy < 30 ? 'GPS lock' : accuracy < 100 ? 'WiFi fix' : 'Low accuracy'}
            </span>
          )}
          <small className="text-muted ms-2">{recordedPoints.length} points</small>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row">
        <div className="col-lg-8 mb-3">
          <div className="map-container-create shadow-sm position-relative">
            <MapContainer center={mapCenter} zoom={16} className="h-100 w-100">
              <TileLayer url="https://{s}.tile.opentopomap.org/{z}/{x}/{y}.png"
                         attribution="&copy; <a href='https://opentopomap.org'>OpenTopoMap</a> contributors" />
              <MapController center={mapCenter} />
              <LocateButton />
              {polylinePositions.length > 1 && (
                <Polyline positions={polylinePositions} color="#dc3545" weight={4} />
              )}
              {currentPos && (
                <>
                  <CircleMarker center={[currentPos.latitude, currentPos.longitude]}
                    radius={8} pathOptions={{ color: '#dc3545', fillColor: '#dc3545', fillOpacity: 0.8 }} />
                  {accuracy && (
                    <CircleMarker center={[currentPos.latitude, currentPos.longitude]}
                      radius={accuracy} pathOptions={{ color: '#dc3545', fillOpacity: 0.1, weight: 1 }} />
                  )}
                </>
              )}
            </MapContainer>
          </div>
        </div>

        <div className="col-lg-4">
          <div className="card shadow-sm mb-3">
            <div className="card-body">
              <h5 className="card-title">
                <i className="bi bi-speedometer2 me-2"></i>Live Stats
              </h5>
              <table className="table table-borderless mb-0">
                <tbody>
                  <tr>
                    <td><i className="bi bi-clock me-2"></i>Duration</td>
                    <td className="text-end fw-bold">{formatTime(elapsed)}</td>
                  </tr>
                  <tr>
                    <td><i className="bi bi-rulers me-2"></i>Distance</td>
                    <td className="text-end fw-bold">
                      {distance > 0 ? `${distance.toFixed(2)} km` : '0.00 km'}
                    </td>
                  </tr>
                  <tr>
                    <td><i className="bi bi-speedometer me-2"></i>Pace</td>
                    <td className="text-end fw-bold">
                      {distance > 0
                        ? `${(elapsed / 60 / distance).toFixed(1)} min/km`
                        : '--:--'}
                    </td>
                  </tr>
                  <tr>
                    <td><i className="bi bi-satellite me-2"></i>Accuracy</td>
                    <td className="text-end fw-bold">
                      {accuracy !== null ? (
                        <span className={accuracy < 30 ? 'text-success' : accuracy < 100 ? 'text-warning' : 'text-danger'}>
                          {accuracy} m
                        </span>
                      ) : '--'}
                    </td>
                  </tr>
                </tbody>
              </table>
              {accuracy !== null && accuracy >= 100 && (
                <div className="alert alert-warning py-1 px-2 mt-2 mb-0 small">
                  <i className="bi bi-info-circle me-1"></i>
                  Accuracy &gt;100m — for precise tracking, use a phone with GPS outdoors.
                </div>
              )}
            </div>
          </div>

          <div className="card shadow-sm">
            <div className="card-body">
              {!tracking && !showForm && (
                <button className="btn btn-danger w-100 py-3 mb-2"
                        onClick={startTracking}
                        disabled={locating}>
                  {locating ? (
                    <><span className="spinner-border spinner-border-sm me-2" />Acquiring GPS...</>
                  ) : (
                    <><i className="bi bi-record-circle fs-4 me-2"></i>Start Recording</>
                  )}
                </button>
              )}

              {tracking && (
                <div className="d-flex gap-2 mb-2">
                  {paused ? (
                    <button className="btn btn-success flex-fill py-2"
                            onClick={resumeTracking}>
                      <i className="bi bi-play-fill me-1"></i>Resume
                    </button>
                  ) : (
                    <button className="btn btn-warning flex-fill py-2"
                            onClick={pauseTracking}>
                      <i className="bi bi-pause-fill me-1"></i>Pause
                    </button>
                  )}
                  <button className="btn btn-dark flex-fill py-2"
                          onClick={stopTracking}>
                    <i className="bi bi-stop-fill me-1"></i>Stop
                  </button>
                </div>
              )}

              {tracking && (
                <div className="d-flex align-items-center justify-content-center mb-2">
                  <span className={`badge ${paused ? 'bg-warning' : 'bg-danger recording-pulse'} px-3 py-2`}>
                    {paused ? (
                      <><i className="bi bi-pause-fill me-1"></i>Paused</>
                    ) : (
                      <><i className="bi bi-record-circle me-1"></i>Recording...</>
                    )}
                  </span>
                </div>
              )}

              {showForm && (
                <form onSubmit={handleSubmit}>
                  {recordedPoints.length >= 2 && (
                    <div className="alert alert-success py-2 small">
                      <i className="bi bi-check-circle me-1"></i>
                      Recorded {recordedPoints.length} points, {distance.toFixed(2)} km
                    </div>
                  )}

                  <div className="mb-2">
                    <label className="form-label small">Route Name</label>
                    <input type="text" className="form-control form-control-sm"
                           placeholder={`Recorded Route ${new Date().toLocaleDateString()}`}
                           value={form.name}
                           onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
                  </div>

                  <div className="mb-2">
                    <label className="form-label small">Description</label>
                    <textarea className="form-control form-control-sm" rows="2"
                              value={form.description}
                              onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
                  </div>

                  <div className="row mb-2">
                    <div className="col-6">
                      <label className="form-label small">Difficulty</label>
                      <select className="form-select form-select-sm"
                              value={form.difficulty}
                              onChange={e => setForm(f => ({ ...f, difficulty: e.target.value }))}>
                        <option value="EASY">Easy</option>
                        <option value="MEDIUM">Medium</option>
                        <option value="HARD">Hard</option>
                      </select>
                    </div>
                    <div className="col-6">
                      <label className="form-label small">Elevation (m)</label>
                      <input type="number" step="1" className="form-control form-control-sm"
                             value={form.elevationGain}
                             onChange={e => setForm(f => ({ ...f, elevationGain: e.target.value }))} />
                    </div>
                  </div>

                  <div className="d-flex gap-2">
                    <button type="submit" className="btn btn-success flex-fill py-2"
                            disabled={submitting}>
                      {submitting ? (
                        <><span className="spinner-border spinner-border-sm me-1" />Saving...</>
                      ) : (
                        <><i className="bi bi-check-lg me-1"></i>Save Route</>
                      )}
                    </button>
                    <button type="button" className="btn btn-outline-secondary flex-fill py-2"
                            onClick={handleDiscard} disabled={submitting}>
                      <i className="bi bi-trash me-1"></i>Discard
                    </button>
                  </div>
                </form>
              )}

              {!tracking && recordedPoints.length >= 2 && !showForm && (
                <div className="d-flex gap-2">
                  <button className="btn btn-success flex-fill py-2"
                          onClick={() => setShowForm(true)}>
                    <i className="bi bi-check-lg me-1"></i>Save Route
                  </button>
                  <button className="btn btn-outline-secondary flex-fill py-2"
                          onClick={handleDiscard}>
                    <i className="bi bi-trash me-1"></i>Discard
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
