import { useState } from "react"
import { useNavigate } from "react-router-dom"
import { useAuth } from "../context/AuthContext"
import api from "../services/api"

function Login() {
  const navigate = useNavigate()
  const { login } = useAuth()

  const [formData, setFormData] = useState({
    username: "",
    password: ""
  })

  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    setError("")
  }

  const handleSubmit = (e) => {
    e.preventDefault()

    if (!formData.username.trim() || !formData.password.trim()) {
      setError("Username and password are required")
      return
    }

    setLoading(true)

    api.post("/auth/login", formData)
      .then((res) => {
        login(res.data.token, res.data.username)
        navigate("/")
      })
      .catch(() => {
        setError("Invalid username or password. Please try again.")
        setLoading(false)
      })
  }

  return (
    <div style={{
      minHeight: "100vh",
      background: "linear-gradient(135deg, #1B4F8A 0%, #0d2d52 50%, #061828 100%)",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "20px",
      fontFamily: "Arial, sans-serif"
    }}>
      <div style={{
        width: "100%",
        maxWidth: "420px"
      }}>

        {/* Logo and Title */}
        <div style={{ textAlign: "center", marginBottom: "32px" }}>
          <div style={{
            width: "64px",
            height: "64px",
            background: "rgba(255,255,255,0.15)",
            borderRadius: "16px",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            margin: "0 auto 16px",
            fontSize: "28px"
          }}>
            🔐
          </div>
          <h1 style={{
            color: "white",
            fontSize: "28px",
            fontWeight: "700",
            margin: "0 0 8px",
            letterSpacing: "-0.5px"
          }}>
            DPDP Act
          </h1>
          <p style={{
            color: "rgba(255,255,255,0.6)",
            fontSize: "14px",
            margin: 0
          }}>
            Consent Audit Trail — Compliance Portal
          </p>
        </div>

        {/* Card */}
        <div style={{
          background: "white",
          borderRadius: "16px",
          padding: "36px",
          boxShadow: "0 25px 50px rgba(0,0,0,0.3)"
        }}>
          <h2 style={{
            fontSize: "18px",
            fontWeight: "600",
            color: "#1a1a2e",
            margin: "0 0 24px"
          }}>
            Sign in to continue
          </h2>

          <form onSubmit={handleSubmit}>

            <div style={{ marginBottom: "16px" }}>
              <label style={{
                display: "block",
                fontSize: "13px",
                fontWeight: "600",
                color: "#4a5568",
                marginBottom: "6px"
              }}>
                Username
              </label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="Enter your username"
                style={{
                  width: "100%",
                  padding: "12px 14px",
                  border: "1.5px solid #e2e8f0",
                  borderRadius: "8px",
                  fontSize: "14px",
                  outline: "none",
                  boxSizing: "border-box",
                  transition: "border-color 0.2s",
                  fontFamily: "Arial, sans-serif"
                }}
                onFocus={e => e.target.style.borderColor = "#1B4F8A"}
                onBlur={e => e.target.style.borderColor = "#e2e8f0"}
              />
            </div>

            <div style={{ marginBottom: "20px" }}>
              <label style={{
                display: "block",
                fontSize: "13px",
                fontWeight: "600",
                color: "#4a5568",
                marginBottom: "6px"
              }}>
                Password
              </label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Enter your password"
                style={{
                  width: "100%",
                  padding: "12px 14px",
                  border: "1.5px solid #e2e8f0",
                  borderRadius: "8px",
                  fontSize: "14px",
                  outline: "none",
                  boxSizing: "border-box",
                  transition: "border-color 0.2s",
                  fontFamily: "Arial, sans-serif"
                }}
                onFocus={e => e.target.style.borderColor = "#1B4F8A"}
                onBlur={e => e.target.style.borderColor = "#e2e8f0"}
              />
            </div>

            {error && (
              <div style={{
                background: "#fff5f5",
                border: "1px solid #fed7d7",
                borderRadius: "8px",
                padding: "10px 14px",
                marginBottom: "16px",
                color: "#c53030",
                fontSize: "13px"
              }}>
                ⚠️ {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              style={{
                width: "100%",
                padding: "13px",
                background: loading ? "#a0aec0" : "#1B4F8A",
                color: "white",
                border: "none",
                borderRadius: "8px",
                fontSize: "15px",
                fontWeight: "600",
                cursor: loading ? "not-allowed" : "pointer",
                fontFamily: "Arial, sans-serif",
                transition: "background 0.2s"
              }}
              onMouseOver={e => { if (!loading) e.target.style.background = "#163f6e" }}
              onMouseOut={e => { if (!loading) e.target.style.background = "#1B4F8A" }}
            >
              {loading ? "Signing in..." : "Sign In"}
            </button>

          </form>

          {/* Credentials hint */}
          <div style={{
            marginTop: "20px",
            padding: "12px",
            background: "#f7fafc",
            borderRadius: "8px",
            fontSize: "12px",
            color: "#718096",
            textAlign: "center"
          }}>
            Demo: <strong>admin</strong> / <strong>admin123</strong>
          </div>

        </div>

        {/* Footer */}
        <p style={{
          textAlign: "center",
          color: "rgba(255,255,255,0.4)",
          fontSize: "12px",
          marginTop: "24px"
        }}>
          Digital Personal Data Protection Act 2023
        </p>

      </div>
    </div>
  )
}

export default Login