from flask import Blueprint, request, jsonify
from services.groq_client import call_groq

recommend_bp = Blueprint("recommend", __name__)

@recommend_bp.route("/recommend", methods=["POST"])
def recommend():
    data = request.get_json()

    if not data:
        return jsonify({"error": "Request body required"}), 400

    prompt = f"""You are a DPDP Act compliance expert. Give 3 actionable recommendations for this consent record.

Consent Record:
- Principal: {data.get('dataPrincipalName', 'Unknown')}
- Organization: {data.get('dataFiduciaryName', 'Unknown')}
- Purpose: {data.get('purpose', 'Unknown')}
- Data Categories: {data.get('dataCategories', 'Unknown')}
- Status: {data.get('consentStatus', 'Unknown')}

Respond with ONLY a JSON array of exactly 3 objects in this format:
[
  {{
    "action_type": "REVIEW" or "UPDATE" or "REVOKE" or "NOTIFY" or "AUDIT",
    "description": "specific actionable recommendation",
    "priority": "HIGH" or "MEDIUM" or "LOW"
  }}
]"""

    content, is_fallback = call_groq([{"role": "user", "content": prompt}])

    if is_fallback or not content:
        return jsonify([
            {
                "action_type": "REVIEW",
                "description": "Review consent record for compliance.",
                "priority": "MEDIUM"
            }
        ])

    try:
        import json
        clean = content.strip().replace("```json", "").replace("```", "").strip()
        result = json.loads(clean)
        return jsonify(result)
    except Exception:
        return jsonify([
            {
                "action_type": "REVIEW",
                "description": content,
                "priority": "MEDIUM"
            }
        ])