import { useEffect, useState, useRef } from "react"
import { useNavigate } from "react-router-dom"
import api from "../services/api"

const statusConfig = {
  GRANTED: { bg: "#f0fdf4", color: "#15803d", border: "#bbf7d0", dot: "#16a34a" },
  PENDING: { bg: "#fefce8", color: "#b45309", border: "#fde68a", dot: "#ca8a04" },
  REVOKED: { bg: "#fff1f2", color: "#be123c", border: "#fecdd3", dot: "#dc2626" },
  EXPIRED: { bg: "#f9fafb", color: "#4b5563", border: "#e5e7eb", dot: "#6b7280" },
}

function ConsentList() {
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalRecords, setTotalRecords] = useState(0)

  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState("")
  const [fromDate, setFromDate] = useState("")
  const [toDate, setToDate] = useState("")

  const navigate = useNavigate()
  const pageSize = 10
  const debounceRef = useRef(null)

  useEffect(() => {
    fetchRecords(currentPage)
  }, [currentPage, statusFilter, fromDate, toDate])

  const handleSearchChange = (e) => {
    const value = e.target.value
    setSearchQuery(value)
    setCurrentPage(0)
    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(() => {
      fetchRecords(0, value)
    }, 400)
  }

  const fetchRecords = (page, search = searchQuery) => {
    setLoading(true)
    const params = new URLSearchParams()
    params.append("page", page)
    params.append("size", pageSize)
    if (search) params.append("q", search)
    if (statusFilter) params.append("status", statusFilter)
    if (fromDate) params.append("from", fromDate)
    if (toDate) params.append("to", toDate)

    api.get(`/consent-records?${params.toString()}`)
      .then((res) => {
        setRecords(res.data.content || [])
        setTotalPages(res.data.totalPages || 0)
        setTotalRecords(res.data.totalElements || 0)
        setLoading(false)
      })
      .catch(() => {
        setError("Could not load consent records.")
        setLoading(false)
      })
  }

  const handleStatusChange = (e) => {
    setStatusFilter(e.target.value)
    setCurrentPage(0)
  }

  const handleFromDate = (e) => {
    setFromDate(e.target.value)
    setCurrentPage(0)
  }

  const handleToDate = (e) => {
    setToDate(e.target.value)
    setCurrentPage(0)
  }

  const clearFilters = () => {
    setSearchQuery("")
    setStatusFilter("")
    setFromDate("")
    setToDate("")
    setCurrentPage(0)
  }

  const handleExport = () => {
    api.get("/consent-records/export", { responseType: "blob" })
      .then(res => {
        const url = window.URL.createObjectURL(new Blob([res.data]))
        const a = document.createElement("a")
        a.href = url
        a.download = "consent-records.csv"
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        window.URL.revokeObjectURL(url)
      })
      .catch(() => alert("Export failed. Please try again."))
  }

  const anyFilterActive = searchQuery || statusFilter || fromDate || toDate

  return (
    <div style={{
      padding: "24px",
      fontFamily: "Arial, sans-serif",
      maxWidth: "1400px",
      margin: "0 auto"
    }}>

      {/* Header */}
      <div style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        marginBottom: "20px"
      }}>
        <div>
          <h1 style={{
            fontSize: "24px",
            fontWeight: "700",
            color: "#1a1a2e",
            margin: "0 0 4px"
          }}>
            Consent Records
          </h1>
          <p style={{ color: "#6b7280", fontSize: "14px", margin: 0 }}>
            {totalRecords} records found
          </p>
        </div>
        <button
          onClick={handleExport}
          style={{
            background: "#16a34a",
            color: "white",
            border: "none",
            borderRadius: "8px",
            padding: "10px 18px",
            fontSize: "13px",
            fontWeight: "600",
            cursor: "pointer",
            fontFamily: "Arial, sans-serif",
            display: "flex",
            alignItems: "center",
            gap: "6px"
          }}
          onMouseOver={e => e.currentTarget.style.background = "#15803d"}
          onMouseOut={e => e.currentTarget.style.background = "#16a34a"}
        >
          ⬇️ Export CSV
        </button>
      </div>

      {/* Search and Filter Bar */}
      <div style={{
        background: "white",
        borderRadius: "12px",
        padding: "16px 20px",
        marginBottom: "16px",
        display: "flex",
        flexWrap: "wrap",
        gap: "12px",
        alignItems: "flex-end",
        boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
        border: "1px solid #f0f0f0"
      }}>

        {/* Search */}
        <div style={{ flex: 1, minWidth: "200px" }}>
          <label style={{
            display: "block",
            fontSize: "11px",
            fontWeight: "600",
            color: "#6b7280",
            marginBottom: "6px",
            textTransform: "uppercase",
            letterSpacing: "0.5px"
          }}>
            Search
          </label>
          <input
            type="text"
            value={searchQuery}
            onChange={handleSearchChange}
            placeholder="Search by name, purpose, fiduciary..."
            style={{
              width: "100%",
              padding: "9px 12px",
              border: "1.5px solid #e5e7eb",
              borderRadius: "8px",
              fontSize: "13px",
              outline: "none",
              boxSizing: "border-box",
              fontFamily: "Arial, sans-serif"
            }}
            onFocus={e => e.target.style.borderColor = "#1B4F8A"}
            onBlur={e => e.target.style.borderColor = "#e5e7eb"}
          />
        </div>

        {/* Status */}
        <div>
          <label style={{
            display: "block",
            fontSize: "11px",
            fontWeight: "600",
            color: "#6b7280",
            marginBottom: "6px",
            textTransform: "uppercase",
            letterSpacing: "0.5px"
          }}>
            Status
          </label>
          <select
            value={statusFilter}
            onChange={handleStatusChange}
            style={{
              padding: "9px 12px",
              border: "1.5px solid #e5e7eb",
              borderRadius: "8px",
              fontSize: "13px",
              outline: "none",
              fontFamily: "Arial, sans-serif",
              background: "white",
              cursor: "pointer"
            }}
          >
            <option value="">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="GRANTED">Granted</option>
            <option value="REVOKED">Revoked</option>
            <option value="EXPIRED">Expired</option>
          </select>
        </div>

        {/* From Date */}
        <div>
          <label style={{
            display: "block",
            fontSize: "11px",
            fontWeight: "600",
            color: "#6b7280",
            marginBottom: "6px",
            textTransform: "uppercase",
            letterSpacing: "0.5px"
          }}>
            From Date
          </label>
          <input
            type="date"
            value={fromDate}
            onChange={handleFromDate}
            style={{
              padding: "9px 12px",
              border: "1.5px solid #e5e7eb",
              borderRadius: "8px",
              fontSize: "13px",
              outline: "none",
              fontFamily: "Arial, sans-serif"
            }}
          />
        </div>

        {/* To Date */}
        <div>
          <label style={{
            display: "block",
            fontSize: "11px",
            fontWeight: "600",
            color: "#6b7280",
            marginBottom: "6px",
            textTransform: "uppercase",
            letterSpacing: "0.5px"
          }}>
            To Date
          </label>
          <input
            type="date"
            value={toDate}
            onChange={handleToDate}
            style={{
              padding: "9px 12px",
              border: "1.5px solid #e5e7eb",
              borderRadius: "8px",
              fontSize: "13px",
              outline: "none",
              fontFamily: "Arial, sans-serif"
            }}
          />
        </div>

        {anyFilterActive && (
          <button
            onClick={clearFilters}
            style={{
              padding: "9px 16px",
              border: "1.5px solid #e5e7eb",
              borderRadius: "8px",
              fontSize: "13px",
              color: "#6b7280",
              background: "white",
              cursor: "pointer",
              fontFamily: "Arial, sans-serif"
            }}
          >
            ✕ Clear
          </button>
        )}
      </div>

      {/* Table */}
      {loading ? (
        <div style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "200px",
          color: "#6b7280",
          fontSize: "14px"
        }}>
          Loading records...
        </div>
      ) : error ? (
        <div style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          height: "200px",
          color: "#dc2626",
          fontSize: "14px"
        }}>
          {error}
        </div>
      ) : records.length === 0 ? (
        <div style={{
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          alignItems: "center",
          height: "200px",
          color: "#9ca3af",
          fontSize: "14px",
          gap: "8px"
        }}>
          <span style={{ fontSize: "32px" }}>📭</span>
          No consent records found.
        </div>
      ) : (
        <div style={{
          background: "white",
          borderRadius: "12px",
          overflow: "hidden",
          boxShadow: "0 1px 4px rgba(0,0,0,0.08)",
          border: "1px solid #f0f0f0"
        }}>
          <table style={{
            width: "100%",
            borderCollapse: "collapse",
            fontSize: "13px"
          }}>
            <thead>
              <tr style={{ background: "#f9fafb" }}>
                {["ID", "Principal Name", "Fiduciary", "Purpose", "Status", "Created At", "Actions"].map(h => (
                  <th key={h} style={{
                    padding: "12px 16px",
                    textAlign: "left",
                    color: "#6b7280",
                    fontSize: "11px",
                    textTransform: "uppercase",
                    letterSpacing: "0.5px",
                    fontWeight: "600",
                    borderBottom: "1px solid #f0f0f0"
                  }}>
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {records.map((record, index) => {
                const sc = statusConfig[record.consentStatus] || statusConfig.EXPIRED
                return (
                  <tr
                    key={record.id}
                    style={{
                      borderBottom: "1px solid #f9fafb",
                      background: index % 2 === 0 ? "white" : "#fafafa",
                      transition: "background 0.15s"
                    }}
                    onMouseOver={e => e.currentTarget.style.background = "#f0f7ff"}
                    onMouseOut={e => e.currentTarget.style.background = index % 2 === 0 ? "white" : "#fafafa"}
                  >
                    <td style={{ padding: "14px 16px", color: "#9ca3af", fontWeight: "600" }}>
                      #{record.id}
                    </td>
                    <td style={{ padding: "14px 16px", fontWeight: "600", color: "#1a1a2e" }}>
                      {record.dataPrincipalName}
                    </td>
                    <td style={{ padding: "14px 16px", color: "#4b5563" }}>
                      {record.dataFiduciaryName}
                    </td>
                    <td style={{
                      padding: "14px 16px",
                      color: "#6b7280",
                      maxWidth: "200px",
                      overflow: "hidden",
                      textOverflow: "ellipsis",
                      whiteSpace: "nowrap"
                    }}>
                      {record.purpose}
                    </td>
                    <td style={{ padding: "14px 16px" }}>
                      <span style={{
                        background: sc.bg,
                        color: sc.color,
                        border: `1px solid ${sc.border}`,
                        padding: "4px 10px",
                        borderRadius: "20px",
                        fontSize: "11px",
                        fontWeight: "700",
                        display: "inline-flex",
                        alignItems: "center",
                        gap: "5px"
                      }}>
                        <span style={{
                          width: "6px",
                          height: "6px",
                          borderRadius: "50%",
                          background: sc.dot,
                          display: "inline-block"
                        }} />
                        {record.consentStatus}
                      </span>
                    </td>
                    <td style={{ padding: "14px 16px", color: "#6b7280" }}>
                      {new Date(record.createdAt).toLocaleDateString("en-IN")}
                    </td>
                    <td style={{ padding: "14px 16px" }}>
                      <div style={{ display: "flex", gap: "8px" }}>
                        <button
                          onClick={() => navigate(`/detail/${record.id}`)}
                          style={{
                            padding: "5px 12px",
                            border: "1px solid #e5e7eb",
                            borderRadius: "6px",
                            fontSize: "12px",
                            fontWeight: "600",
                            color: "#4b5563",
                            background: "white",
                            cursor: "pointer",
                            fontFamily: "Arial, sans-serif"
                          }}
                          onMouseOver={e => e.currentTarget.style.background = "#f9fafb"}
                          onMouseOut={e => e.currentTarget.style.background = "white"}
                        >
                          View
                        </button>
                        <button
                          onClick={() => navigate(`/edit/${record.id}`)}
                          style={{
                            padding: "5px 12px",
                            border: "1px solid #bfdbfe",
                            borderRadius: "6px",
                            fontSize: "12px",
                            fontWeight: "600",
                            color: "#1d4ed8",
                            background: "#eff6ff",
                            cursor: "pointer",
                            fontFamily: "Arial, sans-serif"
                          }}
                          onMouseOver={e => e.currentTarget.style.background = "#dbeafe"}
                          onMouseOut={e => e.currentTarget.style.background = "#eff6ff"}
                        >
                          Edit
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div style={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          gap: "8px",
          marginTop: "20px"
        }}>
          <button
            onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 0))}
            disabled={currentPage === 0}
            style={{
              padding: "7px 14px",
              border: "1px solid #e5e7eb",
              borderRadius: "8px",
              fontSize: "13px",
              color: currentPage === 0 ? "#d1d5db" : "#4b5563",
              background: "white",
              cursor: currentPage === 0 ? "not-allowed" : "pointer",
              fontFamily: "Arial, sans-serif"
            }}
          >
            ← Previous
          </button>
          {[...Array(totalPages)].map((_, index) => (
            <button
              key={index}
              onClick={() => setCurrentPage(index)}
              style={{
                padding: "7px 12px",
                border: currentPage === index ? "none" : "1px solid #e5e7eb",
                borderRadius: "8px",
                fontSize: "13px",
                fontWeight: currentPage === index ? "700" : "500",
                color: currentPage === index ? "white" : "#4b5563",
                background: currentPage === index ? "#1B4F8A" : "white",
                cursor: "pointer",
                fontFamily: "Arial, sans-serif"
              }}
            >
              {index + 1}
            </button>
          ))}
          <button
            onClick={() => setCurrentPage((prev) => Math.min(prev + 1, totalPages - 1))}
            disabled={currentPage === totalPages - 1}
            style={{
              padding: "7px 14px",
              border: "1px solid #e5e7eb",
              borderRadius: "8px",
              fontSize: "13px",
              color: currentPage === totalPages - 1 ? "#d1d5db" : "#4b5563",
              background: "white",
              cursor: currentPage === totalPages - 1 ? "not-allowed" : "pointer",
              fontFamily: "Arial, sans-serif"
            }}
          >
            Next →
          </button>
        </div>
      )}
    </div>
  )
}

export default ConsentList