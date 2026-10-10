import {
  TriangleAlert,
  X,
} from "lucide-react";

function ConfirmModal({
  open,
  title,
  message,
  confirmText = "Confirm",
  cancelText = "Cancel",
  danger = false,
  loading = false,
  onConfirm,
  onCancel,
}) {
  if (!open) {
    return null;
  }

  return (
    <div className="modal-backdrop">
      <div className="confirm-modal">
        <button
          className="confirm-modal-close"
          onClick={onCancel}
          disabled={loading}
        >
          <X size={18} />
        </button>

        <div
          className={`confirm-modal-icon ${
            danger ? "danger" : ""
          }`}
        >
          <TriangleAlert size={22} />
        </div>

        <h2>{title}</h2>

        <p>
          {message}
        </p>

        <div className="confirm-modal-actions">
          <button
            className="btn btn-ghost"
            onClick={onCancel}
            disabled={loading}
          >
            {cancelText}
          </button>

          <button
            className={
              danger
                ? "btn btn-danger"
                : "btn btn-primary"
            }
            onClick={onConfirm}
            disabled={loading}
          >
            {loading
              ? "Please wait..."
              : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}

export default ConfirmModal;