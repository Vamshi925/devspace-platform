import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getTemplates } from "../api/templateApi";

function Templates() {
  const navigate = useNavigate();

  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadTemplates = async () => {
      try {
        const data = await getTemplates();
        setTemplates(data);
      } catch (error) {
        setError(
          error.response?.data?.message ||
            "Unable to load templates"
        );
      } finally {
        setLoading(false);
      }
    };

    loadTemplates();
  }, []);

  if (loading) {
    return <p>Loading templates...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div>
      <button onClick={() => navigate("/dashboard")}>
        Dashboard
      </button>

      <h1>Environment Templates</h1>

      <p>
        Select an approved template when creating a new
        development environment.
      </p>

      {templates.length === 0 ? (
        <p>No templates available.</p>
      ) : (
        templates.map((template) => (
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
              <strong>Status:</strong>{" "}
              {template.active ? "Active" : "Inactive"}
            </p>

            {template.description && (
              <p>
                <strong>Description:</strong>{" "}
                {template.description}
              </p>
            )}

            {template.containerImage && (
              <p>
                <strong>Container Image:</strong>{" "}
                {template.containerImage}
              </p>
            )}

            {template.applicationPort && (
              <p>
                <strong>Application Port:</strong>{" "}
                {template.applicationPort}
              </p>
            )}

            {template.cpuRequest && (
              <p>
                <strong>CPU:</strong>{" "}
                {template.cpuRequest}
                {" → "}
                {template.cpuLimit}
              </p>
            )}

            {template.memoryRequest && (
              <p>
                <strong>Memory:</strong>{" "}
                {template.memoryRequest}
                {" → "}
                {template.memoryLimit}
              </p>
            )}

            {template.active && (
              <button
                onClick={() =>
                  navigate(
                    `/environments/create?templateId=${template.templateId}`
                  )
                }
              >
                Use Template
              </button>
            )}
          </div>
        ))
      )}
    </div>
  );
}

export default Templates;
