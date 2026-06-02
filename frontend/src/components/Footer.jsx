export default function Footer() {
  return (
    <footer className="py-4 mt-5">
      <div className="container text-center">
        <p className="mb-0">
          <i className="bi bi-tree-fill me-2"></i>
          Waypoint &copy; {new Date().getFullYear()} &mdash; Your Trekking Route Platform
        </p>
      </div>
    </footer>
  );
}
