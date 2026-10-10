import { useEffect, useMemo, useState } from "react";

import {
  Bell,
  CheckCheck,
  CircleAlert,
  CircleCheckBig,
  Clock3,
  Server,
} from "lucide-react";

import {
  getMyNotifications,
  markNotificationAsRead,
} from "../api/notificationApi";
import PageLoader from "../components/PageLoader";
import NotificationsSkeleton from "../components/NotificationsSkeleton";
import Toast from "../components/Toast";

function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [filter, setFilter] = useState("ALL");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [toast, setToast] = useState(null);

const showToast = (type, message) => {
  setToast({
    type,
    message,
  });

  setTimeout(() => {
    setToast(null);
  }, 3500);
};

  const loadNotifications = async () => {
    try {
      setLoading(true);

      const data = await getMyNotifications();

      setNotifications(data);
      setError("");
    } catch (error) {
      setError(
        error.response?.data?.message ||
          "Unable to load notifications"
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkRead = async (notificationId) => {
    try {
      await markNotificationAsRead(notificationId);

      setNotifications((current) =>
        current.map((notification) =>
          notification.notificationId === notificationId
            ? {
                ...notification,
                read: true,
              }
            : notification
        )
      );
    } catch (error) {
  showToast(
    "error",
    error.response?.data?.message ||
      "Unable to mark notification as read"
  );
}
  };

  const filteredNotifications = useMemo(() => {
    if (filter === "UNREAD") {
      return notifications.filter(
        (notification) => !notification.read
      );
    }

    if (filter === "READ") {
      return notifications.filter(
        (notification) => notification.read
      );
    }

    return notifications;
  }, [notifications, filter]);

  const unreadCount = notifications.filter(
    (notification) => !notification.read
  ).length;

  const getNotificationIcon = (type) => {
    switch (type) {
      case "ENVIRONMENT_READY":
        return <CircleCheckBig size={20} />;

      case "ENVIRONMENT_FAILED":
        return <CircleAlert size={20} />;

      case "ENVIRONMENT_EXPIRING":
        return <Clock3 size={20} />;

      case "ENVIRONMENT_EXPIRED":
        return <Clock3 size={20} />;

      case "ENVIRONMENT_DELETED":
        return <Server size={20} />;

      default:
        return <Bell size={20} />;
    }
  };

  const getNotificationClass = (type) => {
    switch (type) {
      case "ENVIRONMENT_READY":
        return "notification-ready";

      case "ENVIRONMENT_FAILED":
        return "notification-failed";

      case "ENVIRONMENT_EXPIRING":
      case "ENVIRONMENT_EXPIRED":
        return "notification-warning";

      case "ENVIRONMENT_DELETED":
        return "notification-neutral";

      default:
        return "notification-neutral";
    }
  };

  if (loading) {
  return <NotificationsSkeleton />;
}

  return (
    <>
      <div className="page-header">
        <div>
          <h1 className="page-title">
            Notifications
          </h1>

          <p className="page-subtitle">
            Track environment lifecycle updates and
            platform events.
          </p>
        </div>

        <div className="notifications-header-count">
          <Bell size={17} />

          <span>
            {unreadCount} unread
          </span>
        </div>
      </div>

      <div className="notification-filter-bar">
        <button
          className={`notification-filter-button ${
            filter === "ALL" ? "active" : ""
          }`}
          onClick={() => setFilter("ALL")}
        >
          All
          <span>{notifications.length}</span>
        </button>

        <button
          className={`notification-filter-button ${
            filter === "UNREAD" ? "active" : ""
          }`}
          onClick={() => setFilter("UNREAD")}
        >
          Unread
          <span>{unreadCount}</span>
        </button>

        <button
          className={`notification-filter-button ${
            filter === "READ" ? "active" : ""
          }`}
          onClick={() => setFilter("READ")}
        >
          Read
          <span>
            {notifications.length - unreadCount}
          </span>
        </button>
      </div>

      {error && (
        <div className="glass-panel environment-error">
          {error}
        </div>
      )}

      {filteredNotifications.length === 0 ? (
        <div className="glass-panel notifications-empty-page">
          <div className="notifications-empty-icon">
            <Bell size={28} />
          </div>

          <h3>No notifications</h3>

          <p>
            There are no notifications matching this filter.
          </p>
        </div>
      ) : (
        <div className="notifications-list">
          {filteredNotifications.map(
            (notification) => (
              <div
                key={notification.notificationId}
                className={`notification-card ${
                  !notification.read
                    ? "notification-card-unread"
                    : ""
                }`}
              >
                <div
                  className={`notification-type-icon ${getNotificationClass(
                    notification.type
                  )}`}
                >
                  {getNotificationIcon(
                    notification.type
                  )}
                </div>

                <div className="notification-card-content">
                  <div className="notification-card-top">
                    <div>
                      <div className="notification-title-row">
                        <h3>
                          {notification.title}
                        </h3>

                        {!notification.read && (
                          <span className="notification-new-badge">
                            NEW
                          </span>
                        )}
                      </div>

                      <span className="notification-type">
                        {notification.type}
                      </span>
                    </div>

                    <span className="notification-time">
                      {new Date(
                        notification.createdAt
                      ).toLocaleString()}
                    </span>
                  </div>

                  <p className="notification-message">
                    {notification.message}
                  </p>

                  {notification.environmentId && (
                    <div className="notification-environment">
                      <Server size={14} />

                      Environment:
                      <span>
                        {notification.environmentId}
                      </span>
                    </div>
                  )}

                  {!notification.read && (
                    <button
                      className="notification-read-button"
                      onClick={() =>
                        handleMarkRead(
                          notification.notificationId
                        )
                      }
                    >
                      <CheckCheck size={15} />
                      Mark as Read
                    </button>
                  )}
                </div>
              </div>
            )
          )}
        </div>
      )}
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

export default Notifications;