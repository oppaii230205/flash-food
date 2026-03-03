import { Routes, Route, Navigate } from "react-router-dom";
import { LandingPage } from "@/pages/LandingPage";
import { ProtectedRoute } from "@/components/guards/ProtectedRoute";
import { StoreDashboardLayout } from "@/layouts/StoreDashboardLayout";
import { StoreDashboard } from "@/pages/store/StoreDashboard";
import { StoreFoodItems } from "@/pages/store/StoreFoodItems";
import { StoreOrders } from "@/pages/store/StoreOrders";
import { StoreSettings } from "@/pages/store/StoreSettings";
import { Leaf } from "@phosphor-icons/react";

function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center text-center px-4">
      <div>
        <div className="text-6xl mb-4">🌿</div>
        <h1 className="text-2xl font-extrabold text-green-900 mb-2">
          Page Not Found
        </h1>
        <p className="text-green-700/60 mb-6">
          The page you\'re looking for doesn\'t exist.
        </p>
        <a href="/" className="text-green-600 font-bold hover:underline">
          ← Back to Home
        </a>
      </div>
    </div>
  );
}

function Unauthorized() {
  return (
    <div className="min-h-screen flex items-center justify-center text-center px-4 bg-gray-50">
      <div>
        <div className="w-16 h-16 bg-red-50 rounded-2xl grid place-items-center mx-auto mb-5">
          <span className="text-3xl">🚫</span>
        </div>
        <h1 className="text-2xl font-extrabold text-gray-900 mb-2">
          Access Denied
        </h1>
        <p className="text-gray-500 mb-6">
          You don\'t have permission to view this page.
        </p>
        <a
          href="/"
          className="inline-flex items-center gap-1.5 text-green-600 font-bold hover:underline text-sm"
        >
          <Leaf size={14} weight="bold" /> Back to Home
        </a>
      </div>
    </div>
  );
}

function PlaceholderPage({ title }: { title: string }) {
  return (
    <div className="min-h-screen flex items-center justify-center text-center px-4 pt-28">
      <div>
        <div className="text-5xl mb-4">🚧</div>
        <h1 className="text-2xl font-extrabold text-green-900 mb-2">{title}</h1>
        <p className="text-green-700/60 mb-6">
          Coming soon — this page is under construction.
        </p>
        <a href="/" className="text-green-600 font-bold hover:underline">
          ← Back to Home
        </a>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/unauthorized" element={<Unauthorized />} />

      {/* Customer */}
      <Route path="/deals" element={<PlaceholderPage title="Flash Deals" />} />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <PlaceholderPage title="Your Profile" />
          </ProtectedRoute>
        }
      />
      <Route
        path="/orders"
        element={
          <ProtectedRoute>
            <PlaceholderPage title="My Orders" />
          </ProtectedRoute>
        }
      />

      {/* Store Owner portal */}
      <Route
        path="/store"
        element={
          <ProtectedRoute role="store_owner">
            <StoreDashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<StoreDashboard />} />
        <Route path="food-items" element={<StoreFoodItems />} />
        <Route path="orders" element={<StoreOrders />} />
        <Route path="settings" element={<StoreSettings />} />
        {/* Catch-all inside /store */}
        <Route path="*" element={<Navigate to="/store" replace />} />
      </Route>

      {/* Admin portal — placeholder until built */}
      <Route
        path="/admin/*"
        element={
          <ProtectedRoute role="admin">
            <PlaceholderPage title="Admin Panel" />
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
