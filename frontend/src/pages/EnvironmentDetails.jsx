import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
  deleteEnvironment,
  extendEnvironment,
  getEnvironmentActivity,
  getEnvironmentById,
} from "../api/environmentApi";

function EnvironmentDetails() {
  const { environmentId } = useParams();
  const navigate = useNavigate();

  const [environment, setEnvironment] = useState(null);
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadEnvironment = async () => {
    try {
      setLoading(true);

      const [environmentData, activityData] =
        await Promise.all([
          getEnvironmentById(environmentId),
          getEnvironmentActivity(environmentId),
        ]);

      setEnvironment(environmentData);
      setActivities(activityData);
      setError("");
    } catch (error) {
      setError(
        error.response?.data?.message ||
          "Unable to load environment details"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEnvironment();
  }, [environmentId]);

  const handleExtend = async () => {
    try {
      await extendEnvironment(environmentId, 2);
      await loadEnvironment();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to extend environment"
      );
    }
  };

  const handleDelete = async () => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this environment?"
    );

    if (!confirmed) return;

    try {
      await deleteEnvironment(environmentId);
      await loadEnvironment();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to delete environment"
      );
    }
  };

  if (loading) {
    return <p>Loading environment...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div>
      <button onClick={() => navigate("/environments")}>
        Back
      </button>

      <h1>{environment.applicationName}</h1>

      <h2>Environment Details</h2>

      <p>
        <strong>Environment ID:</strong>{" "}
        {environment.environmentId}
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
        <strong>Type:</strong>{" "}
        {environment.environmentType}
      </p>

      <p>
        <strong>Template ID:</strong>{" "}
        {environment.templateId}
      </p>

      <p>
        <strong>Repository:</strong>{" "}
        <a
          href={environment.repositoryUrl}
          target="_blank"
          rel="noreferrer"
        >
          {environment.repositoryUrl}
        </a>
      </p>

      <p>
        <strong>Branch:</strong>{" "}
        {environment.branchName}
      </p>

      <p>
        <strong>Created:</strong>{" "}
        {new Date(
          environment.createdAt
        ).toLocaleString()}
      </p>

      <p>
        <strong>Expires:</strong>{" "}
        {new Date(
          environment.expiresAt
        ).toLocaleString()}
      </p>

      <p>
        <strong>Namespace:</strong>{" "}
        {environment.namespace || "-"}
      </p>

      <p>
        <strong>Application URL:</strong>{" "}
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

      <p>
        <strong>Failure Reason:</strong>{" "}
        {environment.failureReason || "-"}
      </p>

      {environment.status === "READY" && (
        <button onClick={handleExtend}>
          Extend 2 Hours
        </button>
      )}

      {" "}

      {!["DELETED", "DELETING", "PROVISIONING"].includes(
        environment.status
      ) && (
        <button onClick={handleDelete}>
          Delete Environment
        </button>
      )}

      <hr />

      <h2>Activity History</h2>

      {activities.length === 0 ? (
        <p>No activity history available.</p>
      ) : (
        activities.map((activity) => (
          <div
            key={activity.activityId}
            style={{
              borderLeft: "3px solid #999",
              paddingLeft: "12px",
              marginBottom: "15px",
            }}
          >
            <strong>{activity.type}</strong>

            <p>{activity.message}</p>

            <small>
              {new Date(
                activity.createdAt
              ).toLocaleString()}
            </small>
          </div>
        ))
      )}
    </div>
  );
}

export default EnvironmentDetails;
