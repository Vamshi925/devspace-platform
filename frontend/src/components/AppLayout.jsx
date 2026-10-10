import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";

import Sidebar from "./Sidebar";
import Topbar from "./Topbar";

function AppLayout({ children }) {
  const location = useLocation();

  const [
    mobileSidebarOpen,
    setMobileSidebarOpen,
  ] = useState(false);

  useEffect(() => {
    setMobileSidebarOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (mobileSidebarOpen) {
      document.body.style.overflow =
        "hidden";
    } else {
      document.body.style.overflow =
        "";
    }

    return () => {
      document.body.style.overflow =
        "";
    };
  }, [mobileSidebarOpen]);

  return (
    <div className="app-shell">
      <Sidebar
        mobileOpen={mobileSidebarOpen}
        closeMobileSidebar={() =>
          setMobileSidebarOpen(false)
        }
      />

      {mobileSidebarOpen && (
        <div
          className="sidebar-overlay"
          onClick={() =>
            setMobileSidebarOpen(false)
          }
        />
      )}

      <div className="app-main">
        <Topbar
          onMenuClick={() =>
            setMobileSidebarOpen(true)
          }
        />

        <main className="main-content">
          <div className="page-container">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}

export default AppLayout;