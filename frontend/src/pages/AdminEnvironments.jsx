import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  Eye,
  Search,
  Server,
  Users,
} from "lucide-react";

import apiClient from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
import PageLoader from "../components/PageLoader";
import EnvironmentTableSkeleton from "../components/EnvironmentTableSkeleton";
import AccessDenied from "./AccessDenied";

function AdminEnvironments() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [environments, setEnvironments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] =
    useState("ALL");

  const loadEnvironments = async () => {
    try {
      setLoading(true);

      const response = await apiClient.get(
        "/api/environments"
      );

      setEnvironments(response.data);
      setError("");
    } catch (error) {
      setError(
        error.response?.data?.message ||
          "Unable to load environments"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.role === "ROLE_ADMIN") {
      loadEnvironments();
    } else {
      setLoading(false);
    }
  }, [user]);

  const filteredEnvironments = useMemo(() => {
    return environments.filter((environment) => {
      const query = search.toLowerCase();

      const matchesSearch =
        environment.applicationName
          ?.toLowerCase()
          .includes(query) ||
        environment.environmentCode
          ?.toLowerCase()
          .includes(query) ||
        environment.userId
          ?.toLowerCase()
          .includes(query);

      const matchesStatus =
        statusFilter === "ALL" ||
        environment.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [environments, search, statusFilter]);

  const getStatusClass = (status) => {
    switch (status) {
      case "READY":
        return "status-ready";

      case "PROVISIONING":
        return "status-provisioning";

      case "FAILED":
        return "status-failed";

      case "EXPIRED":
        return "status-expired";

      case "DELETED":
      case "DELETING":
        return "status-deleted";

      default:
        return "status-deleted";
    }
  };

  if (user?.role !== "ROLE_ADMIN") {
  return <AccessDenied />;
}

 if (loading) {
  return <EnvironmentTableSkeleton />;
}

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            All Environments
          </h1>

          <p className="page-subtitle">
            Monitor and inspect developer environments
            across the entire DevSpace platform.
          </p>
        </div>

        <div className="admin-summary-pill">
          <Server size={17} />
          {environments.length} total
        </div>
      </div>

      <div className="environment-toolbar">
        <div className="environment-search">
          <Search size={17} />

          <input
            type="text"
            placeholder="Search by application, code or user ID..."
            value={search}
            onChange={(event) =>
              setSearch(event.target.value)
            }
          />
        </div>

        <select
          className="environment-filter"
          value={statusFilter}
          onChange={(event) =>
            setStatusFilter(event.target.value)
          }
        >
          <option value="ALL">
            All Statuses
          </option>

          <option value="READY">
            Ready
          </option>

          <option value="PROVISIONING">
            Provisioning
          </option>

          <option value="FAILED">
            Failed
          </option>

          <option value="EXPIRED">
            Expired
          </option>

          <option value="DELETING">
            Deleting
          </option>

          <option value="DELETED">
            Deleted
          </option>
        </select>
      </div>

      {error && (
        <div className="glass-panel environment-error">
          {error}
        </div>
      )}

      <div className="environment-summary-line">
        Showing{" "}
        <strong>
          {filteredEnvironments.length}
        </strong>{" "}
        of{" "}
        <strong>
          {environments.length}
        </strong>{" "}
        environments
      </div>

      {filteredEnvironments.length === 0 ? (
        <div className="glass-panel empty-state">
          <h3>No environments found</h3>

          <p>
            Try adjusting the search or status filter.
          </p>
        </div>
      ) : (
        <div className="table-container">
          <table className="dev-table">
            <thead>
              <tr>
                <th>Application</th>
                <th>User</th>
                <th>Status</th>
                <th>Stage</th>
                <th>Type</th>
                <th>Namespace</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {filteredEnvironments.map(
                (environment) => (
                  <tr
                    key={environment.environmentId}
                  >
                    <td>
                      <div className="environment-name">
                        {environment.applicationName}
                      </div>

                      <div className="environment-code">
                        {environment.environmentCode}
                      </div>
                    </td>

                    <td>
                      <div className="admin-user-cell">
                        <Users size={14} />

                        <span>
                          {environment.userId}
                        </span>
                      </div>
                    </td>

                    <td>
                      <span
                        className={`status-badge ${getStatusClass(
                          environment.status
                        )}`}
                      >
                        {environment.status}
                      </span>
                    </td>

                    <td>
                      <span className="environment-stage">
                        {environment.provisioningStage ||
                          "-"}
                      </span>
                    </td>

                    <td>
                      {environment.environmentType}
                    </td>

                    <td>
                      <span className="admin-namespace">
                        {environment.namespace ||
                          "Not assigned"}
                      </span>
                    </td>

                    <td>
                      <button
                        className="btn btn-ghost admin-details-button"
                        onClick={() =>
                          navigate(
                            `/environments/${environment.environmentId}`
                          )
                        }
                      >
                        <Eye size={15} />
                        View Details
                      </button>
                    </td>
                  </tr>
                )
              )}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

export default AdminEnvironments;