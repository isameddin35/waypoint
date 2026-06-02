import { AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';

function haversineDistance(p1, p2) {
  const R = 6371;
  const dLat = (p2.latitude - p1.latitude) * Math.PI / 180;
  const dLon = (p2.longitude - p1.longitude) * Math.PI / 180;
  const a = Math.sin(dLat / 2) ** 2 +
    Math.cos(p1.latitude * Math.PI / 180) * Math.cos(p2.latitude * Math.PI / 180) *
    Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

export default function ElevationProfile({ points }) {
  if (!points || points.length < 2 || points.every(p => p.elevation === null || p.elevation === undefined)) {
    return null;
  }

  let cumulativeDist = 0;
  const data = [{ distance: 0, elevation: Math.round(points[0].elevation || 0) }];
  for (let i = 1; i < points.length; i++) {
    cumulativeDist += haversineDistance(points[i - 1], points[i]);
    data.push({
      distance: Math.round(cumulativeDist * 100) / 100,
      elevation: Math.round(points[i].elevation || 0),
    });
  }

  const elevations = data.map(d => d.elevation);
  const minElev = Math.min(...elevations);
  const maxElev = Math.max(...elevations);
  const totalDist = cumulativeDist;

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-dark text-white px-3 py-2 rounded shadow-sm" style={{ fontSize: 13 }}>
          <div><strong>{payload[0].value} m</strong></div>
          <div className="text-muted">{payload[0].payload.distance} km</div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="card shadow-sm mb-4">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h5 className="card-title mb-0">
            <i className="bi bi-graph-up me-2"></i>Elevation Profile
          </h5>
          <small className="text-muted">
            {points.length} points &middot; {totalDist.toFixed(1)} km &middot;
            {minElev}m &ndash; {maxElev}m
          </small>
        </div>

        <ResponsiveContainer width="100%" height={200}>
          <AreaChart data={data} margin={{ top: 5, right: 10, left: -20, bottom: 0 }}>
            <defs>
              <linearGradient id="elevationGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#28a745" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#28a745" stopOpacity={0.02} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#e9ecef" />
            <XAxis
              dataKey="distance"
              tick={{ fontSize: 11, fill: '#6c757d' }}
              tickFormatter={v => `${v}km`}
            />
            <YAxis
              tick={{ fontSize: 11, fill: '#6c757d' }}
              tickFormatter={v => `${v}m`}
              domain={[minElev - 50 > 0 ? minElev - 50 : 0, maxElev + 50]}
            />
            <Tooltip content={<CustomTooltip />} />
            <Area
              type="monotone"
              dataKey="elevation"
              stroke="#28a745"
              strokeWidth={2}
              fill="url(#elevationGradient)"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
