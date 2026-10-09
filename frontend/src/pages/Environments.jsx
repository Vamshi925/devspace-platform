import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  deleteEnvironment,
  extendEnvironment,
  getMyEnvironments,
} from "../api/environmentApi";

function Environments() {
  const navigate = useNavigate();

  const [environments, setEnvironments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadEnvironments = async () => {
    try {
      setLoading(true);
      const data = await getMyEnvironments();
      setEnvironments(data);
      setError("");
    } catch {
      setError("Unable to load environments");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEnvironments();
  }, []);

  const handleExtend = async (environmentId) => {
    try {
      await extendEnvironment(environmentId, 2);
      await loadEnvironments();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to extend environment"
      );
    }
  };

  const handleDelete = async (environmentId) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this environment?"
    );

    if (!confirmed) return;

    try {
      await deleteEnvironment(environmentId);
      await loadEnvironments();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to delete environment"
      );
    }
  };

  if (loading) {
    return <p>Loading environments...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div>
      <h1>My Environments</h1>

      <button onClick={() => navigate("/dashboard")}>
        Dashboard
      </button>

      <button onClick={() => navigate("/environments/create")}>
        Create Environment
      </button>

      <hr />

      {environments.length === 0 ? (
        <p>No environments found.</p>
      ) : (
        environments.map((environment) => (
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

            <p>
              <strong>URL:</strong>{" "}
              {environment.applicationUrl ? (
                <a
                  href={environment.applicationUrl}
                  target="_blank"
                  rel="noreferrer"
                >
                  Open Environment
                </a>
              ) : (
                "-"
              )}
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

            {" "}

            {environment.status === "READY" && (
              <button
                onClick={() =>
                  handleExtend(
                    environment.environmentId
                  )
                }
              >
                Extend 2 Hours
              </button>
            )}

            {" "}

            {!["DELETED", "DELETING", "PROVISIONING"].includes(
              environment.status
            ) && (
              <button
                onClick={() =>
                  handleDelete(
                    environment.environmentId
                  )
                }
              >
                Delete
              </button>
            )}
          </div>
        ))
      )}
    </div>
  );
}

export default Environments;
