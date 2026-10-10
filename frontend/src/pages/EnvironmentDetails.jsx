import { useEffect, useState } from "react";
import {
  useNavigate,
  useParams,
} from "react-router-dom";

import {
  ArrowLeft,
  Clock3,
  ExternalLink,
  GitBranch,
  Layers3,
  Server,
  Trash2,
  CircleCheckBig,
  CircleX,
  LoaderCircle,
  History,
} from "lucide-react";

import {
  deleteEnvironment,
  extendEnvironment,
  getEnvironmentActivity,
  getEnvironmentById,
} from "../api/environmentApi";

import ConfirmModal from "../components/ConfirmModal";
import Toast from "../components/Toast";
import PageLoader from "../components/PageLoader";

function EnvironmentDetails() {
  const { environmentId } = useParams();
  const navigate = useNavigate();

  const [environment, setEnvironment] =
    useState(null);

  const [activity, setActivity] =
    useState([]);

  const [loading, setLoading] =
    useState(true);

  const [error, setError] =
    useState("");

  const [toast, setToast] =
    useState(null);

  const [deleteModalOpen, setDeleteModalOpen] =
    useState(false);

  const [extendModalOpen, setExtendModalOpen] =
    useState(false);

  const [actionLoading, setActionLoading] =
    useState(false);

  const loadDetails = async () => {
    try {
      setLoading(true);

      const [
        environmentData,
        activityData,
      ] = await Promise.all([
        getEnvironmentById(environmentId),
        getEnvironmentActivity(environmentId),
      ]);

      setEnvironment(environmentData);
      setActivity(activityData || []);
      setError("");
    } catch (error) {
      setError(
        error.response?.data?.message ||
          "Unable to load environment"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDetails();
  }, [environmentId]);

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

  const handleExtend = async () => {
    try {
      setActionLoading(true);

      await extendEnvironment(
        environmentId,
        2
      );

      setExtendModalOpen(false);

      showToast(
        "success",
        "Environment extended successfully by 2 hours."
      );

      await loadDetails();
    } catch (error) {
      showToast(
        "error",
        error.response?.data?.message ||
          "Unable to extend environment"
      );
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async () => {
    try {
      setActionLoading(true);

      await deleteEnvironment(
        environmentId
      );

      setDeleteModalOpen(false);

      showToast(
        "success",
        "Environment deletion started successfully."
      );

      await loadDetails();
    } catch (error) {
      showToast(
        "error",
        error.response?.data?.message ||
          "Unable to delete environment"
      );
    } finally {
      setActionLoading(false);
    }
  };

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

  const getStatusIcon = (status) => {
    switch (status) {
      case "READY":
        return (
          <CircleCheckBig size={24} />
        );

      case "PROVISIONING":
        return (
          <LoaderCircle size={24} />
        );

      case "FAILED":
        return (
          <CircleX size={24} />
        );

      default:
        return (
          <Server size={24} />
        );
    }
  };

  if (loading) {
return (
  <PageLoader
    title="Loading environments..."
    subtitle="Fetching your DevSpace environments"
  />
);
  }

  if (error) {
    return (
      <div className="glass-panel environment-error">
        {error}
      </div>
    );
  }

  if (!environment) {
    return null;
  }

  const canExtend =
    environment.status === "READY";

  const canDelete = ![
    "DELETED",
    "DELETING",
    "PROVISIONING",
  ].includes(environment.status);

  return (
    <>
      <button
        className="btn btn-ghost details-back"
        onClick={() =>
          navigate("/environments")
        }
      >
        <ArrowLeft size={16} />

        Back to Environments
      </button>

      <div className="environment-details-hero">
        <div className="details-hero-main">
          <div
            className={`details-status-icon ${getStatusClass(
              environment.status
            )}`}
          >
            {getStatusIcon(
              environment.status
            )}
          </div>

          <div>
            <div className="details-title-row">
              <h1 className="page-title">
                {
                  environment.applicationName
                }
              </h1>

              <span
                className={`status-badge ${getStatusClass(
                  environment.status
                )}`}
              >
                {
                  environment.status
                }
              </span>
            </div>

            <p className="details-code">
              {
                environment.environmentCode
              }
            </p>

            <p className="page-subtitle">
              Environment ID:{" "}
              {
                environment.environmentId
              }
            </p>
          </div>
        </div>

        <div className="details-actions">
          {canExtend && (
            <button
              className="btn btn-secondary"
              onClick={() =>
                setExtendModalOpen(true)
              }
            >
              <Clock3 size={16} />

              Extend 2 Hours
            </button>
          )}

          {canDelete && (
            <button
              className="btn btn-danger"
              onClick={() =>
                setDeleteModalOpen(true)
              }
            >
              <Trash2 size={16} />

              Delete Environment
            </button>
          )}
        </div>
      </div>

      <div className="details-grid">
        <div className="glass-panel details-card">
          <div className="details-card-header">
            <Server size={18} />

            <h3>
              Environment
            </h3>
          </div>

          <div className="details-info-grid">
            <div>
              <span className="details-label">
                Type
              </span>

              <strong>
                {
                  environment.environmentType
                }
              </strong>
            </div>

            <div>
              <span className="details-label">
                Provisioning Stage
              </span>

              <strong>
                {
                  environment.provisioningStage ||
                  "-"
                }
              </strong>
            </div>

            <div>
              <span className="details-label">
                Namespace
              </span>

              <strong>
                {
                  environment.namespace ||
                  "Not assigned"
                }
              </strong>
            </div>

            <div>
              <span className="details-label">
                Template ID
              </span>

              <strong className="details-small-value">
                {
                  environment.templateId
                }
              </strong>
            </div>
          </div>
        </div>

        <div className="glass-panel details-card">
          <div className="details-card-header">
            <Layers3 size={18} />

            <h3>
              Access
            </h3>
          </div>

          <div className="details-info-grid">
            <div>
              <span className="details-label">
                Application URL
              </span>

              {environment.applicationUrl ? (
                <a
                  href={
                    environment.applicationUrl
                  }
                  target="_blank"
                  rel="noreferrer"
                  className="details-link"
                >
                  Open Environment

                  <ExternalLink size={14} />
                </a>
              ) : (
                <strong>
                  Not available
                </strong>
              )}
            </div>

            <div>
              <span className="details-label">
                Expires At
              </span>

              <strong>
                {new Date(
                  environment.expiresAt
                ).toLocaleString()}
              </strong>
            </div>

            <div>
              <span className="details-label">
                Created At
              </span>

              <strong>
                {new Date(
                  environment.createdAt
                ).toLocaleString()}
              </strong>
            </div>
          </div>
        </div>

        <div className="glass-panel details-card">
          <div className="details-card-header">
            <GitBranch size={18} />

            <h3>
              Source Repository
            </h3>
          </div>

          <div className="details-info-grid">
            <div>
              <span className="details-label">
                Repository
              </span>

              {environment.repositoryUrl ? (
                <a
                  href={
                    environment.repositoryUrl
                  }
                  target="_blank"
                  rel="noreferrer"
                  className="details-link"
                >
                  Open Repository

                  <ExternalLink size={14} />
                </a>
              ) : (
                <strong>
                  -
                </strong>
              )}
            </div>

            <div>
              <span className="details-label">
                Branch
              </span>

              <strong className="details-inline">
                <GitBranch size={14} />

                {
                  environment.branchName
                }
              </strong>
            </div>
          </div>
        </div>

        {environment.failureReason && (
          <div className="glass-panel details-card details-failure-card">
            <div className="details-card-header">
              <CircleX size={18} />

              <h3>
                Failure Information
              </h3>
            </div>

            <p>
              {
                environment.failureReason
              }
            </p>
          </div>
        )}
      </div>

      <div className="details-activity-section">
        <div className="details-section-heading">
          <div>
            <h2>
              Activity Timeline
            </h2>

            <p>
              Track environment lifecycle events.
            </p>
          </div>

          <History size={21} />
        </div>

        <div className="glass-panel timeline-card">
          {activity.length === 0 ? (
            <div className="empty-state">
              <h3>
                No activity history
              </h3>

              <p>
                No lifecycle events are available
                for this environment.
              </p>
            </div>
          ) : (
            <div className="timeline">
              {activity.map(
                (item, index) => (
                  <div
                    className="timeline-item"
                    key={
                      item.activityId ||
                      `${item.activityType}-${index}`
                    }
                  >
                    <div className="timeline-marker">
                      <div className="timeline-dot" />
                    </div>

                    <div className="timeline-content">
                      <div className="timeline-top">
                        <strong>
                          {
                            item.activityType
                          }
                        </strong>

                        <span>
                          {item.createdAt
                            ? new Date(
                                item.createdAt
                              ).toLocaleString()
                            : ""}
                        </span>
                      </div>

                      {item.message && (
                        <p>
                          {
                            item.message
                          }
                        </p>
                      )}
                    </div>
                  </div>
                )
              )}
            </div>
          )}
        </div>
      </div>

      <ConfirmModal
        open={extendModalOpen}
        title="Extend Environment?"
        message="This environment will remain active for an additional 2 hours."
        confirmText="Extend 2 Hours"
        loading={actionLoading}
        onCancel={() =>
          setExtendModalOpen(false)
        }
        onConfirm={handleExtend}
      />

      <ConfirmModal
        open={deleteModalOpen}
        title="Delete Environment?"
        message={`This will start deletion of "${environment.applicationName}". This action cannot be undone.`}
        confirmText="Delete Environment"
        danger
        loading={actionLoading}
        onCancel={() =>
          setDeleteModalOpen(false)
        }
        onConfirm={handleDelete}
      />

      {toast && (
        <Toast
          type={toast.type}
          message={toast.message}
          onClose={() =>
            setToast(null)
          }
        />
      )}
    </>
  );
}

export default EnvironmentDetails;