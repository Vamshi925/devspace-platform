import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  getMyNotifications,
  markNotificationAsRead,
} from "../api/notificationApi";

function Notifications() {
  const navigate = useNavigate();

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadNotifications = async () => {
    try {
      setLoading(true);

      const data = await getMyNotifications();

      setNotifications(data);
      setError("");
    } catch {
      setError("Unable to load notifications");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, []);

  const handleMarkAsRead = async (notificationId) => {
    try {
      await markNotificationAsRead(notificationId);
      await loadNotifications();
    } catch (error) {
      alert(
        error.response?.data?.message ||
          "Unable to mark notification as read"
      );
    }
  };

  if (loading) {
    return <p>Loading notifications...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div>
      <button onClick={() => navigate("/dashboard")}>
        Dashboard
      </button>

      <h1>Notifications</h1>

      {notifications.length === 0 ? (
        <p>No notifications found.</p>
      ) : (
        notifications.map((notification) => (
          <div
            key={notification.notificationId}
            style={{
              border: "1px solid #ccc",
              padding: "15px",
              marginBottom: "15px",
            }}
          >
            <h3>{notification.title}</h3>

            <p>
              <strong>Type:</strong>{" "}
              {notification.type}
            </p>

            <p>{notification.message}</p>

            <p>
              <strong>Environment:</strong>{" "}
              {notification.environmentId || "-"}
            </p>

            <p>
              <strong>Created:</strong>{" "}
              {new Date(
                notification.createdAt
              ).toLocaleString()}
            </p>

            <p>
              <strong>Status:</strong>{" "}
              {notification.read ? "Read" : "Unread"}
            </p>

            {!notification.read && (
              <button
                onClick={() =>
                  handleMarkAsRead(
                    notification.notificationId
                  )
                }
              >
                Mark as Read
              </button>
            )}
          </div>
        ))
      )}
    </div>
  );
}

export default Notifications;
