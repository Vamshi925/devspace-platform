import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  useNavigate,
} from "react-router-dom";

import {
  CircleCheckBig,
  CircleX,
  Clock3,
  ExternalLink,
  Eye,
  LoaderCircle,
  Plus,
  RotateCcw,
  Search,
  Server,
  Trash2,
} from "lucide-react";

import {
  deleteEnvironment,
  extendEnvironment,
  getMyEnvironments,
} from "../api/environmentApi";

import EnvironmentTableSkeleton
  from "../components/EnvironmentTableSkeleton";

import ConfirmModal
  from "../components/ConfirmModal";

import Toast
  from "../components/Toast";

function Environments() {
  const navigate = useNavigate();

  const [
    environments,
    setEnvironments,
  ] = useState([]);

  const [
    loading,
    setLoading,
  ] = useState(true);

  const [
    error,
    setError,
  ] = useState("");

  const [
    search,
    setSearch,
  ] = useState("");

  const [
    statusFilter,
    setStatusFilter,
  ] = useState("ALL");

  const [
    toast,
    setToast,
  ] = useState(null);

  const [
    selectedEnvironment,
    setSelectedEnvironment,
  ] = useState(null);

  const [
    confirmationAction,
    setConfirmationAction,
  ] = useState(null);

  const [
    actionLoading,
    setActionLoading,
  ] = useState(false);

  const loadEnvironments = async () => {
    try {
      setLoading(true);

      const data =
        await getMyEnvironments();

      setEnvironments(
        data || []
      );

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
    loadEnvironments();
  }, []);

  const showToast = (
    type,
    message
  ) => {
    setToast({
      type,
      message,
    });

    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  const requestExtend = (
    environment
  ) => {
    setSelectedEnvironment(
      environment
    );

    setConfirmationAction(
      "EXTEND"
    );
  };

  const requestDelete = (
    environment
  ) => {
    setSelectedEnvironment(
      environment
    );

    setConfirmationAction(
      "DELETE"
    );
  };

  const closeConfirmation =
    () => {
      if (actionLoading) {
        return;
      }

      setSelectedEnvironment(
        null
      );

      setConfirmationAction(
        null
      );
    };

  const handleConfirmAction =
    async () => {
      if (!selectedEnvironment) {
        return;
      }

      try {
        setActionLoading(true);

        if (
          confirmationAction ===
          "EXTEND"
        ) {
          await extendEnvironment(
            selectedEnvironment
              .environmentId,
            2
          );

          showToast(
            "success",
            `"${selectedEnvironment.applicationName}" extended by 2 hours.`
          );
        }

        if (
          confirmationAction ===
          "DELETE"
        ) {
          await deleteEnvironment(
            selectedEnvironment
              .environmentId
          );

          showToast(
            "success",
            `Deletion started for "${selectedEnvironment.applicationName}".`
          );
        }

        setSelectedEnvironment(
          null
        );

        setConfirmationAction(
          null
        );

        await loadEnvironments();
      } catch (error) {
        showToast(
          "error",
          error.response?.data?.message ||
            "Unable to update environment"
        );
      } finally {
        setActionLoading(false);
      }
    };

  const filteredEnvironments =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase();

      return environments.filter(
        (environment) => {
          const matchesSearch =
            environment
              .applicationName
              ?.toLowerCase()
              .includes(query) ||
            environment
              .environmentCode
              ?.toLowerCase()
              .includes(query);

          const matchesStatus =
            statusFilter === "ALL" ||
            environment.status ===
              statusFilter;

          return (
            matchesSearch &&
            matchesStatus
          );
        }
      );
    }, [
      environments,
      search,
      statusFilter,
    ]);

  const getStatusClass = (
    status
  ) => {
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
        return "status-deleted";

      default:
        return "status-deleted";
    }
  };

  const getRemainingTime = (
    expiresAt,
    status
  ) => {
    if (
      !expiresAt ||
      [
        "DELETED",
        "DELETING",
        "EXPIRED",
      ].includes(status)
    ) {
      return null;
    }

    const difference =
      new Date(expiresAt).getTime() -
      Date.now();

    if (difference <= 0) {
      return "Expired";
    }

    const totalMinutes =
      Math.floor(
        difference / 60000
      );

    const hours =
      Math.floor(
        totalMinutes / 60
      );

    const minutes =
      totalMinutes % 60;

    if (hours > 0) {
      return `${hours}h ${minutes}m remaining`;
    }

    return `${minutes}m remaining`;
  };

  const counts = useMemo(
    () => ({
      total:
        environments.length,

      ready:
        environments.filter(
          (environment) =>
            environment.status ===
            "READY"
        ).length,

      provisioning:
        environments.filter(
          (environment) =>
            [
              "REQUESTED",
              "PROVISIONING",
            ].includes(
              environment.status
            )
        ).length,

      failed:
        environments.filter(
          (environment) =>
            environment.status ===
            "FAILED"
        ).length,
    }),
    [environments]
  );

  const hasFilters =
    search.trim() !== "" ||
    statusFilter !== "ALL";

  const clearFilters = () => {
    setSearch("");
    setStatusFilter("ALL");
  };

  if (loading) {
    return (
      <EnvironmentTableSkeleton />
    );
  }

  return (
    <>
      {/* HEADER */}

      <div className="page-header">
        <div>
          <h1 className="page-title">
            My Environments
          </h1>

          <p className="page-subtitle">
            Create, monitor and manage
            your temporary developer
            environments.
          </p>
        </div>

        <button
          className="btn btn-primary"
          onClick={() =>
            navigate(
              "/environments/create"
            )
          }
        >
          <Plus size={17} />
          Create Environment
        </button>
      </div>

      {/* SUMMARY */}

      <div className="environment-metric-row">
        <div className="environment-metric-card">
          <div className="environment-metric-icon metric-purple">
            <Server size={16} />
          </div>

          <div>
            <span>
              Total
            </span>

            <strong>
              {counts.total}
            </strong>
          </div>
        </div>

        <div className="environment-metric-card">
          <div className="environment-metric-icon metric-green">
            <CircleCheckBig size={16} />
          </div>

          <div>
            <span>
              Ready
            </span>

            <strong>
              {counts.ready}
            </strong>
          </div>
        </div>

        <div className="environment-metric-card">
          <div className="environment-metric-icon metric-cyan">
            <LoaderCircle size={16} />
          </div>

          <div>
            <span>
              Provisioning
            </span>

            <strong>
              {counts.provisioning}
            </strong>
          </div>
        </div>

        <div className="environment-metric-card">
          <div className="environment-metric-icon metric-red">
            <CircleX size={16} />
          </div>

          <div>
            <span>
              Failed
            </span>

            <strong>
              {counts.failed}
            </strong>
          </div>
        </div>
      </div>

      {/* FILTERS */}

      <div className="environment-toolbar">
        <div className="environment-search">
          <Search size={17} />

          <input
            type="text"
            placeholder="Search by application or environment code..."
            value={search}
            onChange={(event) =>
              setSearch(
                event.target.value
              )
            }
          />
        </div>

        <select
          className="environment-filter"
          value={statusFilter}
          onChange={(event) =>
            setStatusFilter(
              event.target.value
            )
          }
        >
          <option value="ALL">
            All Statuses
          </option>

          <option value="REQUESTED">
            Requested
          </option>

          <option value="PROVISIONING">
            Provisioning
          </option>

          <option value="READY">
            Ready
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

        {hasFilters && (
          <button
            className="btn btn-ghost environment-clear-filter"
            onClick={
              clearFilters
            }
          >
            <RotateCcw
              size={14}
            />

            Clear
          </button>
        )}
      </div>

      {error && (
        <div className="glass-panel environment-error">
          {error}
        </div>
      )}

      <div className="environment-summary-line">
        Showing{" "}
        <strong>
          {
            filteredEnvironments.length
          }
        </strong>{" "}
        of{" "}
        <strong>
          {
            environments.length
          }
        </strong>{" "}
        environments
      </div>

      {/* EMPTY STATE */}

      {filteredEnvironments.length ===
      0 ? (
        <div className="glass-panel environment-empty-state">
          <div className="environment-empty-icon">
            <Server size={28} />
          </div>

          <h3>
            {hasFilters
              ? "No matching environments"
              : "No environments yet"}
          </h3>

          <p>
            {hasFilters
              ? "Try changing your search or status filter."
              : "Launch your first temporary developer environment using an approved template."}
          </p>

          <div className="environment-empty-actions">
            {hasFilters && (
              <button
                className="btn btn-ghost"
                onClick={
                  clearFilters
                }
              >
                <RotateCcw
                  size={15}
                />

                Clear Filters
              </button>
            )}

            <button
              className="btn btn-primary"
              onClick={() =>
                navigate(
                  "/environments/create"
                )
              }
            >
              <Plus size={16} />

              Create Environment
            </button>
          </div>
        </div>
      ) : (
        /* TABLE */

        <div className="table-container environments-table-container">
          <table className="dev-table">
            <thead>
              <tr>
                <th>
                  Application
                </th>

                <th>
                  Status
                </th>

                <th>
                  Stage
                </th>

                <th>
                  Type
                </th>

                <th>
                  Expires
                </th>

                <th>
                  Environment URL
                </th>

                <th>
                  Actions
                </th>
              </tr>
            </thead>

            <tbody>
              {filteredEnvironments.map(
                (
                  environment
                ) => {
                  const remaining =
                    getRemainingTime(
                      environment.expiresAt,
                      environment.status
                    );

                  return (
                    <tr
                      key={
                        environment.environmentId
                      }
                    >
                      <td>
                        <div className="environment-name">
                          {
                            environment.applicationName
                          }
                        </div>

                        <div className="environment-code">
                          {
                            environment.environmentCode
                          }
                        </div>
                      </td>

                      <td>
                        <span
                          className={`status-badge ${getStatusClass(
                            environment.status
                          )}`}
                        >
                          {
                            environment.status
                          }
                        </span>
                      </td>

                      <td>
                        <span className="environment-stage">
                          {
                            environment.provisioningStage ||
                            "-"
                          }
                        </span>
                      </td>

                      <td>
                        <span className="environment-type-label">
                          {
                            environment.environmentType
                          }
                        </span>
                      </td>

                      <td>
                        <div className="environment-expiry">
                          <Clock3
                            size={14}
                          />

                          <div>
                            <span>
                              {environment.expiresAt
                                ? new Date(
                                    environment.expiresAt
                                  ).toLocaleString()
                                : "-"}
                            </span>

                            {remaining && (
                              <small>
                                {
                                  remaining
                                }
                              </small>
                            )}
                          </div>
                        </div>
                      </td>

                      <td>
                        {environment.applicationUrl ? (
                          <a
                            href={
                              environment.applicationUrl
                            }
                            target="_blank"
                            rel="noreferrer"
                            className="environment-url"
                          >
                            Open

                            <ExternalLink
                              size={14}
                            />
                          </a>
                        ) : (
                          <span className="muted-text">
                            Not available
                          </span>
                        )}
                      </td>

                      <td>
                        <div className="environment-actions">
                          <button
                            className="btn btn-ghost environment-view-details"
                            onClick={() =>
                              navigate(
                                `/environments/${environment.environmentId}`
                              )
                            }
                          >
                            <Eye
                              size={14}
                            />

                            View
                          </button>

                          {environment.status ===
                            "READY" && (
                            <button
                              className="icon-button action-extend"
                              title="Extend 2 Hours"
                              onClick={() =>
                                requestExtend(
                                  environment
                                )
                              }
                            >
                              <Clock3
                                size={16}
                              />
                            </button>
                          )}

                          {![
                            "DELETED",
                            "DELETING",
                            "PROVISIONING",
                          ].includes(
                            environment.status
                          ) && (
                            <button
                              className="icon-button action-delete"
                              title="Delete Environment"
                              onClick={() =>
                                requestDelete(
                                  environment
                                )
                              }
                            >
                              <Trash2
                                size={16}
                              />
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                }
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* EXTEND */}

      <ConfirmModal
        open={
          confirmationAction ===
            "EXTEND" &&
          Boolean(
            selectedEnvironment
          )
        }
        title="Extend Environment?"
        message={
          selectedEnvironment
            ? `"${selectedEnvironment.applicationName}" will remain active for an additional 2 hours.`
            : ""
        }
        confirmText="Extend 2 Hours"
        loading={actionLoading}
        onCancel={
          closeConfirmation
        }
        onConfirm={
          handleConfirmAction
        }
      />

      {/* DELETE */}

      <ConfirmModal
        open={
          confirmationAction ===
            "DELETE" &&
          Boolean(
            selectedEnvironment
          )
        }
        title="Delete Environment?"
        message={
          selectedEnvironment
            ? `This will start deletion of "${selectedEnvironment.applicationName}". This action cannot be undone.`
            : ""
        }
        confirmText="Delete Environment"
        danger
        loading={actionLoading}
        onCancel={
          closeConfirmation
        }
        onConfirm={
          handleConfirmAction
        }
      />

      {/* TOAST */}

      {toast && (
        <Toast
          type={
            toast.type
          }
          message={
            toast.message
          }
          onClose={() =>
            setToast(null)
          }
        />
      )}
    </>
  );
}

export default Environments;