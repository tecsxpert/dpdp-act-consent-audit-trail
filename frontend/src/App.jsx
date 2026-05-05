import { BrowserRouter, Routes, Route } from "react-router-dom"
import { useNavigate } from "react-router-dom"
import { AuthProvider, useAuth } from "./context/AuthContext"
import ProtectedRoute from "./components/ProtectedRoute"
import ConsentList from "./pages/ConsentList"
import ConsentForm from "./pages/ConsentForm"
import ConsentDetail from "./pages/ConsentDetail"
import Dashboard from "./pages/Dashboard"
import Login from "./pages/Login"
import Analytics from "./pages/Analytics"

function Navbar() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()

  const handleLogout = () => {
    logout()
    navigate("/login")
  }

  return (
    <nav style={{
      background: "linear-gradient(90deg, #1B4F8A, #163f6e)",
      padding: "0 24px",
      height: "60px",
      display: "flex",
      justifyContent: "space-between",
      alignItems: "center",
      boxShadow: "0 2px 10px rgba(0,0,0,0.2)",
      fontFamily: "Arial, sans-serif"
    }}>
      {/* Logo */}
      <div
        onClick={() => navigate("/")}
        style={{
          display: "flex",
          alignItems: "center",
          gap: "10px",
          cursor: "pointer"
        }}
      >
        <span style={{ fontSize: "20px" }}>🔐</span>
        <div>
          <div style={{
            color: "white",
            fontWeight: "700",
            fontSize: "16px",
            letterSpacing: "-0.3px"
          }}>
            DPDP Act
          </div>
          <div style={{
            color: "rgba(255,255,255,0.5)",
            fontSize: "10px",
            marginTop: "-2px"
          }}>
            Consent Audit Trail
          </div>
        </div>
      </div>

      {/* Nav Links and Actions */}
      <div style={{
        display: "flex",
        alignItems: "center",
        gap: "8px"
      }}>
        {user && (
          <button
            onClick={() => navigate("/dashboard")}
            style={{
              background: "transparent",
              border: "none",
              color: "rgba(255,255,255,0.8)",
              fontSize: "13px",
              fontWeight: "500",
              cursor: "pointer",
              padding: "6px 12px",
              borderRadius: "6px",
              fontFamily: "Arial, sans-serif"
            }}
            onMouseOver={e => e.target.style.background = "rgba(255,255,255,0.1)"}
            onMouseOut={e => e.target.style.background = "transparent"}
          >
            Dashboard
          </button>
        )}
        {user && (
          <button
            onClick={() => navigate("/analytics")}
            style={{
              background: "transparent",
              border: "none",
              color: "rgba(255,255,255,0.8)",
              fontSize: "13px",
              fontWeight: "500",
              cursor: "pointer",
              padding: "6px 12px",
              borderRadius: "6px",
              fontFamily: "Arial, sans-serif"
            }}
            onMouseOver={e => e.target.style.background = "rgba(255,255,255,0.1)"}
            onMouseOut={e => e.target.style.background = "transparent"}
          >
            Analytics
          </button>
        )}
        {user && (
          <span style={{
            color: "rgba(255,255,255,0.4)",
            fontSize: "13px",
            padding: "0 4px"
          }}>
            |
          </span>
        )}
        {user && (
          <span style={{
            color: "rgba(255,255,255,0.6)",
            fontSize: "13px"
          }}>
            👤 {user.username}
          </span>
        )}
        {user && (
          <button
            onClick={() => navigate("/create")}
            style={{
              background: "#22c55e",
              border: "none",
              color: "white",
              fontSize: "13px",
              fontWeight: "600",
              cursor: "pointer",
              padding: "8px 16px",
              borderRadius: "6px",
              fontFamily: "Arial, sans-serif",
              marginLeft: "4px"
            }}
            onMouseOver={e => e.target.style.background = "#16a34a"}
            onMouseOut={e => e.target.style.background = "#22c55e"}
          >
            + New
          </button>
        )}
        {user && (
          <button
            onClick={handleLogout}
            style={{
              background: "rgba(255,255,255,0.1)",
              border: "1px solid rgba(255,255,255,0.2)",
              color: "rgba(255,255,255,0.8)",
              fontSize: "13px",
              fontWeight: "500",
              cursor: "pointer",
              padding: "7px 14px",
              borderRadius: "6px",
              fontFamily: "Arial, sans-serif"
            }}
            onMouseOver={e => e.target.style.background = "rgba(255,255,255,0.2)"}
            onMouseOut={e => e.target.style.background = "rgba(255,255,255,0.1)"}
          >
            Logout
          </button>
        )}
      </div>
    </nav>
  )
}

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-gray-50">
          <Navbar />
          <main className="max-w-7xl mx-auto mt-6">
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route
                path="/"
                element={
                  <ProtectedRoute>
                    <ConsentList />
                  </ProtectedRoute>
                }
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
                path="/analytics"
                element={
                  <ProtectedRoute>
                    <Analytics />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/create"
                element={
                  <ProtectedRoute>
                    <ConsentForm />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/edit/:id"
                element={
                  <ProtectedRoute>
                    <ConsentForm />
                  </ProtectedRoute>
                }
              />
              <Route
                path="/detail/:id"
                element={
                  <ProtectedRoute>
                    <ConsentDetail />
                  </ProtectedRoute>
                }
              />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App