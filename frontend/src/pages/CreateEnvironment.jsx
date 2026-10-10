import {
  useEffect,
  useMemo,
  useState,
} from "react";

import {
  useNavigate,
  useSearchParams,
} from "react-router-dom";

import {
  ArrowLeft,
  Boxes,
  Clock3,
  Code2,
  GitBranch,
  Layers3,
  Rocket,
  Server,
} from "lucide-react";

import {
  createEnvironment,
} from "../api/environmentApi";

import {
  getTemplates,
} from "../api/templateApi";

import PageLoader from "../components/PageLoader";

function CreateEnvironment() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  const selectedTemplateId =
    searchParams.get("templateId");

  const [templates, setTemplates] =
    useState([]);

  const [
    loadingTemplates,
    setLoadingTemplates,
  ] = useState(true);

  const [submitting, setSubmitting] =
    useState(false);

  const [error, setError] =
    useState("");

  const [
    fieldErrors,
    setFieldErrors,
  ] = useState({});

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
        const data =
          await getTemplates();

        const activeTemplates =
          data.filter(
            (template) =>
              template.active
          );

        setTemplates(
          activeTemplates
        );

        const selectedTemplate =
          activeTemplates.find(
            (template) =>
              template.templateId ===
              selectedTemplateId
          );

        setForm((current) => ({
          ...current,

          templateId:
            selectedTemplate
              ?.templateId ||
            activeTemplates[0]
              ?.templateId ||
            "",
        }));
      } catch {
        setError(
          "Unable to load templates"
        );
      } finally {
        setLoadingTemplates(
          false
        );
      }
    };

    loadTemplates();
  }, [selectedTemplateId]);

  const selectedTemplate =
    useMemo(
      () =>
        templates.find(
          (template) =>
            template.templateId ===
            form.templateId
        ),
      [
        templates,
        form.templateId,
      ]
    );

  const validateField = (
    name,
    value
  ) => {
    switch (name) {
      case "applicationName": {
        const trimmed =
          value.trim();

        if (!trimmed) {
          return "Application name is required";
        }

        if (
          trimmed.length < 3
        ) {
          return "Application name must contain at least 3 characters";
        }

        if (
          trimmed.length > 50
        ) {
          return "Application name cannot exceed 50 characters";
        }

        if (
          !/^[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/.test(
            trimmed
          )
        ) {
          return "Use lowercase letters, numbers and hyphens only";
        }

        return "";
      }

      case "templateId":
        return value
          ? ""
          : "Please select a template";

      case "repositoryUrl": {
        const trimmed =
          value.trim();

        if (!trimmed) {
          return "Repository URL is required";
        }

        try {
          const url =
            new URL(trimmed);

          if (
            url.protocol !==
              "https:" ||
            url.hostname !==
              "github.com"
          ) {
            return "Enter a valid GitHub HTTPS repository URL";
          }

          const parts =
            url.pathname
              .split("/")
              .filter(Boolean);

          if (
            parts.length < 2
          ) {
            return "GitHub URL must contain owner and repository";
          }

          return "";
        } catch {
          return "Enter a valid repository URL";
        }
      }

      case "branchName": {
        const trimmed =
          value.trim();

        if (!trimmed) {
          return "Branch name is required";
        }

        if (
          !/^[A-Za-z0-9._/-]+$/.test(
            trimmed
          )
        ) {
          return "Branch contains unsupported characters";
        }

        return "";
      }

      case "lifetimeHours":
        return [
          2,
          4,
          8,
          24,
        ].includes(
          Number(value)
        )
          ? ""
          : "Select a valid lifetime";

      default:
        return "";
    }
  };

  const validateForm = () => {
    const errors = {
      applicationName:
        validateField(
          "applicationName",
          form.applicationName
        ),

      templateId:
        validateField(
          "templateId",
          form.templateId
        ),

      repositoryUrl:
        validateField(
          "repositoryUrl",
          form.repositoryUrl
        ),

      branchName:
        validateField(
          "branchName",
          form.branchName
        ),

      lifetimeHours:
        validateField(
          "lifetimeHours",
          form.lifetimeHours
        ),
    };

    Object.keys(
      errors
    ).forEach((key) => {
      if (!errors[key]) {
        delete errors[key];
      }
    });

    setFieldErrors(
      errors
    );

    return (
      Object.keys(errors)
        .length === 0
    );
  };

  const handleChange = (
    event
  ) => {
    const {
      name,
      value,
    } = event.target;

    const parsedValue =
      name ===
      "lifetimeHours"
        ? Number(value)
        : value;

    setForm(
      (current) => ({
        ...current,
        [name]:
          parsedValue,
      })
    );

    if (
      fieldErrors[name]
    ) {
      setFieldErrors(
        (current) => ({
          ...current,

          [name]:
            validateField(
              name,
              parsedValue
            ),
        })
      );
    }

    if (error) {
      setError("");
    }
  };

  const validateOnBlur = (
    name
  ) => {
    setFieldErrors(
      (current) => ({
        ...current,

        [name]:
          validateField(
            name,
            form[name]
          ),
      })
    );
  };

  const handleSubmit = async (
    event
  ) => {
    event.preventDefault();

    setError("");

    if (!validateForm()) {
      return;
    }

    try {
      setSubmitting(true);

      const environment =
        await createEnvironment({
          ...form,

          applicationName:
            form.applicationName
              .trim(),

          repositoryUrl:
            form.repositoryUrl
              .trim(),

          branchName:
            form.branchName
              .trim(),
        });

      navigate(
        `/environments/${environment.environmentId}`
      );
    } catch (error) {
      setError(
        error.response?.data
          ?.message ||
          "Unable to create environment"
      );
    } finally {
      setSubmitting(false);
    }
  };

  if (
    loadingTemplates
  ) {
    return (
      <PageLoader
        title="Loading templates..."
        subtitle="Fetching approved DevSpace environment templates"
      />
    );
  }

  return (
    <>
      <button
        className="btn btn-ghost create-back"
        onClick={() =>
          navigate(
            "/environments"
          )
        }
      >
        <ArrowLeft size={16} />

        Back to Environments
      </button>

      <div className="page-header">
        <div>
          <h1 className="page-title">
            Create Environment
          </h1>

          <p className="page-subtitle">
            Launch an isolated temporary
            developer environment using an
            approved DevSpace template.
          </p>
        </div>
      </div>

      {error && (
        <div className="glass-panel create-error">
          {error}
        </div>
      )}

      <form
        onSubmit={
          handleSubmit
        }
        className="create-layout"
      >
        <div className="create-main">
          {/* APPLICATION DETAILS */}

          <div className="glass-panel create-section">
            <div className="create-section-header">
              <div className="create-section-icon">
                <Code2 size={19} />
              </div>

              <div>
                <h3>
                  Application Details
                </h3>

                <p>
                  Basic information about
                  your application.
                </p>
              </div>
            </div>

            <div className="create-form-grid">
              <div className="form-group create-full">
                <label className="form-label">
                  Application Name
                </label>

                <input
                  className={`form-input ${
                    fieldErrors.applicationName
                      ? "form-input-error"
                      : ""
                  }`}
                  type="text"
                  name="applicationName"
                  value={
                    form.applicationName
                  }
                  onChange={
                    handleChange
                  }
                  onBlur={() =>
                    validateOnBlur(
                      "applicationName"
                    )
                  }
                  placeholder="payment-service"
                />

                {fieldErrors.applicationName && (
                  <span className="field-error">
                    {
                      fieldErrors.applicationName
                    }
                  </span>
                )}

                <span className="field-helper">
                  Use lowercase letters,
                  numbers and hyphens.
                  Example:
                  payment-service
                </span>
              </div>
            </div>
          </div>

          {/* TEMPLATE */}

          <div className="glass-panel create-section">
            <div className="create-section-header">
              <div className="create-section-icon">
                <Layers3 size={19} />
              </div>

              <div>
                <h3>
                  Template Selection
                </h3>

                <p>
                  Choose an approved
                  runtime and
                  infrastructure
                  template.
                </p>
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Environment Template
              </label>

              <select
                className={`form-input ${
                  fieldErrors.templateId
                    ? "form-input-error"
                    : ""
                }`}
                name="templateId"
                value={
                  form.templateId
                }
                onChange={
                  handleChange
                }
                onBlur={() =>
                  validateOnBlur(
                    "templateId"
                  )
                }
              >
                {templates.length ===
                  0 && (
                  <option value="">
                    No active templates
                  </option>
                )}

                {templates.map(
                  (template) => (
                    <option
                      key={
                        template.templateId
                      }
                      value={
                        template.templateId
                      }
                    >
                      {
                        template.name
                      }
                    </option>
                  )
                )}
              </select>

              {fieldErrors.templateId && (
                <span className="field-error">
                  {
                    fieldErrors.templateId
                  }
                </span>
              )}
            </div>

            {selectedTemplate && (
              <div className="selected-template-preview">
                <div className="selected-template-top">
                  <strong>
                    {
                      selectedTemplate.name
                    }
                  </strong>

                  <span className="template-status template-active">
                    ACTIVE
                  </span>
                </div>

                <div className="selected-template-grid">
                  <div>
                    <span>
                      Runtime
                    </span>

                    <strong>
                      {
                        selectedTemplate.runtimeLanguage
                      }{" "}
                      {
                        selectedTemplate.runtimeVersion
                      }
                    </strong>
                  </div>

                  <div>
                    <span>
                      Database
                    </span>

                    <strong>
                      {
                        selectedTemplate.databaseType
                      }
                    </strong>
                  </div>

                  <div>
                    <span>
                      CPU
                    </span>

                    <strong>
                      {
                        selectedTemplate.cpuRequest
                      }{" "}
                      →{" "}
                      {
                        selectedTemplate.cpuLimit
                      }
                    </strong>
                  </div>

                  <div>
                    <span>
                      Memory
                    </span>

                    <strong>
                      {
                        selectedTemplate.memoryRequest
                      }{" "}
                      →{" "}
                      {
                        selectedTemplate.memoryLimit
                      }
                    </strong>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* REPOSITORY */}

          <div className="glass-panel create-section">
            <div className="create-section-header">
              <div className="create-section-icon">
                <GitBranch size={19} />
              </div>

              <div>
                <h3>
                  Repository Configuration
                </h3>

                <p>
                  DevSpace validates the
                  repository and branch
                  before provisioning.
                </p>
              </div>
            </div>

            <div className="create-form-grid">
              <div className="form-group create-full">
                <label className="form-label">
                  Repository URL
                </label>

                <input
                  className={`form-input ${
                    fieldErrors.repositoryUrl
                      ? "form-input-error"
                      : ""
                  }`}
                  type="url"
                  name="repositoryUrl"
                  value={
                    form.repositoryUrl
                  }
                  onChange={
                    handleChange
                  }
                  onBlur={() =>
                    validateOnBlur(
                      "repositoryUrl"
                    )
                  }
                  placeholder="https://github.com/your-org/payment-service"
                />

                {fieldErrors.repositoryUrl && (
                  <span className="field-error">
                    {
                      fieldErrors.repositoryUrl
                    }
                  </span>
                )}

                <span className="field-helper">
                  Enter a GitHub HTTPS
                  repository URL.
                </span>
              </div>

              <div className="form-group">
                <label className="form-label">
                  Branch
                </label>

                <div className="create-input-icon">
                  <GitBranch
                    size={16}
                  />

                  <input
                    className={`form-input ${
                      fieldErrors.branchName
                        ? "form-input-error"
                        : ""
                    }`}
                    type="text"
                    name="branchName"
                    value={
                      form.branchName
                    }
                    onChange={
                      handleChange
                    }
                    onBlur={() =>
                      validateOnBlur(
                        "branchName"
                      )
                    }
                    placeholder="main"
                  />
                </div>

                {fieldErrors.branchName && (
                  <span className="field-error">
                    {
                      fieldErrors.branchName
                    }
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* ENVIRONMENT SETTINGS */}

          <div className="glass-panel create-section">
            <div className="create-section-header">
              <div className="create-section-icon">
                <Server size={19} />
              </div>

              <div>
                <h3>
                  Environment Settings
                </h3>

                <p>
                  Configure environment
                  type and automatic
                  expiration.
                </p>
              </div>
            </div>

            <div className="create-form-grid">
              <div className="form-group">
                <label className="form-label">
                  Environment Type
                </label>

                <select
                  className="form-input"
                  name="environmentType"
                  value={
                    form.environmentType
                  }
                  onChange={
                    handleChange
                  }
                >
                  <option value="DEVELOPMENT">
                    Development
                  </option>

                  <option value="TEST">
                    Test
                  </option>
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">
                  Lifetime
                </label>

                <select
                  className={`form-input ${
                    fieldErrors.lifetimeHours
                      ? "form-input-error"
                      : ""
                  }`}
                  name="lifetimeHours"
                  value={
                    form.lifetimeHours
                  }
                  onChange={
                    handleChange
                  }
                  onBlur={() =>
                    validateOnBlur(
                      "lifetimeHours"
                    )
                  }
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

                {fieldErrors.lifetimeHours && (
                  <span className="field-error">
                    {
                      fieldErrors.lifetimeHours
                    }
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* SUMMARY */}

        <aside className="create-summary-wrapper">
          <div className="glass-panel create-summary">
            <div className="create-summary-icon">
              <Rocket size={24} />
            </div>

            <h3>
              Provisioning Summary
            </h3>

            <p className="create-summary-description">
              Review the environment
              before launching.
            </p>

            <div className="create-summary-list">
              <div>
                <span>
                  Application
                </span>

                <strong>
                  {form.applicationName ||
                    "Not specified"}
                </strong>
              </div>

              <div>
                <span>
                  Template
                </span>

                <strong>
                  {selectedTemplate
                    ?.name ||
                    "Not selected"}
                </strong>
              </div>

              <div>
                <span>
                  Environment
                </span>

                <strong>
                  {
                    form.environmentType
                  }
                </strong>
              </div>

              <div>
                <span>
                  Branch
                </span>

                <strong>
                  {form.branchName ||
                    "-"}
                </strong>
              </div>

              <div>
                <span>
                  Lifetime
                </span>

                <strong className="summary-lifetime">
                  <Clock3
                    size={14}
                  />

                  {
                    form.lifetimeHours
                  }{" "}
                  hours
                </strong>
              </div>

              {selectedTemplate && (
                <>
                  <div>
                    <span>
                      Runtime
                    </span>

                    <strong>
                      {
                        selectedTemplate.runtimeLanguage
                      }{" "}
                      {
                        selectedTemplate.runtimeVersion
                      }
                    </strong>
                  </div>

                  <div>
                    <span>
                      Database
                    </span>

                    <strong>
                      {
                        selectedTemplate.databaseType
                      }
                    </strong>
                  </div>
                </>
              )}
            </div>

            <button
              className="btn btn-primary create-submit"
              type="submit"
              disabled={
                submitting ||
                templates.length ===
                  0
              }
            >
              {submitting ? (
                <>
                  <span className="button-spinner" />

                  Provisioning...
                </>
              ) : (
                <>
                  <Rocket
                    size={17}
                  />

                  Launch Environment
                </>
              )}
            </button>

            <div className="create-summary-note">
              <Boxes size={14} />

              DevSpace will validate
              your repository before
              provisioning.
            </div>
          </div>
        </aside>
      </form>
    </>
  );
}

export default CreateEnvironment;