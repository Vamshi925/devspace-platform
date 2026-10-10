import { useEffect, useRef, useState } from "react";
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

import { useAuth } from "../context/AuthContext";

function Topbar({ onMenuClick }) {
  const navigate = useNavigate();

  const { user, logout } = useAuth();

  const [notifications, setNotifications] =
    useState([]);

  const [showNotifications, setShowNotifications] =
    useState(false);

  const [showProfile, setShowProfile] =
    useState(false);

  const notificationRef = useRef(null);
  const profileRef = useRef(null);

  const loadNotifications = async () => {
    try {
      const data =
        await getMyNotifications();

      setNotifications(data || []);
    } catch (error) {
      console.error(
        "Unable to load notifications:",
        error
      );
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        notificationRef.current &&
        !notificationRef.current.contains(
          event.target
        )
      ) {
        setShowNotifications(false);
      }

      if (
        profileRef.current &&
        !profileRef.current.contains(
          event.target
        )
      ) {
        setShowProfile(false);
      }
    };

    document.addEventListener(
      "mousedown",
      handleClickOutside
    );

    return () => {
      document.removeEventListener(
        "mousedown",
        handleClickOutside
      );
    };
  }, []);

  const unreadCount =
    notifications.filter(
      (notification) =>
        !notification.read
    ).length;

  const latestNotifications =
    notifications.slice(0, 5);

  const handleNotificationClick =
    async (notification) => {
      try {
        if (!notification.read) {
          await markNotificationAsRead(
            notification.notificationId
          );

          setNotifications((current) =>
            current.map((item) =>
              item.notificationId ===
              notification.notificationId
                ? {
                    ...item,
                    read: true,
                  }
                : item
            )
          );
        }
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
    if (!user?.email) {
      return "DS";
    }

    return user.email
      .split("@")[0]
      .slice(0, 2)
      .toUpperCase();
  };

  return (
    <header className="topbar">
      <div className="topbar-left">
  <button
    className="topbar-mobile-menu"
    onClick={onMenuClick}
    aria-label="Open navigation"
  >
    <Menu size={20} />
  </button>

  <div>
    <h2>
      Internal Developer Platform
    </h2>

    <p>
      Self-service environments,
      templates and automation
    </p>
  </div>
</div>

      <div className="topbar-right">
        {/* =====================
            NOTIFICATIONS
        ===================== */}

        <div
          className="topbar-notification-wrapper"
          ref={notificationRef}
        >
          <button
            className="topbar-icon-button"
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
                {unreadCount > 9
                  ? "9+"
                  : unreadCount}
              </span>
            )}
          </button>

          {showNotifications && (
            <div className="topbar-notification-popup">
              <div className="topbar-popup-header">
                <div>
                  <h3>
                    Notifications
                  </h3>

                  <p>
                    {unreadCount} unread
                  </p>
                </div>

                <Bell size={17} />
              </div>

              <div className="topbar-popup-list">
                {latestNotifications.length ===
                0 ? (
                  <div className="topbar-popup-empty">
                    <Bell size={22} />

                    <span>
                      No notifications yet
                    </span>
                  </div>
                ) : (
                  latestNotifications.map(
                    (notification) => (
                      <button
                        key={
                          notification.notificationId
                        }
                        className={`topbar-notification-item ${
                          !notification.read
                            ? "unread"
                            : ""
                        }`}
                        onClick={() =>
                          handleNotificationClick(
                            notification
                          )
                        }
                      >
                        <div className="topbar-notification-dot" />

                        <div>
                          <strong>
                            {notification.type
                              ?.replaceAll(
                                "_",
                                " "
                              )}
                          </strong>

                          <p>
                            {
                              notification.message
                            }
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
                className="topbar-popup-footer"
                onClick={() => {
                  navigate(
                    "/notifications"
                  );

                  setShowNotifications(
                    false
                  );
                }}
              >
                View all notifications
              </button>
            </div>
          )}
        </div>

        {/* =====================
            USER PROFILE
        ===================== */}

        <div
          className="topbar-profile-wrapper"
          ref={profileRef}
        >
          <button
            className="topbar-profile-button"
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
              <strong>
                {user?.email}
              </strong>

              <span>
                {user?.role ===
                "ROLE_ADMIN"
                  ? "Administrator"
                  : "Developer"}
              </span>
            </div>

            <ChevronDown
              size={15}
              className={`topbar-chevron ${
                showProfile
                  ? "open"
                  : ""
              }`}
            />
          </button>

          {showProfile && (
            <div className="topbar-profile-popup">
              <div className="topbar-profile-header">
                <div className="topbar-profile-avatar">
                  {getInitials()}
                </div>

                <div>
                  <strong>
                    {user?.email}
                  </strong>

                  <span>
                    DevSpace account
                  </span>
                </div>
              </div>

              <div className="topbar-profile-role">
                {user?.role ===
                "ROLE_ADMIN" ? (
                  <>
                    <ShieldCheck size={15} />

                    Administrator
                  </>
                ) : (
                  <>
                    <UserRound size={15} />

                    Developer
                  </>
                )}
              </div>

              <div className="topbar-profile-divider" />

              <button
                className="topbar-profile-menu-item logout"
                onClick={handleLogout}
              >
                <LogOut size={16} />

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