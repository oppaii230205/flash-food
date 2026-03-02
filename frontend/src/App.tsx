import { Routes, Route } from "react-router-dom";
import { LandingPage } from "@/pages/LandingPage";

function NotFound() {
  return (
    <div className="min-h-screen flex items-center justify-center text-center px-4">
      <div>
        <div className="text-6xl mb-4">🌿</div>
        <h1 className="text-2xl font-extrabold text-green-900 mb-2">
          Page Not Found
        </h1>
        <p className="text-green-700/60 mb-6">
          The page you're looking for doesn't exist.
        </p>
        <a href="/" className="text-green-600 font-bold hover:underline">
          ← Back to Home
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
      <Route path="/" element={<LandingPage />} />
      <Route path="/deals" element={<PlaceholderPage title="Flash Deals" />} />
      <Route
        path="/profile"
        element={<PlaceholderPage title="Your Profile" />}
      />
      <Route path="/orders" element={<PlaceholderPage title="My Orders" />} />
      <Route
        path="/store/*"
        element={<PlaceholderPage title="Store Dashboard" />}
      />
      <Route
        path="/admin/*"
        element={<PlaceholderPage title="Admin Panel" />}
      />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
