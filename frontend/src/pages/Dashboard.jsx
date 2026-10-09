import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { getDashboardSummary } from "../api/dashboardApi";
import { useAuth } from "../context/AuthContext";

function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [summary, setSummary] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        const data = await getDashboardSummary();
        setSummary(data);
      } catch {
        setError("Unable to load dashboard");
      }
    };

    loadDashboard();
  }, []);

  if (error) {
    return <p>{error}</p>;
  }

  if (!summary) {
    return <p>Loading...</p>;
  }

  return (
    <div>
      <h1>DevSpace Dashboard</h1>

      <p>
        Logged in as: {user?.email}
      </p>

      <p>
        Role: {user?.role}
      </p>

      <button onClick={() => navigate("/environments")}>
        My Environments
      </button>

      {" "}

      <button onClick={() => navigate("/templates")}>
        Templates
      </button>

      {" "}

      <button onClick={() => navigate("/notifications")}>
        Notifications ({summary.unreadNotifications})
      </button>

      {" "}

     {user?.role === "ROLE_ADMIN" && (
  <>
    <button
      onClick={() =>
        navigate("/admin/environments")
      }
    >
      All Environments
    </button>

    {" "}

    <button
      onClick={() =>
        navigate("/admin/templates")
      }
    >
      Manage Templates
    </button>
  </>
)}

      {" "}

      <button onClick={logout}>
        Logout
      </button>

      <hr />

      <h2>Environment Summary</h2>

      <p>Total: {summary.total}</p>
      <p>Ready: {summary.ready}</p>
      <p>Provisioning: {summary.provisioning}</p>
      <p>Failed: {summary.failed}</p>
      <p>Expired: {summary.expired}</p>
      <p>Deleted: {summary.deleted}</p>

      <p>
        Unread Notifications:{" "}
        {summary.unreadNotifications}
      </p>
    </div>
  );
}

export default Dashboard;