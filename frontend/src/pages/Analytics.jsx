import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, PieChart, Pie,
  Cell, Legend
} from "recharts"
import api from "../services/api"

function Analytics() {
  const navigate = useNavigate()
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get("/consent-records/stats")
      .then((res) => {
        setStats(res.data)
        setLoading(false)
      })
      .catch(() => {
        setStats({ total: 0, granted: 0, revoked: 0, pending: 0, expired: 0 })
        setLoading(false)
      })
  }, [])

  if (loading) {
    return (
      <div style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "300px",
        fontFamily: "Arial, sans-serif",
        color: "#6b7280"
      }}>
        Loading analytics...
      </div>
    )
  }

  const pieData = [
    { name: "Granted", value: stats.granted, color: "#16a34a" },
    { name: "Pending", value: stats.pending, color: "#ca8a04" },
    { name: "Revoked", value: stats.revoked, color: "#dc2626" },
    { name: "Expired", value: stats.expired, color: "#6b7280" },
  ].filter(d => d.value > 0)

  const lineData = [
    { period: "Granted", count: stats.granted },
    { period: "Pending", count: stats.pending },
    { period: "Revoked", count: stats.revoked },
    { period: "Expired", count: stats.expired },
  ]

  const complianceRate = stats.total > 0
    ? Math.round((stats.granted / stats.total) * 100)
    : 0

  const tableRows = [
    { label: "Granted", value: stats.granted, color: "#15803d", bg: "#f0fdf4" },
    { label: "Pending", value: stats.pending, color: "#b45309", bg: "#fefce8" },
    { label: "Revoked", value: stats.revoked, color: "#be123c", bg: "#fff1f2" },
    { label: "Expired", value: stats.expired, color: "#4b5563", bg: "#f9fafb" },
  ]

  return (
    <div style={{
      padding: "24px",
      fontFamily: "Arial, sans-serif",
      maxWidth: "1200px",
      margin: "0 auto"
    }}>

      {/* Header */}
      <div style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "24px"
      }}>
        <div>
          <h1 style={{
            fontSize: "24px",
            fontWeight: "700",
            color: "#1a1a2e",
            margin: "0 0 4px"
          }}>
            Analytics
          </h1>
          <p style={{ color: "#6b7280", fontSize: "14px", margin: 0 }}>
            DPDP Act Compliance Insights
          </p>
        </div>
        <button
          onClick={() => navigate("/")}
          style={{
            background: "#1B4F8A",
            color: "white",
            border: "none",
            borderRadius: "8px",
            padding: "8px 16px",
            fontSize: "13px",
            fontWeight: "600",
            cursor: "pointer",
            fontFamily: "Arial, sans-serif"
          }}
        >
          View All Records →
        </button>
      </div>

      {/* Compliance Rate Banner */}
      <div style={{
        background: "linear-gradient(135deg, #1B4F8A, #163f6e)",
        borderRadius: "16px",
        padding: "28px 32px",
        marginBottom: "24px",
        display: "flex",
        alignItems: "center",
        gap: "32px",
        color: "white"
      }}>
        <div style={{ textAlign: "center", minWidth: "120px" }}>
          <div style={{
            fontSize: "56px",
            fontWeight: "800",
            lineHeight: 1,
            color: complianceRate >= 70 ? "#4ade80" : complianceRate >= 50 ? "#fbbf24" : "#f87171"
          }}>
            {complianceRate}%
          </div>
          <div style={{
            fontSize: "13px",
            color: "rgba(255,255,255,0.7)",
            marginTop: "6px",
            fontWeight: "600"
          }}>
            Compliance Rate
          </div>
        </div>
        <div style={{
          borderLeft: "1px solid rgba(255,255,255,0.2)",
          paddingLeft: "32px"
        }}>
          <div style={{ fontSize: "16px", fontWeight: "600", marginBottom: "8px" }}>
            DPDP Act 2023 — Compliance Status
          </div>
          <div style={{ fontSize: "14px", color: "rgba(255,255,255,0.7)", lineHeight: 1.6 }}>
            {stats.granted} out of {stats.total} consent records are currently
            in GRANTED status. {complianceRate >= 70
              ? "Your organization is maintaining good compliance standards."
              : "Consider reviewing pending and revoked consents to improve compliance."}
          </div>
          <div style={{
            display: "flex",
            gap: "16px",
            marginTop: "12px"
          }}>
            {[
              { label: "Granted", value: stats.granted, color: "#4ade80" },
              { label: "Pending", value: stats.pending, color: "#fbbf24" },
              { label: "Revoked", value: stats.revoked, color: "#f87171" },
              { label: "Expired", value: stats.expired, color: "#9ca3af" },
            ].map(item => (
              <div key={item.label} style={{ textAlign: "center" }}>
                <div style={{
                  fontSize: "20px",
                  fontWeight: "700",
                  color: item.color
                }}>
                  {item.value}
                </div>
                <div style={{
                  fontSize: "11px",
                  color: "rgba(255,255,255,0.6)"
                }}>
                  {item.label}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Charts Row */}
      <div style={{
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: "16px",
        marginBottom: "16px"
      }}>

        {/* Pie Chart */}
        <div style={{
          background: "white",
          borderRadius: "12px",
          padding: "24px",
          boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
          border: "1px solid #f0f0f0"
        }}>
          <h2 style={{
            fontSize: "16px",
            fontWeight: "600",
            color: "#1a1a2e",
            margin: "0 0 16px"
          }}>
            Consent Distribution
          </h2>
          <ResponsiveContainer width="100%" height={280}>
            <PieChart>
              <Pie
                data={pieData}
                cx="50%"
                cy="50%"
                outerRadius={100}
                dataKey="value"
                label={({ name, percent }) =>
                  `${name} ${(percent * 100).toFixed(0)}%`
                }
              >
                {pieData.map((entry, index) => (
                  <Cell key={index} fill={entry.color} />
                ))}
              </Pie>
              <Legend />
              <Tooltip
                contentStyle={{
                  borderRadius: "8px",
                  fontFamily: "Arial"
                }}
              />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Line Chart */}
        <div style={{
          background: "white",
          borderRadius: "12px",
          padding: "24px",
          boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
          border: "1px solid #f0f0f0"
        }}>
          <h2 style={{
            fontSize: "16px",
            fontWeight: "600",
            color: "#1a1a2e",
            margin: "0 0 16px"
          }}>
            Status Overview
          </h2>
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={lineData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="period" tick={{ fontSize: 12, fontFamily: "Arial" }} />
              <YAxis allowDecimals={false} tick={{ fontSize: 12, fontFamily: "Arial" }} />
              <Tooltip contentStyle={{ borderRadius: "8px", fontFamily: "Arial" }} />
              <Line
                type="monotone"
                dataKey="count"
                stroke="#1B4F8A"
                strokeWidth={3}
                dot={{ fill: "#1B4F8A", r: 6 }}
                activeDot={{ r: 8 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

      </div>

      {/* Summary Table */}
      <div style={{
        background: "white",
        borderRadius: "12px",
        padding: "24px",
        boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
        border: "1px solid #f0f0f0"
      }}>
        <h2 style={{
          fontSize: "16px",
          fontWeight: "600",
          color: "#1a1a2e",
          margin: "0 0 16px"
        }}>
          Summary
        </h2>
        <table style={{ width: "100%", borderCollapse: "collapse", fontSize: "14px" }}>
          <thead>
            <tr style={{ background: "#f9fafb" }}>
              <th style={{ padding: "12px 16px", textAlign: "left", color: "#6b7280", fontSize: "12px", textTransform: "uppercase", fontWeight: "600" }}>Status</th>
              <th style={{ padding: "12px 16px", textAlign: "left", color: "#6b7280", fontSize: "12px", textTransform: "uppercase", fontWeight: "600" }}>Count</th>
              <th style={{ padding: "12px 16px", textAlign: "left", color: "#6b7280", fontSize: "12px", textTransform: "uppercase", fontWeight: "600" }}>Percentage</th>
              <th style={{ padding: "12px 16px", textAlign: "left", color: "#6b7280", fontSize: "12px", textTransform: "uppercase", fontWeight: "600" }}>Progress</th>
            </tr>
          </thead>
          <tbody>
            {tableRows.map((row) => (
              <tr key={row.label} style={{ borderBottom: "1px solid #f0f0f0" }}>
                <td style={{ padding: "12px 16px" }}>
                  <span style={{
                    background: row.bg,
                    color: row.color,
                    padding: "4px 10px",
                    borderRadius: "20px",
                    fontSize: "12px",
                    fontWeight: "600"
                  }}>
                    {row.label}
                  </span>
                </td>
                <td style={{ padding: "12px 16px", fontWeight: "600", color: "#1a1a2e" }}>
                  {row.value}
                </td>
                <td style={{ padding: "12px 16px", color: "#6b7280" }}>
                  {stats.total > 0 ? Math.round((row.value / stats.total) * 100) : 0}%
                </td>
                <td style={{ padding: "12px 16px", width: "200px" }}>
                  <div style={{
                    background: "#f0f0f0",
                    borderRadius: "4px",
                    height: "8px",
                    overflow: "hidden"
                  }}>
                    <div style={{
                      background: row.color,
                      height: "100%",
                      width: `${stats.total > 0 ? Math.round((row.value / stats.total) * 100) : 0}%`,
                      borderRadius: "4px",
                      transition: "width 0.3s"
                    }} />
                  </div>
                </td>
              </tr>
            ))}
            <tr style={{ background: "#f9fafb", fontWeight: "700" }}>
              <td style={{ padding: "12px 16px", color: "#1a1a2e" }}>Total</td>
              <td style={{ padding: "12px 16px", color: "#1a1a2e" }}>{stats.total}</td>
              <td style={{ padding: "12px 16px", color: "#1a1a2e" }}>100%</td>
              <td style={{ padding: "12px 16px" }}></td>
            </tr>
          </tbody>
        </table>
      </div>

    </div>
  )
}

export default Analytics