import { NavLink } from "react-router-dom";

import {
  Boxes,
  LayoutDashboard,
  Layers3,
  Sparkles,
  ShieldCheck,
  Server,
  Users,
  X,
} from "lucide-react";

import { useAuth } from "../context/AuthContext";

function Sidebar({
  mobileOpen,
  closeMobileSidebar,
}) {
  const { user } = useAuth();

  const navClass = ({ isActive }) =>
    `nav-item ${isActive ? "active" : ""}`;

  const handleNavigation = () => {
    if (closeMobileSidebar) {
      closeMobileSidebar();
    }
  };

  return (
    <aside
      className={`sidebar ${
        mobileOpen
          ? "sidebar-mobile-open"
          : ""
      }`}
    >
      {/* ======================
          BRAND
      ====================== */}

      <div className="sidebar-header">
        <div className="logo-area">
          <div className="logo-icon">
            <Sparkles size={23} />
          </div>

          <div className="logo-text">
            Dev<span>Space</span>
          </div>
        </div>

        <button
          className="sidebar-mobile-close"
          onClick={closeMobileSidebar}
        >
          <X size={19} />
        </button>
      </div>

      {/* ======================
          WORKSPACE
      ====================== */}

      <div className="sidebar-section-title">
        Workspace
      </div>

      <nav className="nav-menu">
        <NavLink
          to="/dashboard"
          className={navClass}
          onClick={handleNavigation}
        >
          <LayoutDashboard size={18} />

          <span>
            Dashboard
          </span>
        </NavLink>

        <NavLink
          to="/environments"
          className={navClass}
          onClick={handleNavigation}
        >
          <Server size={18} />

          <span>
            My Environments
          </span>
        </NavLink>

        <NavLink
          to="/templates"
          className={navClass}
          onClick={handleNavigation}
        >
          <Layers3 size={18} />

          <span>
            Templates
          </span>
        </NavLink>
      </nav>

      {/* ======================
          ADMINISTRATION
      ====================== */}

      {user?.role === "ROLE_ADMIN" && (
        <>
          <div className="sidebar-section-title">
            Administration
          </div>

          <nav className="nav-menu">
            <NavLink
              to="/admin/environments"
              className={navClass}
              onClick={handleNavigation}
            >
              <Boxes size={18} />

              <span>
                All Environments
              </span>
            </NavLink>

            <NavLink
              to="/admin/templates"
              className={navClass}
              onClick={handleNavigation}
            >
              <ShieldCheck size={18} />

              <span>
                Manage Templates
              </span>
            </NavLink>

            <NavLink
              to="/admin/users"
              className={navClass}
              onClick={handleNavigation}
            >
              <Users size={18} />

              <span>
                Manage Users
              </span>
            </NavLink>
          </nav>
        </>
      )}

      {/* ======================
          PLATFORM INFO
      ====================== */}

      <div className="sidebar-platform-card">
        <div className="sidebar-platform-icon">
          <Sparkles size={15} />
        </div>

        <div>
          <strong>
            DevSpace Platform
          </strong>

          <span>
            Internal Developer Platform
          </span>
        </div>
      </div>
    </aside>
  );
}

export default Sidebar;