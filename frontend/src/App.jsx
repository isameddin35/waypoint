import { Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Home from './pages/Home';
import RoutesPage from './pages/RoutesPage';
import RouteDetail from './pages/RouteDetail';
import CreateRoute from './pages/CreateRoute';
import RecordRoute from './pages/RecordRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import AdminDashboard from './pages/AdminDashboard';
import ProtectedRoute from './components/ProtectedRoute';

export default function App() {
  return (
    <AuthProvider>
      <div className="d-flex flex-column min-vh-100">
        <Navbar />
        <main className="flex-grow-1">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/routes" element={<RoutesPage />} />
            <Route path="/routes/:id" element={<RouteDetail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/create-route" element={
              <ProtectedRoute><CreateRoute /></ProtectedRoute>
            } />
            <Route path="/record-route" element={
              <ProtectedRoute><RecordRoute /></ProtectedRoute>
            } />
            <Route path="/profile" element={
              <ProtectedRoute><Profile /></ProtectedRoute>
            } />
            <Route path="/admin" element={
              <ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>
            } />
          </Routes>
        </main>
        <Footer />
      </div>
    </AuthProvider>
  );
}
