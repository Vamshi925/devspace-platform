import { useState } from "react";

import {
  Mail,
  Save,
  ShieldCheck,
  UserCog,
} from "lucide-react";

import { updateUserRole } from "../api/adminApi";
import { useAuth } from "../context/AuthContext";

import ConfirmModal from "../components/ConfirmModal";
import Toast from "../components/Toast";
import AccessDenied from "./AccessDenied";

function AdminUsers() {
  const { user } = useAuth();

  const [email, setEmail] = useState("");
  const [role, setRole] = useState("ROLE_USER");

  const [submitting, setSubmitting] =
    useState(false);

  const [confirmOpen, setConfirmOpen] =
    useState(false);

  const [toast, setToast] =
    useState(null);

  const showToast = (type, message) => {
    setToast({
      type,
      message,
    });

    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!email.trim()) {
      showToast(
        "error",
        "Please enter a user email."
      );

      return;
    }

    setConfirmOpen(true);
  };

  const handleRoleUpdate = async () => {
    try {
      setSubmitting(true);

      const response =
        await updateUserRole(
          email.trim(),
          role
        );

      setConfirmOpen(false);

      showToast(
        "success",
        `${response.email} updated successfully to ${
          response.role === "ROLE_ADMIN"
            ? "Administrator"
            : "Developer"
        }.`
      );

      setEmail("");
      setRole("ROLE_USER");
    } catch (error) {
      setConfirmOpen(false);

      showToast(
        "error",
        error.response?.data?.message ||
          "Unable to update user role"
      );
    } finally {
      setSubmitting(false);
    }
  };

  const selectedRoleLabel =
    role === "ROLE_ADMIN"
      ? "Administrator"
      : "Developer";

  if (user?.role !== "ROLE_ADMIN") {
  return <AccessDenied />;
}

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            Manage Users
          </h1>

          <p className="page-subtitle">
            Promote or demote DevSpace users using
            their registered email address.
          </p>
        </div>

        <div className="admin-summary-pill">
          <ShieldCheck size={17} />
          Administrator Access
        </div>
      </div>

      <div className="admin-users-layout">
        <div className="glass-panel admin-user-card">
          <div className="admin-user-card-header">
            <div className="create-section-icon">
              <UserCog size={20} />
            </div>

            <div>
              <h2>
                User Role Management
              </h2>

              <p>
                Update a user's platform
                permissions.
              </p>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">
                User Email
              </label>

              <div className="create-input-icon">
                <Mail size={16} />

                <input
                  className="form-input"
                  type="email"
                  value={email}
                  onChange={(event) =>
                    setEmail(
                      event.target.value
                    )
                  }
                  placeholder="user2@devspace.com"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">
                Role
              </label>

              <select
                className="form-input"
                value={role}
                onChange={(event) =>
                  setRole(
                    event.target.value
                  )
                }
              >
                <option value="ROLE_USER">
                  Developer
                </option>

                <option value="ROLE_ADMIN">
                  Administrator
                </option>
              </select>
            </div>

            <button
              className="btn btn-primary admin-user-submit"
              type="submit"
            >
              <Save size={16} />
              Review Role Change
            </button>
          </form>
        </div>

        <div className="glass-panel admin-role-guide">
          <h3>
            Role Permissions
          </h3>

          <div className="role-guide-item">
            <div className="role-guide-icon developer-role">
              <UserCog size={18} />
            </div>

            <div>
              <strong>
                Developer
              </strong>

              <p>
                Can create, view, extend and delete
                their own environments.
              </p>
            </div>
          </div>

          <div className="role-guide-item">
            <div className="role-guide-icon admin-role">
              <ShieldCheck size={18} />
            </div>

            <div>
              <strong>
                Administrator
              </strong>

              <p>
                Can inspect all environments,
                manage templates and update user
                roles.
              </p>
            </div>
          </div>

          <div className="role-security-note">
            Role changes are stored immediately.
            The affected user must sign in again
            to receive a JWT containing the new
            role.
          </div>
        </div>
      </div>

      <ConfirmModal
        open={confirmOpen}
        title="Change User Role?"
        message={`Change ${email || "this user"} to ${selectedRoleLabel}? The user must sign in again before the new permissions take effect.`}
        confirmText={`Set as ${selectedRoleLabel}`}
        danger={role === "ROLE_ADMIN"}
        loading={submitting}
        onCancel={() =>
          setConfirmOpen(false)
        }
        onConfirm={handleRoleUpdate}
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

export default AdminUsers;