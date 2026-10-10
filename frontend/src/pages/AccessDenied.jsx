import { useNavigate } from "react-router-dom";

import {
  ArrowLeft,
  ShieldAlert,
} from "lucide-react";

function AccessDenied() {
  const navigate = useNavigate();

  return (
    <div className="system-state-page">
      <div className="system-state-glow" />

      <div className="system-state-card">
        <div className="system-state-icon danger">
          <ShieldAlert size={30} />
        </div>

        <div className="system-state-code">
          403
        </div>

        <h1>
          Access denied
        </h1>

        <p>
          You do not have permission to access
          this DevSpace resource.
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

export default AccessDenied;