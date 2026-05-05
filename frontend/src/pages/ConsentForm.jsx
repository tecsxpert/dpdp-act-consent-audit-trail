import { useState, useEffect } from "react"
import { useNavigate, useParams } from "react-router-dom"
import api from "../services/api"

function ConsentForm() {
  const navigate = useNavigate()
  const { id } = useParams()

  const isEditing = Boolean(id)

  const [formData, setFormData] = useState({
    dataPrincipalId: "",
    dataPrincipalName: "",
    dataPrincipalEmail: "",
    dataFiduciaryId: "",
    dataFiduciaryName: "",
    purpose: "",
    dataCategories: "",
    consentStatus: "PENDING",
    consentDate: "",
    expiryDate: "",
  })

  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)
  const [loadingRecord, setLoadingRecord] = useState(false)

  useEffect(() => {
    if (isEditing) {
      setLoadingRecord(true)
      api.get(`/consent-records/${id}`)
        .then((res) => {
          const r = res.data
          setFormData({
            dataPrincipalId: r.dataPrincipalId || "",
            dataPrincipalName: r.dataPrincipalName || "",
            dataPrincipalEmail: r.dataPrincipalEmail || "",
            dataFiduciaryId: r.dataFiduciaryId || "",
            dataFiduciaryName: r.dataFiduciaryName || "",
            purpose: r.purpose || "",
            dataCategories: r.dataCategories || "",
            consentStatus: r.consentStatus || "PENDING",
            consentDate: r.consentDate ? r.consentDate.slice(0, 10) : "",
            expiryDate: r.expiryDate ? r.expiryDate.slice(0, 10) : "",
          })
          setLoadingRecord(false)
        })
        .catch(() => {
          alert("Could not load record for editing.")
          setLoadingRecord(false)
        })
    }
  }, [id, isEditing])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }))
    }
  }

  const validate = () => {
    const newErrors = {}

    if (!formData.dataPrincipalId.trim())
      newErrors.dataPrincipalId = "Principal ID is required"

    if (!formData.dataPrincipalName.trim())
      newErrors.dataPrincipalName = "Principal Name is required"

    if (!formData.dataPrincipalEmail.trim()) {
      newErrors.dataPrincipalEmail = "Email is required"
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.dataPrincipalEmail)) {
      newErrors.dataPrincipalEmail = "Enter a valid email address"
    }

    if (!formData.dataFiduciaryId.trim())
      newErrors.dataFiduciaryId = "Fiduciary ID is required"

    if (!formData.dataFiduciaryName.trim())
      newErrors.dataFiduciaryName = "Fiduciary Name is required"

    if (!formData.purpose.trim())
      newErrors.purpose = "Purpose is required"

    if (!formData.dataCategories.trim())
      newErrors.dataCategories = "Data Categories is required"

    if (
      formData.consentDate &&
      formData.expiryDate &&
      formData.expiryDate < formData.consentDate
    ) {
      newErrors.expiryDate = "Expiry date cannot be before consent date"
    }

    return newErrors
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    const validationErrors = validate()

    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors)
      return
    }

    setSubmitting(true)

    // convert date strings to LocalDateTime format for backend
    const payload = {
      ...formData,
      consentDate: formData.consentDate ? formData.consentDate + "T00:00:00" : null,
      expiryDate: formData.expiryDate ? formData.expiryDate + "T00:00:00" : null,
    }

    const request = isEditing
      ? api.put(`/consent-records/${id}`, payload)
      : api.post("/consent-records", payload)

    request
      .then(() => {
        navigate("/")
      })
      .catch(() => {
        alert("Failed to save record. Please try again.")
        setSubmitting(false)
      })
  }

  const pageStyle = {
    padding: "24px",
    fontFamily: "Arial, sans-serif",
    maxWidth: "1100px",
    margin: "0 auto"
  }

  const headerStyle = {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: "24px"
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
    display: "block",
    fontSize: "12px",
    fontWeight: "600",
    color: "#4a5568",
    marginBottom: "6px"
  }

  const inputStyle = {
    width: "100%",
    padding: "10px 12px",
    border: "1.5px solid #e5e7eb",
    borderRadius: "8px",
    fontSize: "13px",
    outline: "none",
    boxSizing: "border-box",
    fontFamily: "Arial, sans-serif",
    background: "white"
  }

  const errorStyle = {
    color: "#dc2626",
    fontSize: "12px",
    marginTop: "6px"
  }

  const primaryButtonStyle = {
    background: submitting ? "#94a3b8" : "#1B4F8A",
    color: "white",
    border: "none",
    borderRadius: "8px",
    padding: "10px 16px",
    fontSize: "13px",
    fontWeight: "600",
    cursor: submitting ? "not-allowed" : "pointer",
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

  if (loadingRecord) {
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

  return (
    <div style={pageStyle}>
      <div style={headerStyle}>
        <div>
          <h1 style={{
            fontSize: "24px",
            fontWeight: "700",
            color: "#1a1a2e",
            margin: "0 0 4px"
          }}>
            {isEditing ? "Edit Consent Record" : "Create Consent Record"}
          </h1>
          <p style={{ color: "#6b7280", fontSize: "14px", margin: 0 }}>
            Capture consent details for DPDP Act compliance
          </p>
        </div>
        <button
          type="button"
          onClick={() => navigate("/")}
          style={secondaryButtonStyle}
        >
          Back to List
        </button>
      </div>

      <form onSubmit={handleSubmit} style={{ display: "grid", gap: "16px" }}>
        <div style={cardStyle}>
          <div style={sectionTitleStyle}>Data Principal (Citizen)</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: "16px" }}>
            <div>
              <label style={labelStyle}>Principal ID *</label>
              <input
                type="text"
                name="dataPrincipalId"
                value={formData.dataPrincipalId}
                onChange={handleChange}
                style={inputStyle}
                placeholder="e.g. CUST-001"
              />
              {errors.dataPrincipalId && (
                <div style={errorStyle}>{errors.dataPrincipalId}</div>
              )}
            </div>

            <div>
              <label style={labelStyle}>Principal Name *</label>
              <input
                type="text"
                name="dataPrincipalName"
                value={formData.dataPrincipalName}
                onChange={handleChange}
                style={inputStyle}
                placeholder="e.g. Rahul Sharma"
              />
              {errors.dataPrincipalName && (
                <div style={errorStyle}>{errors.dataPrincipalName}</div>
              )}
            </div>

            <div style={{ gridColumn: "span 2" }}>
              <label style={labelStyle}>Principal Email *</label>
              <input
                type="text"
                name="dataPrincipalEmail"
                value={formData.dataPrincipalEmail}
                onChange={handleChange}
                style={inputStyle}
                placeholder="e.g. rahul@example.com"
              />
              {errors.dataPrincipalEmail && (
                <div style={errorStyle}>{errors.dataPrincipalEmail}</div>
              )}
            </div>
          </div>
        </div>

        <div style={cardStyle}>
          <div style={sectionTitleStyle}>Data Fiduciary (Organisation)</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: "16px" }}>
            <div>
              <label style={labelStyle}>Fiduciary ID *</label>
              <input
                type="text"
                name="dataFiduciaryId"
                value={formData.dataFiduciaryId}
                onChange={handleChange}
                style={inputStyle}
                placeholder="e.g. ORG-101"
              />
              {errors.dataFiduciaryId && (
                <div style={errorStyle}>{errors.dataFiduciaryId}</div>
              )}
            </div>

            <div>
              <label style={labelStyle}>Fiduciary Name *</label>
              <input
                type="text"
                name="dataFiduciaryName"
                value={formData.dataFiduciaryName}
                onChange={handleChange}
                style={inputStyle}
                placeholder="e.g. HDFC Bank"
              />
              {errors.dataFiduciaryName && (
                <div style={errorStyle}>{errors.dataFiduciaryName}</div>
              )}
            </div>
          </div>
        </div>

        <div style={cardStyle}>
          <div style={sectionTitleStyle}>Consent Details</div>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(2, 1fr)", gap: "16px" }}>
            <div style={{ gridColumn: "span 2" }}>
              <label style={labelStyle}>Purpose *</label>
              <textarea
                name="purpose"
                value={formData.purpose}
                onChange={handleChange}
                rows={3}
                style={{ ...inputStyle, resize: "vertical" }}
                placeholder="e.g. Credit score assessment and loan processing"
              />
              {errors.purpose && (
                <div style={errorStyle}>{errors.purpose}</div>
              )}
            </div>

            <div style={{ gridColumn: "span 2" }}>
              <label style={labelStyle}>Data Categories *</label>
              <textarea
                name="dataCategories"
                value={formData.dataCategories}
                onChange={handleChange}
                rows={2}
                style={{ ...inputStyle, resize: "vertical" }}
                placeholder="e.g. Financial data, Identity documents"
              />
              {errors.dataCategories && (
                <div style={errorStyle}>{errors.dataCategories}</div>
              )}
            </div>

            <div>
              <label style={labelStyle}>Consent Status</label>
              <select
                name="consentStatus"
                value={formData.consentStatus}
                onChange={handleChange}
                style={inputStyle}
              >
                <option value="PENDING">PENDING</option>
                <option value="GRANTED">GRANTED</option>
                <option value="REVOKED">REVOKED</option>
                <option value="EXPIRED">EXPIRED</option>
              </select>
            </div>

            <div>
              <label style={labelStyle}>Consent Date</label>
              <input
                type="date"
                name="consentDate"
                value={formData.consentDate}
                onChange={handleChange}
                style={inputStyle}
              />
            </div>

            <div>
              <label style={labelStyle}>Expiry Date</label>
              <input
                type="date"
                name="expiryDate"
                value={formData.expiryDate}
                onChange={handleChange}
                style={inputStyle}
              />
              {errors.expiryDate && (
                <div style={errorStyle}>{errors.expiryDate}</div>
              )}
            </div>
          </div>
        </div>

        <div style={{ display: "flex", justifyContent: "flex-end", gap: "12px" }}>
          <button
            type="button"
            onClick={() => navigate("/")}
            style={secondaryButtonStyle}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            style={primaryButtonStyle}
          >
            {submitting ? "Saving..." : isEditing ? "Update" : "Create"}
          </button>
        </div>
      </form>
    </div>
  )
}

export default ConsentForm