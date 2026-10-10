export const NOTIFICATIONS_UPDATED = "devspace-notifications-updated";

export const emitNotificationsUpdated = () => {
  window.dispatchEvent(new Event(NOTIFICATIONS_UPDATED));
};