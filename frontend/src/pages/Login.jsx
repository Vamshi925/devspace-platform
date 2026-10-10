import { useState } from "react";
import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  Boxes,
  CloudCog,
  LockKeyhole,
  Rocket,
  ServerCog,
  ShieldCheck,
  Sparkles,
} from "lucide-react";

import { loginUser } from "../api/authApi";
import { useAuth } from "../context/AuthContext";

function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();

    setSubmitting(true);
    setError("");

    try {
      const response = await loginUser(
        email,
        password
      );

      login(response.token);

      navigate("/dashboard");
    } catch (error) {
      console.error(
        "Login failed:",
        error.response?.data || error
      );

      setError(
        error.response?.data?.message ||
          "Invalid email or password"
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
              Internal Developer Platform
            </div>

            <h1>
              Build faster.
              <br />
              Provision smarter.
            </h1>

            <p>
              Launch standardized temporary developer
              environments with approved templates,
              automated provisioning and built-in lifecycle
              management.
            </p>
          </div>

          <div className="login-feature-grid">
            <div className="login-feature-card">
              <div className="login-feature-icon">
                <Rocket size={20} />
              </div>

              <div>
                <strong>
                  Self-Service Provisioning
                </strong>

                <span>
                  Launch isolated environments in minutes.
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
                  Standardized runtime and infrastructure
                  configurations.
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
                  Provision, extend, expire and clean up
                  environments automatically.
                </span>
              </div>
            </div>

            <div className="login-feature-card">
              <div className="login-feature-icon">
                <ShieldCheck size={20} />
              </div>

              <div>
                <strong>
                  Secure Access
                </strong>

                <span>
                  JWT authentication with developer and
                  administrator roles.
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

          <h2>Welcome back</h2>

          <p className="login-card-description">
            Sign in to access your DevSpace workspace.
          </p>

          {error && (
            <div className="login-error">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">
                Email Address
              </label>

              <input
                className="form-input login-input"
                type="email"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                placeholder="you@devspace.com"
                autoComplete="email"
                required
              />
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
                  value={password}
                  onChange={(event) =>
                    setPassword(event.target.value)
                  }
                  placeholder="Enter your password"
                  autoComplete="current-password"
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
                  Signing in...
                </>
              ) : (
                <>
                  Sign In
                  <Rocket size={16} />
                </>
              )}
            </button>
          </form>

          <div className="register-login-link">
            New to DevSpace?{" "}
            <Link to="/register">
              Create account
            </Link>
          </div>

          <div className="login-security">
            <ShieldCheck size={14} />

            Protected by DevSpace authentication and
            role-based access control.
          </div>
        </div>
      </section>
    </div>
  );
}

export default Login;