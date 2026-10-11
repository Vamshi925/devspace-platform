import { CircleCheckBig, CircleX, Info, X } from "lucide-react";

function Toast({ type = "success", message, onClose }) {
  const icon =
    type === "success" ? (
      <CircleCheckBig size={18} />
    ) : type === "error" ? (
      <CircleX size={18} />
    ) : (
      <Info size={18} />
    );

  return (
    <div
      className={`toast toast-${type}`}
      role={type === "error" ? "alert" : "status"}
      aria-live={type === "error" ? "assertive" : "polite"}
      aria-atomic="true"
    >
      <div className="toast-icon" aria-hidden="true">
        {icon}
      </div>

      <div className="toast-message">{message}</div>

      <button
        type="button"
        className="toast-close"
        onClick={onClose}
        aria-label="Close notification"
      >
        <X size={16} />
      </button>
    </div>
  );
}

export default Toast;