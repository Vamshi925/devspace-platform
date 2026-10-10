import { useEffect, useState } from "react";

import {
  Boxes,
  Cpu,
  Database,
  Layers3,
  MemoryStick,
  Plus,
  Power,
  PowerOff,
  ServerCog,
  X,
} from "lucide-react";

import {
  activateTemplate,
  createTemplate,
  deactivateTemplate,
  getTemplates,
} from "../api/templateApi";

import { useAuth } from "../context/AuthContext";

import ConfirmModal from "../components/ConfirmModal";
import Toast from "../components/Toast";
import PageLoader from "../components/PageLoader";
import TemplatesSkeleton from "../components/TemplatesSkeleton";
import AccessDenied from "./AccessDenied";

function AdminTemplates() {
  const { user } = useAuth();

  const initialForm = {
    name: "",
    description: "",
    runtimeLanguage: "",
    runtimeVersion: "",
    databaseType: "",
    containerImage: "",
    applicationPort: 8080,
    cpuRequest: "100m",
    cpuLimit: "500m",
    memoryRequest: "128Mi",
    memoryLimit: "512Mi",
    redisEnabled: false,
    kafkaEnabled: false,
  };

  const [templates, setTemplates] = useState([]);
  const [form, setForm] = useState(initialForm);

  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);

  const [error, setError] = useState("");

  const [toast, setToast] = useState(null);

  const [
    selectedTemplate,
    setSelectedTemplate,
  ] = useState(null);

  const [
    confirmationAction,
    setConfirmationAction,
  ] = useState(null);

  const loadTemplates = async () => {
    try {
      setLoading(true);

      const data = await getTemplates();

      setTemplates(data);
      setError("");
    } catch (error) {
      setError(
        error.response?.data?.message ||
          "Unable to load templates"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.role === "ROLE_ADMIN") {
      loadTemplates();
    } else {
      setLoading(false);
    }
  }, [user]);

  const showToast = (type, message) => {
    setToast({
      type,
      message,
    });

    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  const handleChange = (event) => {
    const {
      name,
      value,
      type,
      checked,
    } = event.target;

    setForm((current) => ({
      ...current,

      [name]:
        type === "checkbox"
          ? checked
          : name === "applicationPort"
            ? Number(value)
            : value,
    }));
  };

  const handleCreate = async (event) => {
    event.preventDefault();

    try {
      setSubmitting(true);

      await createTemplate(form);

      setForm(initialForm);
      setShowForm(false);

      showToast(
        "success",
        "Template created successfully."
      );

      await loadTemplates();
    } catch (error) {
      showToast(
        "error",
        error.response?.data?.message ||
          "Unable to create template"
      );
    } finally {
      setSubmitting(false);
    }
  };

  const requestActivate = (template) => {
    setSelectedTemplate(template);
    setConfirmationAction("ACTIVATE");
  };

  const requestDeactivate = (template) => {
    setSelectedTemplate(template);
    setConfirmationAction("DEACTIVATE");
  };

  const closeConfirmation = () => {
    if (actionLoading) {
      return;
    }

    setSelectedTemplate(null);
    setConfirmationAction(null);
  };

  const handleConfirmAction = async () => {
    if (!selectedTemplate) {
      return;
    }

    try {
      setActionLoading(true);

      if (
        confirmationAction === "ACTIVATE"
      ) {
        await activateTemplate(
          selectedTemplate.templateId
        );

        showToast(
          "success",
          `"${selectedTemplate.name}" activated successfully.`
        );
      }

      if (
        confirmationAction ===
        "DEACTIVATE"
      ) {
        await deactivateTemplate(
          selectedTemplate.templateId
        );

        showToast(
          "success",
          `"${selectedTemplate.name}" deactivated successfully.`
        );
      }

      setSelectedTemplate(null);
      setConfirmationAction(null);

      await loadTemplates();
    } catch (error) {
      showToast(
        "error",
        error.response?.data?.message ||
          "Unable to update template"
      );
    } finally {
      setActionLoading(false);
    }
  };

  if (user?.role !== "ROLE_ADMIN") {
  return <AccessDenied />;
}

  if (loading) {
  return <TemplatesSkeleton />;
}

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            Manage Templates
          </h1>

          <p className="page-subtitle">
            Create and manage approved developer
            environment templates.
          </p>
        </div>

        <button
          className="btn btn-primary"
          onClick={() =>
            setShowForm(
              (current) => !current
            )
          }
        >
          {showForm ? (
            <>
              <X size={17} />
              Close Form
            </>
          ) : (
            <>
              <Plus size={17} />
              Create Template
            </>
          )}
        </button>
      </div>

      {error && (
        <div className="glass-panel environment-error">
          {error}
        </div>
      )}

      {showForm && (
        <form
          className="glass-panel admin-template-form"
          onSubmit={handleCreate}
        >
          <div className="admin-template-form-header">
            <div className="create-section-icon">
              <Layers3 size={20} />
            </div>

            <div>
              <h2>
                Create Template
              </h2>

              <p>
                Define the runtime, database and
                resource configuration for this
                template.
              </p>
            </div>
          </div>

          <div className="admin-template-form-grid">
            <div className="form-group">
              <label className="form-label">
                Name
              </label>

              <input
                className="form-input"
                type="text"
                name="name"
                value={form.name}
                onChange={handleChange}
                placeholder="spring-postgres"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Runtime Language
              </label>

              <select
                className="form-input"
                name="runtimeLanguage"
                value={form.runtimeLanguage}
                onChange={handleChange}
                required
              >
                <option value="">
                  Select Runtime
                </option>

                <option value="JAVA">
                  Java
                </option>

                <option value="PYTHON">
                  Python
                </option>

                <option value="NODEJS">
                  Node.js
                </option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">
                Runtime Version
              </label>

              <input
                className="form-input"
                type="text"
                name="runtimeVersion"
                value={form.runtimeVersion}
                onChange={handleChange}
                placeholder="21"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Database Type
              </label>

              <select
                className="form-input"
                name="databaseType"
                value={form.databaseType}
                onChange={handleChange}
                required
              >
                <option value="">
                  Select Database
                </option>

                <option value="POSTGRESQL">
                  PostgreSQL
                </option>

                <option value="MYSQL">
                  MySQL
                </option>

                <option value="NONE">
                  None
                </option>
              </select>
            </div>

            <div className="form-group admin-template-full">
              <label className="form-label">
                Description
              </label>

              <textarea
                className="form-input admin-template-textarea"
                name="description"
                value={form.description}
                onChange={handleChange}
                placeholder="Describe when this template should be used..."
              />
            </div>

            <div className="form-group admin-template-full">
              <label className="form-label">
                Container Image
              </label>

              <input
                className="form-input"
                type="text"
                name="containerImage"
                value={form.containerImage}
                onChange={handleChange}
                placeholder="nginx:alpine"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Application Port
              </label>

              <input
                className="form-input"
                type="number"
                name="applicationPort"
                value={form.applicationPort}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                CPU Request
              </label>

              <input
                className="form-input"
                type="text"
                name="cpuRequest"
                value={form.cpuRequest}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                CPU Limit
              </label>

              <input
                className="form-input"
                type="text"
                name="cpuLimit"
                value={form.cpuLimit}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Memory Request
              </label>

              <input
                className="form-input"
                type="text"
                name="memoryRequest"
                value={form.memoryRequest}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                Memory Limit
              </label>

              <input
                className="form-input"
                type="text"
                name="memoryLimit"
                value={form.memoryLimit}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="admin-feature-options">
            <label className="admin-feature-option">
              <input
                type="checkbox"
                name="redisEnabled"
                checked={form.redisEnabled}
                onChange={handleChange}
              />

              <span>
                Redis Enabled
              </span>
            </label>

            <label className="admin-feature-option">
              <input
                type="checkbox"
                name="kafkaEnabled"
                checked={form.kafkaEnabled}
                onChange={handleChange}
              />

              <span>
                Kafka Enabled
              </span>
            </label>
          </div>

          <div className="admin-template-submit">
            <button
              className="btn btn-primary"
              type="submit"
              disabled={submitting}
            >
              {submitting ? (
                <>
                  <span className="button-spinner" />
                  Creating...
                </>
              ) : (
                <>
                  <Plus size={16} />
                  Create Template
                </>
              )}
            </button>
          </div>
        </form>
      )}

      <div className="admin-template-summary">
        <span>
          {templates.length} templates
        </span>

        <span>
          {
            templates.filter(
              (template) =>
                template.active
            ).length
          }{" "}
          active
        </span>
      </div>

      <div className="templates-grid">
        {templates.map((template) => (
          <div
            className={`template-card ${
              !template.active
                ? "admin-template-disabled"
                : ""
            }`}
            key={template.templateId}
          >
            <div className="template-card-top">
              <div className="template-icon">
                <Layers3 size={22} />
              </div>

              <span
                className={
                  template.active
                    ? "template-status template-active"
                    : "template-status template-inactive"
                }
              >
                {template.active
                  ? "ACTIVE"
                  : "INACTIVE"}
              </span>
            </div>

            <h3>
              {template.name}
            </h3>

            <p className="template-description">
              {template.description ||
                "DevSpace environment template"}
            </p>

            <div className="template-stack">
              <div className="template-stack-item">
                <ServerCog size={15} />

                <div>
                  <span>
                    Runtime
                  </span>

                  <strong>
                    {template.runtimeLanguage}{" "}
                    {template.runtimeVersion}
                  </strong>
                </div>
              </div>

              <div className="template-stack-item">
                <Database size={15} />

                <div>
                  <span>
                    Database
                  </span>

                  <strong>
                    {template.databaseType}
                  </strong>
                </div>
              </div>

              <div className="template-stack-item">
                <Cpu size={15} />

                <div>
                  <span>
                    CPU
                  </span>

                  <strong>
                    {template.cpuRequest} →{" "}
                    {template.cpuLimit}
                  </strong>
                </div>
              </div>

              <div className="template-stack-item">
                <MemoryStick size={15} />

                <div>
                  <span>
                    Memory
                  </span>

                  <strong>
                    {template.memoryRequest} →{" "}
                    {template.memoryLimit}
                  </strong>
                </div>
              </div>
            </div>

            <div className="template-features">
              <span
                className={
                  template.redisEnabled
                    ? "feature-pill feature-enabled"
                    : "feature-pill"
                }
              >
                Redis
              </span>

              <span
                className={
                  template.kafkaEnabled
                    ? "feature-pill feature-enabled"
                    : "feature-pill"
                }
              >
                Kafka
              </span>

              <span className="feature-pill feature-enabled">
                Port{" "}
                {
                  template.applicationPort
                }
              </span>
            </div>

            <div className="admin-template-image">
              <Boxes size={14} />

              {
                template.containerImage
              }
            </div>

            <div className="admin-template-card-footer">
              {template.active ? (
                <button
                  className="btn btn-danger"
                  onClick={() =>
                    requestDeactivate(
                      template
                    )
                  }
                >
                  <PowerOff size={15} />

                  Deactivate
                </button>
              ) : (
                <button
                  className="btn btn-success"
                  onClick={() =>
                    requestActivate(
                      template
                    )
                  }
                >
                  <Power size={15} />

                  Activate
                </button>
              )}
            </div>
          </div>
        ))}
      </div>

      <ConfirmModal
        open={
          confirmationAction ===
            "ACTIVATE" &&
          selectedTemplate
        }
        title="Activate Template?"
        message={
          selectedTemplate
            ? `"${selectedTemplate.name}" will become available for developers when creating environments.`
            : ""
        }
        confirmText="Activate Template"
        loading={actionLoading}
        onCancel={closeConfirmation}
        onConfirm={handleConfirmAction}
      />

      <ConfirmModal
        open={
          confirmationAction ===
            "DEACTIVATE" &&
          selectedTemplate
        }
        title="Deactivate Template?"
        message={
          selectedTemplate
            ? `"${selectedTemplate.name}" will no longer be available for new developer environments. Existing environments are not affected.`
            : ""
        }
        confirmText="Deactivate Template"
        danger
        loading={actionLoading}
        onCancel={closeConfirmation}
        onConfirm={handleConfirmAction}
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

export default AdminTemplates;