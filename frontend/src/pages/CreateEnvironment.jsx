import { useEffect, useState } from "react";
import { createEnvironment } from "../api/environmentApi";
import { getTemplates } from "../api/templateApi";
import { useNavigate, useSearchParams } from "react-router-dom";
function CreateEnvironment() {
  const navigate = useNavigate();

  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const [searchParams] = useSearchParams();
  
  const selectedTemplateId = searchParams.get("templateId");

  const [form, setForm] = useState({
    applicationName: "",
    templateId: "",
    environmentType: "DEVELOPMENT",
    lifetimeHours: 2,
    repositoryUrl: "",
    branchName: "main",
  });

  useEffect(() => {
    const loadTemplates = async () => {
      try {
        const data = await getTemplates();

        const activeTemplates = data.filter(
          (template) => template.active
        );

        setTemplates(activeTemplates);

        if (activeTemplates.length > 0) {
  const selectedTemplate = activeTemplates.find(
    (template) =>
      template.templateId === selectedTemplateId
  );

  setForm((current) => ({
    ...current,
    templateId:
      selectedTemplate?.templateId ||
      activeTemplates[0].templateId,
  }));
}
      } catch {
        setError("Unable to load templates");
      } finally {
        setLoading(false);
      }
    };

    loadTemplates();
  }, [selectedTemplateId]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]:
        name === "lifetimeHours"
          ? Number(value)
          : value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError("");

    try {
      const environment =
        await createEnvironment(form);

      navigate(
        `/environments/${environment.environmentId}`
      );
    } catch (error) {
      setError(
        error.response?.data?.message ||
          "Unable to create environment"
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <p>Loading templates...</p>;
  }

  return (
    <div>
      <button
        onClick={() =>
          navigate("/environments")
        }
      >
        Back
      </button>

      <h1>Create Environment</h1>

      {error && <p>{error}</p>}

      <form onSubmit={handleSubmit}>
        <div>
          <label>
            Application Name
          </label>

          <br />

          <input
            type="text"
            name="applicationName"
            value={form.applicationName}
            onChange={handleChange}
            placeholder="payment-service"
            required
          />
        </div>

        <br />

        <div>
          <label>
            Template
          </label>

          <br />

          <select
            name="templateId"
            value={form.templateId}
            onChange={handleChange}
            required
          >
            {templates.map((template) => (
              <option
                key={template.templateId}
                value={template.templateId}
              >
                {template.name}
              </option>
            ))}
          </select>
        </div>

        <br />

        <div>
          <label>
            Environment Type
          </label>

          <br />

          <select
            name="environmentType"
            value={form.environmentType}
            onChange={handleChange}
          >
            <option value="DEVELOPMENT">
              Development
            </option>

            <option value="TEST">
              Test
            </option>
          </select>
        </div>

        <br />

        <div>
          <label>
            Lifetime
          </label>

          <br />

          <select
            name="lifetimeHours"
            value={form.lifetimeHours}
            onChange={handleChange}
          >
            <option value={2}>
              2 Hours
            </option>

            <option value={4}>
              4 Hours
            </option>

            <option value={8}>
              8 Hours
            </option>

            <option value={24}>
              24 Hours
            </option>
          </select>
        </div>

        <br />

        <div>
          <label>
            Repository URL
          </label>

          <br />

          <input
            type="url"
            name="repositoryUrl"
            value={form.repositoryUrl}
            onChange={handleChange}
            placeholder="https://github.com/user/repository"
            required
          />
        </div>

        <br />

        <div>
          <label>
            Branch
          </label>

          <br />

          <input
            type="text"
            name="branchName"
            value={form.branchName}
            onChange={handleChange}
            placeholder="main"
            required
          />
        </div>

        <br />

        <button
          type="submit"
          disabled={
            submitting ||
            templates.length === 0
          }
        >
          {submitting
            ? "Creating..."
            : "Create Environment"}
        </button>
      </form>

      {templates.length === 0 && (
        <p>
          No active templates available.
        </p>
      )}
    </div>
  );
}

export default CreateEnvironment;
