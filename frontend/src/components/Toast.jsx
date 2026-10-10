import {
  CircleCheckBig,
  CircleX,
  Info,
  X,
} from "lucide-react";

function Toast({
  type = "success",
  message,
  onClose,
}) {
  const icon =
    type === "success" ? (
      <CircleCheckBig size={18} />
    ) : type === "error" ? (
      <CircleX size={18} />
    ) : (
      <Info size={18} />
    );

  return (
    <div className={`toast toast-${type}`}>
      <div className="toast-icon">
        {icon}
      </div>

      <div className="toast-message">
        {message}
      </div>

      <button
        className="toast-close"
        onClick={onClose}
      >
        <X size={16} />
      </button>
    </div>
  );
}

export default Toast;