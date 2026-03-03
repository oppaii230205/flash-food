import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";

interface Props {
  children: ReactNode;
  /**
   * Required role string. If supplied, users without this role are redirected
   * to an "unauthorized" page rather than the login page.
   * Values match the backend `UserResponse.role`: "CUSTOMER" | "STORE_OWNER" | "ADMIN"
   */
  role?: string;
}

/**
 * Wraps a route subtree with authentication (and optional role) checks.
 *
 * - Unauthenticated users are sent back to `/` with `{ state: { openLogin: true } }`.
 * - Authenticated users with the wrong role are sent to `/unauthorized`.
 */
export function ProtectedRoute({ children, role }: Props) {
  const { isAuthenticated, user } = useAuthStore();
  const location = useLocation();

  if (!isAuthenticated) {
    return (
      <Navigate
        to="/"
        state={{ openLogin: true, from: location.pathname }}
        replace
      />
    );
  }

  if (role && user?.role !== role) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <>{children}</>;
}
