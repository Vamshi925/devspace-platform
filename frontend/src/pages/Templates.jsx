import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  Boxes,
  Cpu,
  Database,
  Layers3,
  MemoryStick,
  Play,
  ServerCog,
} from "lucide-react";

import { getTemplates } from "../api/templateApi";
import PageLoader from "../components/PageLoader";
import TemplatesSkeleton from "../components/TemplatesSkeleton";

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
  return <TemplatesSkeleton />;
}

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            Environment Templates
          </h1>

          <p className="page-subtitle">
            Choose from approved platform templates
            to launch standardized environments.
          </p>
        </div>
      </div>

      {error && (
        <div className="glass-panel environment-error">
          {error}
        </div>
      )}

      <div className="templates-grid">
        {templates.map((template) => (
          <div
            className="template-card"
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

            <h3>{template.name}</h3>

            <p className="template-description">
              {template.description ||
                "Approved DevSpace environment template"}
            </p>

            <div className="template-stack">
              <div className="template-stack-item">
                <ServerCog size={15} />

                <div>
                  <span>Runtime</span>
                  <strong>
                    {template.runtimeLanguage}{" "}
                    {template.runtimeVersion}
                  </strong>
                </div>
              </div>

              <div className="template-stack-item">
                <Database size={15} />

                <div>
                  <span>Database</span>
                  <strong>
                    {template.databaseType}
                  </strong>
                </div>
              </div>

              <div className="template-stack-item">
                <Cpu size={15} />

                <div>
                  <span>CPU</span>
                  <strong>
                    {template.cpuRequest} →{" "}
                    {template.cpuLimit}
                  </strong>
                </div>
              </div>

              <div className="template-stack-item">
                <MemoryStick size={15} />

                <div>
                  <span>Memory</span>
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
                Port {template.applicationPort}
              </span>
            </div>

            <div className="template-footer">
              <div className="template-image">
                <Boxes size={14} />
                <span>
                  {template.containerImage}
                </span>
              </div>

              <button
                className="btn btn-primary"
                disabled={!template.active}
                onClick={() =>
                  navigate(
                    `/environments/create?templateId=${template.templateId}`
                  )
                }
              >
                <Play size={15} />
                Use Template
              </button>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}

export default Templates;