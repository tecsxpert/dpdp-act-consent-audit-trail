import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, Cell
} from "recharts"
import api from "../services/api"

function Dashboard() {
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

  const chartData = stats ? [
    { name: "Granted", value: stats.granted, color: "#16a34a" },
    { name: "Pending", value: stats.pending, color: "#ca8a04" },
    { name: "Revoked", value: stats.revoked, color: "#dc2626" },
    { name: "Expired", value: stats.expired, color: "#6b7280" },
  ] : []

  const kpiCards = stats ? [
    {
      label: "Total Records",
      value: stats.total,
      icon: "📋",
      bg: "#eff6ff",
      border: "#bfdbfe",
      color: "#1d4ed8",
      desc: "All consent records"
    },
    {
      label: "Granted",
      value: stats.granted,
      icon: "✅",
      bg: "#f0fdf4",
      border: "#bbf7d0",
      color: "#15803d",
      desc: "Active consents"
    },
    {
      label: "Pending",
      value: stats.pending,
      icon: "⏳",
      bg: "#fefce8",
      border: "#fde68a",
      color: "#b45309",
      desc: "Awaiting decision"
    },
    {
      label: "Revoked",
      value: stats.revoked,
      icon: "🚫",
      bg: "#fff1f2",
      border: "#fecdd3",
      color: "#be123c",
      desc: "Withdrawn consents"
    },
    {
      label: "Expired",
      value: stats.expired,
      icon: "⌛",
      bg: "#f9fafb",
      border: "#e5e7eb",
      color: "#4b5563",
      desc: "Past expiry date"
    },
    {
      label: "Compliance Rate",
      value: stats.total > 0
        ? Math.round((stats.granted / stats.total) * 100) + "%"
        : "0%",
      icon: "📊",
      bg: "#f5f3ff",
      border: "#ddd6fe",
      color: "#6d28d9",
      desc: "Granted vs total"
    },
  ] : []

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
        Loading dashboard...
      </div>
    )
  }

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
            Dashboard
          </h1>
          <p style={{ color: "#6b7280", fontSize: "14px", margin: 0 }}>
            DPDP Act Consent Overview
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

      {/* KPI Cards */}
      <div style={{
        display: "grid",
        gridTemplateColumns: "repeat(3, 1fr)",
        gap: "16px",
        marginBottom: "24px"
      }}>
        {kpiCards.map((card) => (
          <div
            key={card.label}
            style={{
              background: card.bg,
              border: `1px solid ${card.border}`,
              borderRadius: "12px",
              padding: "20px",
              display: "flex",
              alignItems: "center",
              gap: "16px"
            }}
          >
            <div style={{
              fontSize: "28px",
              width: "48px",
              height: "48px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              background: "white",
              borderRadius: "10px",
              boxShadow: "0 1px 4px rgba(0,0,0,0.1)"
            }}>
              {card.icon}
            </div>
            <div>
              <p style={{
                fontSize: "13px",
                color: "#6b7280",
                margin: "0 0 4px"
              }}>
                {card.label}
              </p>
              <p style={{
                fontSize: "28px",
                fontWeight: "700",
                color: card.color,
                margin: "0 0 2px"
              }}>
                {card.value}
              </p>
              <p style={{
                fontSize: "11px",
                color: "#9ca3af",
                margin: 0
              }}>
                {card.desc}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Bar Chart */}
      <div style={{
        background: "white",
        borderRadius: "12px",
        padding: "24px",
        boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
        border: "1px solid #f0f0f0"
      }}>
        <div style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          marginBottom: "20px"
        }}>
          <h2 style={{
            fontSize: "16px",
            fontWeight: "600",
            color: "#1a1a2e",
            margin: 0
          }}>
            Consent Status Breakdown
          </h2>
          <span style={{
            background: "#eff6ff",
            color: "#1d4ed8",
            fontSize: "12px",
            fontWeight: "600",
            padding: "4px 10px",
            borderRadius: "20px"
          }}>
            {stats.total} Total
          </span>
        </div>
        <ResponsiveContainer width="100%" height={300}>
          <BarChart
            data={chartData}
            margin={{ top: 5, right: 20, left: 0, bottom: 5 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis dataKey="name" tick={{ fontSize: 13, fontFamily: "Arial" }} />
            <YAxis allowDecimals={false} tick={{ fontSize: 13, fontFamily: "Arial" }} />
            <Tooltip
              contentStyle={{
                borderRadius: "8px",
                border: "1px solid #e5e7eb",
                fontFamily: "Arial"
              }}
            />
            <Bar dataKey="value" radius={[6, 6, 0, 0]}>
              {chartData.map((entry, index) => (
                <Cell key={index} fill={entry.color} />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

    </div>
  )
}

export default Dashboard