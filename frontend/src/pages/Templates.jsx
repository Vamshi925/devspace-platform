import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Boxes,
  Cpu,
  Database,
  Layers3,
  MemoryStick,
  Play,
  Search,
  ServerCog,
  X,
} from "lucide-react";
import { getTemplates } from "../api/templateApi";
import TemplatesSkeleton from "../components/TemplatesSkeleton";
import { getErrorMessage } from "../utils/getErrorMessage";

function Templates() {
  const navigate = useNavigate();
  const [templates, setTemplates] = useState([]);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadTemplates = async () => {
      try {
        const data = await getTemplates();
        setTemplates(data || []);
        setError("");
      } catch (error) {
        setError(
          getErrorMessage(error, "Unable to load templates")
        );
      } finally {
        setLoading(false);
      }
    };

    loadTemplates();
  }, []);

  const filteredTemplates = useMemo(() => {
    const query = search.trim().toLowerCase();

    return templates.filter((template) => {
      const matchesStatus =
        statusFilter === "ALL" ||
        (statusFilter === "ACTIVE" && template.active) ||
        (statusFilter === "INACTIVE" && !template.active);

      const searchable = [
        template.name,
        template.description,
        template.runtimeLanguage,
        template.runtimeVersion,
        template.databaseType,
        template.containerImage,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return matchesStatus && (!query || searchable.includes(query));
    });
  }, [templates, search, statusFilter]);

  const clearFilters = () => {
    setSearch("");
    setStatusFilter("ALL");
  };

  const hasFilters = Boolean(search) || statusFilter !== "ALL";
  const activeCount = templates.filter((template) => template.active).length;

  if (loading) return <TemplatesSkeleton />;

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">Environment Templates</h1>
          <p className="page-subtitle">
            Choose from approved platform templates to launch standardized environments.
          </p>
        </div>
      </div>

      {error && (
        <div className="glass-panel environment-error">{error}</div>
      )}

      <div className="template-browser-toolbar">
        <div className="template-search">
          <Search size={17} />
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search name, runtime, database..."
          />
          {search && (
            <button
              onClick={() => setSearch("")}
              aria-label="Clear search"
            >
              <X size={15} />
            </button>
          )}
        </div>

        <div className="template-filter-group">
          {["ALL", "ACTIVE", "INACTIVE"].map((status) => (
            <button
              key={status}
              className={`template-filter-button ${statusFilter === status ? "active" : ""}`}
              onClick={() => setStatusFilter(status)}
            >
              {status === "ALL" ? "All" : status === "ACTIVE" ? "Active" : "Inactive"}
            </button>
          ))}
        </div>
      </div>

      <div className="template-browser-summary">
        <span>
          Showing <strong>{filteredTemplates.length}</strong> of {templates.length} templates
        </span>
        <span>{activeCount} active</span>
        {hasFilters && (
          <button className="template-clear-filter" onClick={clearFilters}>
            Clear filters
          </button>
        )}
      </div>

      {filteredTemplates.length === 0 ? (
        <div className="glass-panel template-empty-state">
          <div className="template-empty-icon">
            <Layers3 size={27} />
          </div>
          <h3>No templates found</h3>
          <p>Try changing your search term or template status filter.</p>
          {hasFilters && (
            <button className="btn btn-ghost" onClick={clearFilters}>
              Clear Filters
            </button>
          )}
        </div>
      ) : (
        <div className="templates-grid">
          {filteredTemplates.map((template) => (
            <div className="template-card" key={template.templateId}>
              <div className="template-card-top">
                <div className="template-icon">
                  <Layers3 size={22} />
                </div>
                <span className={`template-status ${template.active ? "template-active" : "template-inactive"}`}>
                  {template.active ? "ACTIVE" : "INACTIVE"}
                </span>
              </div>

              <h3>{template.name}</h3>
              <p className="template-description">
                {template.description || "Approved DevSpace environment template"}
              </p>

              <div className="template-stack">
                <div className="template-stack-item">
                  <ServerCog size={15} />
                  <div>
                    <span>Runtime</span>
                    <strong>{template.runtimeLanguage} {template.runtimeVersion}</strong>
                  </div>
                </div>
                <div className="template-stack-item">
                  <Database size={15} />
                  <div>
                    <span>Database</span>
                    <strong>{template.databaseType}</strong>
                  </div>
                </div>
                <div className="template-stack-item">
                  <Cpu size={15} />
                  <div>
                    <span>CPU</span>
                    <strong>{template.cpuRequest} → {template.cpuLimit}</strong>
                  </div>
                </div>
                <div className="template-stack-item">
                  <MemoryStick size={15} />
                  <div>
                    <span>Memory</span>
                    <strong>{template.memoryRequest} → {template.memoryLimit}</strong>
                  </div>
                </div>
              </div>

              <div className="template-features">
                <span className={`feature-pill ${template.redisEnabled ? "feature-enabled" : ""}`}>Redis</span>
                <span className={`feature-pill ${template.kafkaEnabled ? "feature-enabled" : ""}`}>Kafka</span>
                <span className="feature-pill feature-enabled">Port {template.applicationPort}</span>
              </div>

              <div className="template-footer">
                <div className="template-image">
                  <Boxes size={14} />
                  <span>{template.containerImage}</span>
                </div>
                <button
                  className="btn btn-primary"
                  disabled={!template.active}
                  onClick={() =>
                    navigate(`/environments/create?templateId=${template.templateId}`)
                  }
                >
                  <Play size={15} />
                  Use Template
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}

export default Templates;
