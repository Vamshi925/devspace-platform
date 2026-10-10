import {
  BrowserRouter,
  Routes,
  Route,
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";

import Environments from "./pages/Environments";
import CreateEnvironment from "./pages/CreateEnvironment";
import EnvironmentDetails from "./pages/EnvironmentDetails";

import Notifications from "./pages/Notifications";
import Templates from "./pages/Templates";

import AdminEnvironments from "./pages/AdminEnvironments";
import AdminTemplates from "./pages/AdminTemplates";
import AdminUsers from "./pages/AdminUsers";

import ProtectedRoute from "./components/ProtectedRoute";
import NotFound from "./pages/NotFound";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={<Login />}
        />

        <Route
          path="/register"
          element={<Register />}
        />

        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/environments"
          element={
            <ProtectedRoute>
              <Environments />
            </ProtectedRoute>
          }
        />

        <Route
          path="/environments/create"
          element={
            <ProtectedRoute>
              <CreateEnvironment />
            </ProtectedRoute>
          }
        />

        <Route
          path="/environments/:environmentId"
          element={
            <ProtectedRoute>
              <EnvironmentDetails />
            </ProtectedRoute>
          }
        />

        <Route
          path="/notifications"
          element={
            <ProtectedRoute>
              <Notifications />
            </ProtectedRoute>
          }
        />

        <Route
          path="/templates"
          element={
            <ProtectedRoute>
              <Templates />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/environments"
          element={
            <ProtectedRoute>
              <AdminEnvironments />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/templates"
          element={
            <ProtectedRoute>
              <AdminTemplates />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/users"
          element={
            <ProtectedRoute>
              <AdminUsers />
            </ProtectedRoute>
          }
        />
        <Route
  path="*"
  element={<NotFound />}
/>
      </Routes>
    </BrowserRouter>
  );
}

export default App;