import { useState } from "react";
import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  Boxes,
  CloudCog,
  LockKeyhole,
  Mail,
  Phone,
  Rocket,
  ServerCog,
  ShieldCheck,
  Sparkles,
  UserRound,
} from "lucide-react";

import { registerUser } from "../api/authApi";

function Register() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    name: "",
    email: "",
    phoneNumber: "",
    password: "",
    confirmPassword: "",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [submitting, setSubmitting] =
    useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (
      form.password !==
      form.confirmPassword
    ) {
      setError("Passwords do not match");
      return;
    }

    try {
      setSubmitting(true);

      await registerUser({
        name: form.name,
        email: form.email,
        phoneNumber: form.phoneNumber,
        password: form.password,
      });

      setSuccess(
        "Account created successfully. Redirecting to sign in..."
      );

      setTimeout(() => {
        navigate("/");
      }, 1200);
    } catch (error) {
      console.error(
        "Registration failed:",
        error.response?.data || error
      );

      setError(
        error.response?.data?.message ||
          "Unable to create account"
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <section className="login-brand-panel">
        <div className="login-brand-content">
          <div className="login-logo">
            <div className="login-logo-icon">
              <Sparkles size={28} />
            </div>

            <div className="login-logo-text">
              Dev<span>Space</span>
            </div>
          </div>

          <div className="login-hero">
            <div className="login-badge">
              Developer Self-Service Platform
            </div>

            <h1>
              Your workspace.
              <br />
              On demand.
            </h1>

            <p>
              Create a developer account and launch
              temporary environments using approved
              platform templates.
            </p>
          </div>

          <div className="login-feature-grid">
            <div className="login-feature-card">
              <div className="login-feature-icon">
                <Rocket size={20} />
              </div>

              <div>
                <strong>
                  Launch Environments
                </strong>

                <span>
                  Create isolated developer
                  environments when you need them.
                </span>
              </div>
            </div>

            <div className="login-feature-card">
              <div className="login-feature-icon">
                <Boxes size={20} />
              </div>

              <div>
                <strong>
                  Approved Templates
                </strong>

                <span>
                  Use standardized runtime and
                  infrastructure configurations.
                </span>
              </div>
            </div>

            <div className="login-feature-card">
              <div className="login-feature-icon">
                <CloudCog size={20} />
              </div>

              <div>
                <strong>
                  Automated Lifecycle
                </strong>

                <span>
                  Environments expire and clean
                  themselves up automatically.
                </span>
              </div>
            </div>

            <div className="login-feature-card">
              <div className="login-feature-icon">
                <ShieldCheck size={20} />
              </div>

              <div>
                <strong>
                  Developer Access
                </strong>

                <span>
                  New accounts receive the standard
                  developer role.
                </span>
              </div>
            </div>
          </div>
        </div>

        <div className="login-glow login-glow-one" />
        <div className="login-glow login-glow-two" />
      </section>

      <section className="login-form-panel">
        <div className="login-card">
          <div className="login-card-icon">
            <ServerCog size={24} />
          </div>

          <h2>Create account</h2>

          <p className="login-card-description">
            Join DevSpace as a developer.
          </p>

          {error && (
            <div className="login-error">
              {error}
            </div>
          )}

          {success && (
            <div className="register-success">
              {success}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">
                Full Name
              </label>

              <div className="login-password-wrapper">
                <UserRound size={16} />

                <input
                  className="form-input login-input"
                  type="text"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Your name"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Email Address
              </label>

              <div className="login-password-wrapper">
                <Mail size={16} />

                <input
                  className="form-input login-input"
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={handleChange}
                  placeholder="you@devspace.com"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Phone Number
              </label>

              <div className="login-password-wrapper">
                <Phone size={16} />

                <input
                  className="form-input login-input"
                  type="tel"
                  name="phoneNumber"
                  value={form.phoneNumber}
                  onChange={handleChange}
                  placeholder="9876543210"
                  autoComplete="tel"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Password
              </label>

              <div className="login-password-wrapper">
                <LockKeyhole size={16} />

                <input
                  className="form-input login-input"
                  type="password"
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Create password"
                  autoComplete="new-password"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Confirm Password
              </label>

              <div className="login-password-wrapper">
                <LockKeyhole size={16} />

                <input
                  className="form-input login-input"
                  type="password"
                  name="confirmPassword"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Confirm password"
                  autoComplete="new-password"
                  required
                />
              </div>
            </div>

            <button
              className="btn btn-primary login-submit"
              type="submit"
              disabled={submitting}
            >
              {submitting ? (
                <>
                  <span className="button-spinner" />
                  Creating account...
                </>
              ) : (
                <>
                  Create Account
                  <Rocket size={16} />
                </>
              )}
            </button>
          </form>

          <div className="register-login-link">
            Already have an account?{" "}
            <Link to="/">
              Sign in
            </Link>
          </div>

          <div className="login-security">
            <ShieldCheck size={14} />

            New registrations receive developer
            access only.
          </div>
        </div>
      </section>
    </div>
  );
}

export default Register;