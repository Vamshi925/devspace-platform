import { useCallback, useEffect, useState } from "react";
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

import NotificationsSkeleton from "../components/NotificationsSkeleton";
import Toast from "../components/Toast";
import { emitNotificationsUpdated } from "../utils/appEvents";
import { getErrorMessage } from "../utils/getErrorMessage";

function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [toast, setToast] = useState(null);

  const showToast = (type, message) => {
    setToast({ type, message });

    setTimeout(() => {
      setToast(null);
    }, 3500);
  };

  const loadNotifications = useCallback(async (showLoader = true) => {
    try {
      if (showLoader) {
        setLoading(true);
      }

      const data = await getMyNotifications();

      const unreadNotifications = (data || []).filter(
        (notification) => !notification.read
      );

      setNotifications(unreadNotifications);
      setError("");
    } catch (error) {
      if (showLoader) {
        setError(
          getErrorMessage(
            error,
            "Unable to load notifications"
          )
        );
      }
    } finally {
      if (showLoader) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    loadNotifications(true);

    const interval = setInterval(
      () => loadNotifications(false),
      15000
    );

    return () => {
      clearInterval(interval);
    };
  }, [loadNotifications]);

  const handleMarkRead = async (notificationId) => {
    try {
      await markNotificationAsRead(notificationId);

      setNotifications((current) =>
        current.filter(
          (notification) =>
            notification.notificationId !== notificationId
        )
      );

      emitNotificationsUpdated();

      showToast(
        "success",
        "Notification marked as read."
      );
    } catch (error) {
      showToast(
        "error",
        getErrorMessage(
          error,
          "Unable to mark notification as read"
        )
      );
    }
  };

  const unreadCount = notifications.length;

  const getNotificationIcon = (type) => {
    switch (type) {
      case "ENVIRONMENT_READY":
        return <CircleCheckBig size={20} />;

      case "ENVIRONMENT_FAILED":
        return <CircleAlert size={20} />;

      case "ENVIRONMENT_EXPIRING":
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
            Track unread environment lifecycle updates and platform events.
          </p>
        </div>

        <div className="notifications-header-count">
          <Bell size={17} />
          <span>{unreadCount} unread</span>
        </div>
      </div>

      {error && (
        <div className="glass-panel environment-error">
          {error}
        </div>
      )}

      {notifications.length === 0 ? (
        <div className="glass-panel notifications-empty-page">
          <div className="notifications-empty-icon">
            <Bell size={28} />
          </div>

          <h3>You're all caught up</h3>

          <p>
            You have no unread notifications.
          </p>
        </div>
      ) : (
        <div className="notifications-list">
          {notifications.map((notification) => (
            <div
              key={notification.notificationId}
              className="notification-card notification-card-unread"
            >
              <div
                className={`notification-type-icon ${getNotificationClass(
                  notification.type
                )}`}
              >
                {getNotificationIcon(notification.type)}
              </div>

              <div className="notification-card-content">
                <div className="notification-card-top">
                  <div>
                    <div className="notification-title-row">
                      <h3>{notification.title}</h3>

                      <span className="notification-new-badge">
                        NEW
                      </span>
                    </div>

                    <span className="notification-type">
                      {notification.type}
                    </span>
                  </div>

                  <span className="notification-time">
                    {notification.createdAt
                      ? new Date(
                          notification.createdAt
                        ).toLocaleString()
                      : ""}
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

                <button
                  type="button"
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
              </div>
            </div>
          ))}
        </div>
      )}

      {toast && (
        <Toast
          type={toast.type}
          message={toast.message}
          onClose={() => setToast(null)}
        />
      )}
    </>
  );
}

export default Notifications;