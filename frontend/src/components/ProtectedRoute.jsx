import { Navigate } from "react-router-dom";

import { useAuth } from "../context/AuthContext";
import AppLayout from "./AppLayout";

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <AppLayout>
      {children}
    </AppLayout>
  );
}

export default ProtectedRoute;