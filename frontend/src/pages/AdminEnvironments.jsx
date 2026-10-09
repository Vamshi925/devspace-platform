import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import apiClient from "../api/apiClient";
import { useAuth } from "../context/AuthContext";

function AdminEnvironments() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [environments, setEnvironments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadEnvironments = async () => {
      try {
        const response = await apiClient.get("/api/environments");
        setEnvironments(response.data);
      } catch (error) {
        setError(
          error.response?.data?.message ||
            "Unable to load environments"
        );
      } finally {
        setLoading(false);
      }
    };

    if (user?.role === "ROLE_ADMIN") {
      loadEnvironments();
    } else {
      setLoading(false);
    }
  }, [user]);

  if (user?.role !== "ROLE_ADMIN") {
    return <p>Access denied</p>;
  }

  if (loading) {
    return <p>Loading environments...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div>
      <button onClick={() => navigate("/dashboard")}>
        Dashboard
      </button>

      <h1>Admin - All Environments</h1>

      <p>Total Environments: {environments.length}</p>

      {environments.map((environment) => (
        <div
          key={environment.environmentId}
          style={{
            border: "1px solid #ccc",
            padding: "15px",
            marginBottom: "15px",
          }}
        >
          <h3>{environment.applicationName}</h3>

          <p>
            <strong>User:</strong>{" "}
            {environment.userId}
          </p>

          <p>
            <strong>Code:</strong>{" "}
            {environment.environmentCode}
          </p>

          <p>
            <strong>Status:</strong>{" "}
            {environment.status}
          </p>

          <p>
            <strong>Provisioning Stage:</strong>{" "}
            {environment.provisioningStage || "-"}
          </p>

          <p>
            <strong>Expires:</strong>{" "}
            {new Date(
              environment.expiresAt
            ).toLocaleString()}
          </p>

          <button
            onClick={() =>
              navigate(
                `/environments/${environment.environmentId}`
              )
            }
          >
            View Details
          </button>
        </div>
      ))}
    </div>
  );
}

export default AdminEnvironments;