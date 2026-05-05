import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import api from "../services/api"

const statusConfig = {
  GRANTED: { bg: "#f0fdf4", color: "#15803d", border: "#bbf7d0" },
  PENDING: { bg: "#fefce8", color: "#b45309", border: "#fde68a" },
  REVOKED: { bg: "#fff1f2", color: "#be123c", border: "#fecdd3" },
  EXPIRED: { bg: "#f9fafb", color: "#4b5563", border: "#e5e7eb" },
}

function ConsentDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState(null)
  const [loading, setLoading] = useState(true)
  const [deleting, setDeleting] = useState(false)
  const [recommendations, setRecommendations] = useState(null)
  const [loadingRec, setLoadingRec] = useState(false)
  const [report, setReport] = useState(null)
  const [loadingReport, setLoadingReport] = useState(false)

  const fetchRecommendations = () => {
    setLoadingRec(true)
    fetch("http://localhost:5000/recommend", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        dataPrincipalName: record?.dataPrincipalName,
        dataFiduciaryName: record?.dataFiduciaryName,
        purpose: record?.purpose,
        dataCategories: record?.dataCategories,
        consentStatus: record?.consentStatus
      })
    })
      .then(res => res.json())
      .then(data => {
        setRecommendations(data)
        setLoadingRec(false)
      })
      .catch(() => setLoadingRec(false))
  }

  const fetchReport = () => {
    setLoadingReport(true)
    fetch("http://localhost:5000/generate-report", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        stats: { total: 1 },
        records: [record]
      })
    })
      .then(res => res.json())
      .then(data => {
        setReport(data)
        setLoadingReport(false)
      })
      .catch(() => setLoadingReport(false))
  }

  useEffect(() => {
    api.get(`/consent-records/${id}`)
      .then((res) => {
        setRecord(res.data)
        setLoading(false)
      })
      .catch(() => {
        setLoading(false)
      })
  }, [id])

  const handleDelete = () => {
    if (!window.confirm("Are you sure you want to delete this record?")) return
    setDeleting(true)
    api.delete(`/consent-records/${id}`)
      .then(() => {
        navigate("/")
      })
      .catch(() => {
        alert("Failed to delete record.")
        setDeleting(false)
      })
  }

  const getScoreBadge = (score) => {
    if (!score) return null
    if (score >= 75) return { bg: "#f0fdf4", color: "#15803d", border: "#bbf7d0" }
    if (score >= 50) return { bg: "#fefce8", color: "#b45309", border: "#fde68a" }
    return { bg: "#fff1f2", color: "#be123c", border: "#fecdd3" }
  }

  const getStatusBadge = (status) => {
    return statusConfig[status] || { bg: "#f9fafb", color: "#4b5563", border: "#e5e7eb" }
  }

  if (loading) {
    return (
      <div style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "260px",
        fontFamily: "Arial, sans-serif",
        color: "#6b7280"
      }}>
        Loading record...
      </div>
    )
  }

  if (!record) {
    return (
      <div style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "260px",
        fontFamily: "Arial, sans-serif",
        color: "#dc2626"
      }}>
        Record not found.
      </div>
    )
  }

  const pageStyle = {
    padding: "24px",
    fontFamily: "Arial, sans-serif",
    maxWidth: "1100px",
    margin: "0 auto"
  }

  const cardStyle = {
    background: "white",
    borderRadius: "12px",
    padding: "24px",
    boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
    border: "1px solid #f0f0f0"
  }

  const sectionTitleStyle = {
    fontSize: "12px",
    fontWeight: "700",
    color: "#6b7280",
    textTransform: "uppercase",
    letterSpacing: "0.6px",
    marginBottom: "12px"
  }

  const labelStyle = {
    fontSize: "11px",
    fontWeight: "600",
    color: "#9ca3af",
    marginBottom: "4px",
    textTransform: "uppercase",
    letterSpacing: "0.5px"
  }

  const valueStyle = {
    fontSize: "14px",
    color: "#111827",
    fontWeight: "600"
  }

  const primaryButtonStyle = {
    background: "#1B4F8A",
    color: "white",
    border: "none",
    borderRadius: "8px",
    padding: "10px 16px",
    fontSize: "13px",
    fontWeight: "600",
    cursor: "pointer",
    fontFamily: "Arial, sans-serif"
  }

  const secondaryButtonStyle = {
    background: "white",
    color: "#1B4F8A",
    border: "1px solid #cbd5f5",
    borderRadius: "8px",
    padding: "10px 16px",
    fontSize: "13px",
    fontWeight: "600",
    cursor: "pointer",
    fontFamily: "Arial, sans-serif"
  }

  const dangerButtonStyle = {
    background: "#ef4444",
    color: "white",
    border: "none",
    borderRadius: "8px",
    padding: "10px 16px",
    fontSize: "13px",
    fontWeight: "600",
    cursor: deleting ? "not-allowed" : "pointer",
    opacity: deleting ? 0.7 : 1,
    fontFamily: "Arial, sans-serif"
  }

  return (
    <div style={pageStyle}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" }}>
        <div>
          <h1 style={{
            fontSize: "24px",
            fontWeight: "700",
            color: "#1a1a2e",
            margin: "0 0 4px"
          }}>
            Consent Record #{record.id}
          </h1>
          <p style={{ color: "#6b7280", fontSize: "14px", margin: 0 }}>
            Detailed consent information and AI insights
          </p>
        </div>
        <div style={{ display: "flex", gap: "10px" }}>
          <button
            onClick={() => navigate(`/edit/${record.id}`)}
            style={primaryButtonStyle}
          >
            Edit
          </button>
          <button
            onClick={handleDelete}
            disabled={deleting}
            style={dangerButtonStyle}
          >
            {deleting ? "Deleting..." : "Delete"}
          </button>
          <button
            onClick={() => navigate("/")}
            style={secondaryButtonStyle}
          >
            Back
          </button>
        </div>
      </div>

      <div style={{ display: "grid", gap: "16px" }}>
        <div style={cardStyle}>
          <div style={{ display: "flex", flexWrap: "wrap", gap: "10px", alignItems: "center" }}>
            <span style={{
              background: getStatusBadge(record.consentStatus).bg,
              color: getStatusBadge(record.consentStatus).color,
              border: `1px solid ${getStatusBadge(record.consentStatus).border}`,
              borderRadius: "999px",
              padding: "6px 12px",
              fontSize: "12px",
              fontWeight: "700",
              letterSpacing: "0.4px"
            }}>
              {record.consentStatus}
            </span>
            {record.aiScore && (() => {
              const scoreStyle = getScoreBadge(record.aiScore)
              return (
                <span style={{
                  background: scoreStyle.bg,
                  color: scoreStyle.color,
                  border: `1px solid ${scoreStyle.border}`,
                  borderRadius: "999px",
                  padding: "6px 12px",
                  fontSize: "12px",
                  fontWeight: "700"
                }}>
                  AI Score: {record.aiScore}/100
                </span>
              )
            })()}
            {record.isFallback && (
              <span style={{
                background: "#f9fafb",
                color: "#6b7280",
                border: "1px solid #e5e7eb",
                borderRadius: "999px",
                padding: "6px 12px",
                fontSize: "12px",
                fontWeight: "700"
              }}>
                Fallback Response
              </span>
            )}
          </div>
        </div>

        <div style={cardStyle}>
          <div style={sectionTitleStyle}>Data Principal (Citizen)</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "16px" }}>
            <div>
              <div style={labelStyle}>ID</div>
              <div style={valueStyle}>{record.dataPrincipalId}</div>
            </div>
            <div>
              <div style={labelStyle}>Name</div>
              <div style={valueStyle}>{record.dataPrincipalName}</div>
            </div>
            <div>
              <div style={labelStyle}>Email</div>
              <div style={valueStyle}>{record.dataPrincipalEmail}</div>
            </div>
          </div>
        </div>

        <div style={cardStyle}>
          <div style={sectionTitleStyle}>Data Fiduciary (Organisation)</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: "16px" }}>
            <div>
              <div style={labelStyle}>ID</div>
              <div style={valueStyle}>{record.dataFiduciaryId}</div>
            </div>
            <div>
              <div style={labelStyle}>Name</div>
              <div style={valueStyle}>{record.dataFiduciaryName}</div>
            </div>
          </div>
        </div>

        <div style={cardStyle}>
          <div style={sectionTitleStyle}>Consent Details</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: "16px" }}>
            <div style={{ gridColumn: "span 2" }}>
              <div style={labelStyle}>Purpose</div>
              <div style={{ fontSize: "14px", color: "#111827" }}>{record.purpose}</div>
            </div>
            <div style={{ gridColumn: "span 2" }}>
              <div style={labelStyle}>Data Categories</div>
              <div style={{ fontSize: "14px", color: "#111827" }}>{record.dataCategories}</div>
            </div>
            <div>
              <div style={labelStyle}>Consent Date</div>
              <div style={valueStyle}>
                {record.consentDate
                  ? new Date(record.consentDate).toLocaleDateString()
                  : "—"}
              </div>
            </div>
            <div>
              <div style={labelStyle}>Expiry Date</div>
              <div style={valueStyle}>
                {record.expiryDate
                  ? new Date(record.expiryDate).toLocaleDateString()
                  : "—"}
              </div>
            </div>
          </div>
        </div>

        {record.aiDescription && (
          <div style={cardStyle}>
            <div style={sectionTitleStyle}>AI Description</div>
            <div style={{
              background: "#f9fafb",
              border: "1px solid #e5e7eb",
              borderRadius: "10px",
              padding: "14px",
              fontSize: "14px",
              color: "#374151",
              lineHeight: 1.6
            }}>
              {record.aiDescription}
            </div>
          </div>
        )}

        <div style={cardStyle}>
          <div style={sectionTitleStyle}>Timestamps</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: "16px" }}>
            <div>
              <div style={labelStyle}>Created At</div>
              <div style={valueStyle}>{new Date(record.createdAt).toLocaleString()}</div>
            </div>
            <div>
              <div style={labelStyle}>Last Updated</div>
              <div style={valueStyle}>{new Date(record.updatedAt).toLocaleString()}</div>
            </div>
          </div>
        </div>

        <div style={cardStyle}>
          <div style={sectionTitleStyle}>AI Actions</div>
          <div style={{ display: "flex", gap: "10px", flexWrap: "wrap", marginBottom: "16px" }}>
            <button
              onClick={fetchRecommendations}
              disabled={loadingRec}
              style={{
                background: loadingRec ? "#c4b5fd" : "#7c3aed",
                color: "white",
                border: "none",
                borderRadius: "8px",
                padding: "10px 14px",
                fontSize: "13px",
                fontWeight: "600",
                cursor: loadingRec ? "not-allowed" : "pointer",
                fontFamily: "Arial, sans-serif"
              }}
            >
              {loadingRec ? "Loading..." : "Get Recommendations"}
            </button>
            <button
              onClick={fetchReport}
              disabled={loadingReport}
              style={{
                background: loadingReport ? "#c7d2fe" : "#4338ca",
                color: "white",
                border: "none",
                borderRadius: "8px",
                padding: "10px 14px",
                fontSize: "13px",
                fontWeight: "600",
                cursor: loadingReport ? "not-allowed" : "pointer",
                fontFamily: "Arial, sans-serif"
              }}
            >
              {loadingReport ? "Generating..." : "Generate Report"}
            </button>
          </div>

          {recommendations && (
            <div style={{ marginBottom: "16px" }}>
              <div style={{
                fontSize: "13px",
                fontWeight: "700",
                color: "#374151",
                marginBottom: "10px"
              }}>
                Recommendations
              </div>
              <div style={{ display: "grid", gap: "10px" }}>
                {recommendations.map((rec, index) => (
                  <div key={index} style={{
                    border: "1px solid #e9d5ff",
                    background: "#faf5ff",
                    borderRadius: "10px",
                    padding: "12px"
                  }}>
                    <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "6px" }}>
                      <span style={{
                        fontSize: "12px",
                        fontWeight: "700",
                        color: "#7c3aed"
                      }}>
                        {rec.action_type}
                      </span>
                      <span style={{
                        fontSize: "11px",
                        fontWeight: "700",
                        padding: "4px 8px",
                        borderRadius: "999px",
                        background: rec.priority === "HIGH"
                          ? "#fee2e2"
                          : rec.priority === "MEDIUM"
                            ? "#fef9c3"
                            : "#dcfce7",
                        color: rec.priority === "HIGH"
                          ? "#b91c1c"
                          : rec.priority === "MEDIUM"
                            ? "#b45309"
                            : "#15803d"
                      }}>
                        {rec.priority}
                      </span>
                    </div>
                    <div style={{ fontSize: "13px", color: "#374151" }}>{rec.description}</div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {report && (
            <div style={{
              border: "1px solid #c7d2fe",
              background: "#eef2ff",
              borderRadius: "12px",
              padding: "16px"
            }}>
              <div style={{
                fontSize: "14px",
                fontWeight: "700",
                color: "#4338ca",
                marginBottom: "8px"
              }}>
                {report.title}
              </div>
              <div style={{ fontSize: "13px", color: "#374151", marginBottom: "12px" }}>
                {report.summary}
              </div>
              {report.key_items && report.key_items.length > 0 && (
                <div style={{ marginBottom: "12px" }}>
                  <div style={{
                    fontSize: "11px",
                    fontWeight: "700",
                    color: "#6b7280",
                    textTransform: "uppercase",
                    letterSpacing: "0.5px",
                    marginBottom: "6px"
                  }}>
                    Key Findings
                  </div>
                  <ul style={{ margin: 0, paddingLeft: "18px", color: "#374151", fontSize: "13px" }}>
                    {report.key_items.map((item, i) => (
                      <li key={i} style={{ marginBottom: "4px" }}>{item}</li>
                    ))}
                  </ul>
                </div>
              )}
              {report.recommendations && report.recommendations.length > 0 && (
                <div>
                  <div style={{
                    fontSize: "11px",
                    fontWeight: "700",
                    color: "#6b7280",
                    textTransform: "uppercase",
                    letterSpacing: "0.5px",
                    marginBottom: "6px"
                  }}>
                    Recommendations
                  </div>
                  <ul style={{ margin: 0, paddingLeft: "18px", color: "#374151", fontSize: "13px" }}>
                    {report.recommendations.map((rec, i) => (
                      <li key={i} style={{ marginBottom: "4px" }}>{rec}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}


export default ConsentDetail