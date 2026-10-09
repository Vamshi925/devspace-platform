import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  activateTemplate,
  createTemplate,
  deactivateTemplate,
  getTemplates,
} from "../api/templateApi";

import { useAuth } from "../context/AuthContext";

function AdminTemplates() {
  const navigate = useNavigate();
  const { user } = useAuth();

  const [templates, setTemplates] = useState([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

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

  const [form, setForm] = useState(initialForm);

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
      await createTemplate(form);

      setForm(initialForm);
      setShowForm(false);

      await loadTemplates();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to create template"
      );
    }
  };

  const handleActivate = async (templateId) => {
    try {
      await activateTemplate(templateId);
      await loadTemplates();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to activate template"
      );
    }
  };

  const handleDeactivate = async (templateId) => {
    try {
      await deactivateTemplate(templateId);
      await loadTemplates();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to deactivate template"
      );
    }
  };

  if (user?.role !== "ROLE_ADMIN") {
    return <p>Access denied</p>;
  }

  if (loading) {
    return <p>Loading templates...</p>;
  }

  return (
    <div>
      <button onClick={() => navigate("/dashboard")}>
        Dashboard
      </button>

      <h1>Admin - Template Management</h1>

      {error && <p>{error}</p>}

      <button onClick={() => setShowForm(!showForm)}>
        {showForm ? "Cancel" : "Create Template"}
      </button>

      {showForm && (
        <form onSubmit={handleCreate}>
          <h2>Create Template</h2>

          <div>
            <label>Name</label>
            <br />

            <input
              type="text"
              name="name"
              value={form.name}
              onChange={handleChange}
              required
            />
          </div>

          <br />

          <div>
            <label>Description</label>
            <br />

            <input
              type="text"
              name="description"
              value={form.description}
              onChange={handleChange}
            />
          </div>

          <br />

          <div>
            <label>Runtime Language</label>
            <br />

            <select
              name="runtimeLanguage"
              value={form.runtimeLanguage}
              onChange={handleChange}
              required
            >
              <option value="">
                Select Runtime Language
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

          <br />

          <div>
            <label>Runtime Version</label>
            <br />

            <input
              type="text"
              name="runtimeVersion"
              value={form.runtimeVersion}
              onChange={handleChange}
              placeholder="21"
              required
            />
          </div>

          <br />

          <div>
            <label>Database Type</label>
            <br />

            <select
              name="databaseType"
              value={form.databaseType}
              onChange={handleChange}
              required
            >
              <option value="">
                Select Database Type
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

          <br />

          <div>
            <label>Container Image</label>
            <br />

            <input
              type="text"
              name="containerImage"
              value={form.containerImage}
              onChange={handleChange}
              placeholder="nginx:alpine"
              required
            />
          </div>

          <br />

          <div>
            <label>Application Port</label>
            <br />

            <input
              type="number"
              name="applicationPort"
              value={form.applicationPort}
              onChange={handleChange}
              required
            />
          </div>

          <br />

          <div>
            <label>CPU Request</label>
            <br />

            <input
              type="text"
              name="cpuRequest"
              value={form.cpuRequest}
              onChange={handleChange}
              required
            />
          </div>

          <br />

          <div>
            <label>CPU Limit</label>
            <br />

            <input
              type="text"
              name="cpuLimit"
              value={form.cpuLimit}
              onChange={handleChange}
              required
            />
          </div>

          <br />

          <div>
            <label>Memory Request</label>
            <br />

            <input
              type="text"
              name="memoryRequest"
              value={form.memoryRequest}
              onChange={handleChange}
              required
            />
          </div>

          <br />

          <div>
            <label>Memory Limit</label>
            <br />

            <input
              type="text"
              name="memoryLimit"
              value={form.memoryLimit}
              onChange={handleChange}
              required
            />
          </div>

          <br />

          <div>
            <label>
              <input
                type="checkbox"
                name="redisEnabled"
                checked={form.redisEnabled}
                onChange={handleChange}
              />

              {" "}Redis Enabled
            </label>
          </div>

          <br />

          <div>
            <label>
              <input
                type="checkbox"
                name="kafkaEnabled"
                checked={form.kafkaEnabled}
                onChange={handleChange}
              />

              {" "}Kafka Enabled
            </label>
          </div>

          <br />

          <button type="submit">
            Create
          </button>
        </form>
      )}

      <hr />

      {templates.map((template) => (
        <div
          key={template.templateId}
          style={{
            border: "1px solid #ccc",
            padding: "15px",
            marginBottom: "15px",
          }}
        >
          <h3>{template.name}</h3>

          <p>
            <strong>ID:</strong>{" "}
            {template.templateId}
          </p>

          <p>
            <strong>Status:</strong>{" "}
            {template.active ? "Active" : "Inactive"}
          </p>

          <p>
            <strong>Description:</strong>{" "}
            {template.description || "-"}
          </p>

          <p>
            <strong>Runtime:</strong>{" "}
            {template.runtimeLanguage}{" "}
            {template.runtimeVersion}
          </p>

          <p>
            <strong>Database:</strong>{" "}
            {template.databaseType}
          </p>

          <p>
            <strong>Image:</strong>{" "}
            {template.containerImage}
          </p>

          <p>
            <strong>Port:</strong>{" "}
            {template.applicationPort}
          </p>

          <p>
            <strong>CPU:</strong>{" "}
            {template.cpuRequest} → {template.cpuLimit}
          </p>

          <p>
            <strong>Memory:</strong>{" "}
            {template.memoryRequest} → {template.memoryLimit}
          </p>

          <p>
            <strong>Redis:</strong>{" "}
            {template.redisEnabled ? "Enabled" : "Disabled"}
          </p>

          <p>
            <strong>Kafka:</strong>{" "}
            {template.kafkaEnabled ? "Enabled" : "Disabled"}
          </p>

          {template.active ? (
            <button
              onClick={() =>
                handleDeactivate(template.templateId)
              }
            >
              Deactivate
            </button>
          ) : (
            <button
              onClick={() =>
                handleActivate(template.templateId)
              }
            >
              Activate
            </button>
          )}
        </div>
      ))}
    </div>
  );
}

export default AdminTemplates;