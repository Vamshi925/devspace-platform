import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Eye, Search, Server, Users, X } from "lucide-react";
import apiClient from "../api/apiClient";
import { useAuth } from "../context/AuthContext";
import EnvironmentTableSkeleton from "../components/EnvironmentTableSkeleton";
import AccessDenied from "./AccessDenied";
import { getErrorMessage } from "../utils/getErrorMessage";

function AdminEnvironments() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [environments, setEnvironments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [typeFilter, setTypeFilter] = useState("ALL");

  const loadEnvironments = async () => {
    try {
      setLoading(true);
      const response = await apiClient.get("/api/environments");
      setEnvironments(response.data || []);
      setError("");
    } catch (error) {
      setError(
        getErrorMessage(error, "Unable to load environments")
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.role === "ROLE_ADMIN") loadEnvironments();
    else setLoading(false);
  }, [user]);

  const filteredEnvironments = useMemo(() => {
    const query = search.trim().toLowerCase();

    return environments.filter((environment) => {
      const searchable = [
        environment.applicationName,
        environment.environmentCode,
        environment.userId,
        environment.namespace,
        environment.provisioningStage,
        environment.environmentType,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      const matchesSearch = !query || searchable.includes(query);
      const matchesStatus =
        statusFilter === "ALL" || environment.status === statusFilter;
      const matchesType =
        typeFilter === "ALL" || environment.environmentType === typeFilter;

      return matchesSearch && matchesStatus && matchesType;
    });
  }, [environments, search, statusFilter, typeFilter]);

  const environmentTypes = useMemo(
    () => [
      ...new Set(
        environments
          .map((environment) => environment.environmentType)
          .filter(Boolean)
      ),
    ].sort(),
    [environments]
  );

  const clearFilters = () => {
    setSearch("");
    setStatusFilter("ALL");
    setTypeFilter("ALL");
  };

  const hasFilters =
    Boolean(search) || statusFilter !== "ALL" || typeFilter !== "ALL";

  const getStatusClass = (status) => {
    switch (status) {
      case "READY":
        return "status-ready";
      case "REQUESTED":
      case "PROVISIONING":
        return "status-provisioning";
      case "FAILED":
        return "status-failed";
      case "EXPIRED":
        return "status-expired";
      case "DELETED":
      case "DELETING":
      default:
        return "status-deleted";
    }
  };

  if (user?.role !== "ROLE_ADMIN") return <AccessDenied />;
  if (loading) return <EnvironmentTableSkeleton />;

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">All Environments</h1>
          <p className="page-subtitle">
            Monitor and inspect developer environments across the entire DevSpace platform.
          </p>
        </div>
        <div className="admin-summary-pill">
          <Server size={17} />
          {environments.length} total
        </div>
      </div>

      {error && (
        <div className="glass-panel environment-error">{error}</div>
      )}

      <div className="admin-environment-toolbar">
        <div className="environment-search">
          <Search size={17} />
          <input
            type="text"
            placeholder="Search application, code, user, namespace..."
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
          {search && (
            <button
              className="admin-environment-search-clear"
              onClick={() => setSearch("")}
              aria-label="Clear search"
            >
              <X size={15} />
            </button>
          )}
        </div>

        <select
          className="environment-filter"
          value={statusFilter}
          onChange={(event) => setStatusFilter(event.target.value)}
        >
          <option value="ALL">All Statuses</option>
          <option value="REQUESTED">Requested</option>
          <option value="PROVISIONING">Provisioning</option>
          <option value="READY">Ready</option>
          <option value="FAILED">Failed</option>
          <option value="EXPIRED">Expired</option>
          <option value="DELETING">Deleting</option>
          <option value="DELETED">Deleted</option>
        </select>

        <select
          className="environment-filter"
          value={typeFilter}
          onChange={(event) => setTypeFilter(event.target.value)}
        >
          <option value="ALL">All Types</option>
          {environmentTypes.map((type) => (
            <option key={type} value={type}>{type}</option>
          ))}
        </select>

        {hasFilters && (
          <button
            className="btn btn-ghost admin-environment-clear"
            onClick={clearFilters}
          >
            <X size={14} />
            Clear
          </button>
        )}
      </div>

      <div className="admin-environment-summary">
        <span>
          Showing <strong>{filteredEnvironments.length}</strong> of{" "}
          <strong>{environments.length}</strong> environments
        </span>
        {hasFilters && (
          <span className="admin-environment-filtered-label">Filtered results</span>
        )}
      </div>

      {filteredEnvironments.length === 0 ? (
        <div className="glass-panel admin-environment-empty">
          <div className="admin-environment-empty-icon">
            <Server size={28} />
          </div>
          <h3>No environments found</h3>
          <p>
            {hasFilters
              ? "No environments match the current search or filters."
              : "There are no environments available on the platform yet."}
          </p>
          {hasFilters && (
            <button className="btn btn-ghost" onClick={clearFilters}>
              Clear Filters
            </button>
          )}
        </div>
      ) : (
        <div className="table-container admin-environments-table">
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
              {filteredEnvironments.map((environment) => (
                <tr key={environment.environmentId}>
                  <td>
                    <div className="environment-name">{environment.applicationName}</div>
                    <div className="environment-code">{environment.environmentCode}</div>
                  </td>
                  <td>
                    <div className="admin-user-cell">
                      <Users size={14} />
                      <span>{environment.userId}</span>
                    </div>
                  </td>
                  <td>
                    <span className={`status-badge ${getStatusClass(environment.status)}`}>
                      {environment.status}
                    </span>
                  </td>
                  <td>
                    <span className="environment-stage">
                      {environment.provisioningStage || "-"}
                    </span>
                  </td>
                  <td>
                    <span className="environment-type-label">
                      {environment.environmentType || "-"}
                    </span>
                  </td>
                  <td>
                    <span className="admin-namespace">
                      {environment.namespace || "Not assigned"}
                    </span>
                  </td>
                  <td>
                    <button
                      className="btn btn-ghost admin-details-button"
                      onClick={() =>
                        navigate(`/environments/${environment.environmentId}`)
                      }
                    >
                      <Eye size={15} />
                      View Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  );
}

export default AdminEnvironments;
