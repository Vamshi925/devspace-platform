import { useNavigate } from "react-router-dom";

import {
  ArrowLeft,
  SearchX,
} from "lucide-react";

function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="system-state-page">
      <div className="system-state-glow" />

      <div className="system-state-card">
        <div className="system-state-icon">
          <SearchX size={30} />
        </div>

        <div className="system-state-code">
          404
        </div>

        <h1>
          Page not found
        </h1>

        <p>
          The page you are looking for does not
          exist or may have been moved.
        </p>

        <button
          className="btn btn-primary"
          onClick={() =>
            navigate("/dashboard")
          }
        >
          <ArrowLeft size={16} />
          Back to Dashboard
        </button>
      </div>
    </div>
  );
}

export default NotFound;