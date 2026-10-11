import { useCallback, useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Bell,
  ChevronDown,
  LogOut,
  Menu,
  ShieldCheck,
  UserRound,
} from "lucide-react";

import {
  getMyNotifications,
  markNotificationAsRead,
} from "../api/notificationApi";

import {
  NOTIFICATIONS_UPDATED,
  emitNotificationsUpdated,
} from "../utils/appEvents";

import { useAuth } from "../context/AuthContext";

function Topbar({ onMenuClick }) {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [notifications, setNotifications] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);
  const [showProfile, setShowProfile] = useState(false);

  const notificationRef = useRef(null);
  const profileRef = useRef(null);

  const loadNotifications = useCallback(async () => {
    try {
      const data = await getMyNotifications();

      const unreadNotifications = (data || []).filter(
        (notification) => !notification.read
      );

      setNotifications(unreadNotifications);
    } catch (error) {
      console.error(
        "Unable to load notifications:",
        error
      );
    }
  }, []);

  useEffect(() => {
    loadNotifications();

    const refresh = () => loadNotifications();

    const interval = setInterval(
      loadNotifications,
      15000
    );

    window.addEventListener(
      NOTIFICATIONS_UPDATED,
      refresh
    );

    return () => {
      clearInterval(interval);

      window.removeEventListener(
        NOTIFICATIONS_UPDATED,
        refresh
      );
    };
  }, [loadNotifications]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(event.target)
      ) {
        setShowNotifications(false);
      }

      if (
        profileRef.current &&
        !profileRef.current.contains(event.target)
      ) {
        setShowProfile(false);
      }
    };

    const handleEscape = (event) => {
      if (event.key === "Escape") {
        setShowNotifications(false);
        setShowProfile(false);
      }
    };

    document.addEventListener(
      "mousedown",
      handleClickOutside
    );

    document.addEventListener(
      "keydown",
      handleEscape
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleClickOutside
      );

      document.removeEventListener(
        "keydown",
        handleEscape
      );
    };
  }, []);

  const unreadCount = notifications.length;
  const latestNotifications = notifications.slice(0, 5);

  const handleNotificationClick = async (notification) => {
    try {
      await markNotificationAsRead(
        notification.notificationId
      );

      setNotifications((current) =>
        current.filter(
          (item) =>
            item.notificationId !==
            notification.notificationId
        )
      );

      emitNotificationsUpdated();
    } catch (error) {
      console.error(
        "Unable to mark notification as read:",
        error
      );
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const getInitials = () => {
    if (!user?.email) return "DS";

    return user.email
      .split("@")[0]
      .slice(0, 2)
      .toUpperCase();
  };

  return (
    <header className="topbar">
      <div className="topbar-left">
        <button
          type="button"
          className="topbar-mobile-menu"
          onClick={onMenuClick}
          aria-label="Open navigation"
        >
          <Menu size={20} />
        </button>

        <div>
          <h2>Internal Developer Platform</h2>
          <p>
            Self-service environments, templates and automation
          </p>
        </div>
      </div>

      <div className="topbar-right">
        <div
          className="topbar-notification-wrapper"
          ref={notificationRef}
        >
          <button
            type="button"
            className="topbar-icon-button"
            aria-label={`Notifications, ${unreadCount} unread`}
            aria-expanded={showNotifications}
            aria-haspopup="true"
            onClick={() => {
              setShowNotifications(
                (current) => !current
              );

              setShowProfile(false);
            }}
          >
            <Bell size={18} />

            {unreadCount > 0 && (
              <span className="topbar-notification-badge">
                {unreadCount > 9 ? "9+" : unreadCount}
              </span>
            )}
          </button>

          {showNotifications && (
            <div
              className="topbar-notification-popup"
              role="menu"
            >
              <div className="topbar-popup-header">
                <div>
                  <h3>Notifications</h3>
                  <p>{unreadCount} unread</p>
                </div>

                <Bell
                  size={17}
                  aria-hidden="true"
                />
              </div>

              <div className="topbar-popup-list">
                {latestNotifications.length === 0 ? (
                  <div className="topbar-popup-empty">
                    <Bell
                      size={22}
                      aria-hidden="true"
                    />

                    <span>You're all caught up</span>
                  </div>
                ) : (
                  latestNotifications.map(
                    (notification) => (
                      <button
                        type="button"
                        key={notification.notificationId}
                        className="topbar-notification-item unread"
                        onClick={() =>
                          handleNotificationClick(
                            notification
                          )
                        }
                      >
                        <div
                          className="topbar-notification-dot"
                          aria-hidden="true"
                        />

                        <div>
                          <strong>
                            {notification.type?.replaceAll(
                              "_",
                              " "
                            )}
                          </strong>

                          <p>
                            {notification.message}
                          </p>

                          <span>
                            {notification.createdAt
                              ? new Date(
                                  notification.createdAt
                                ).toLocaleString()
                              : ""}
                          </span>
                        </div>
                      </button>
                    )
                  )
                )}
              </div>

              <button
                type="button"
                className="topbar-popup-footer"
                onClick={() => {
                  navigate("/notifications");
                  setShowNotifications(false);
                }}
              >
                View all notifications
              </button>
            </div>
          )}
        </div>

        <div
          className="topbar-profile-wrapper"
          ref={profileRef}
        >
          <button
            type="button"
            className="topbar-profile-button"
            aria-label="Open account menu"
            aria-expanded={showProfile}
            aria-haspopup="true"
            onClick={() => {
              setShowProfile(
                (current) => !current
              );

              setShowNotifications(false);
            }}
          >
            <div className="topbar-avatar">
              {getInitials()}
            </div>

            <div className="topbar-user-info">
              <strong>{user?.email}</strong>

              <span>
                {user?.role === "ROLE_ADMIN"
                  ? "Administrator"
                  : "Developer"}
              </span>
            </div>

            <ChevronDown
              size={15}
              aria-hidden="true"
              className={`topbar-chevron ${
                showProfile ? "open" : ""
              }`}
            />
          </button>

          {showProfile && (
            <div
              className="topbar-profile-popup"
              role="menu"
            >
              <div className="topbar-profile-header">
                <div className="topbar-profile-avatar">
                  {getInitials()}
                </div>

                <div>
                  <strong>{user?.email}</strong>
                  <span>DevSpace account</span>
                </div>
              </div>

              <div className="topbar-profile-role">
                {user?.role === "ROLE_ADMIN" ? (
                  <>
                    <ShieldCheck
                      size={15}
                      aria-hidden="true"
                    />
                    Administrator
                  </>
                ) : (
                  <>
                    <UserRound
                      size={15}
                      aria-hidden="true"
                    />
                    Developer
                  </>
                )}
              </div>

              <div
                className="topbar-profile-divider"
                aria-hidden="true"
              />

              <button
                type="button"
                className="topbar-profile-menu-item logout"
                onClick={handleLogout}
              >
                <LogOut
                  size={16}
                  aria-hidden="true"
                />
                Sign out
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default Topbar;