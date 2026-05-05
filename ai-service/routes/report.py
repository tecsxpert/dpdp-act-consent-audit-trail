from flask import Blueprint, request, jsonify
from services.groq_client import call_groq

report_bp = Blueprint("report", __name__)

@report_bp.route("/generate-report", methods=["POST"])
def generate_report():
    data = request.get_json()

    if not data:
        return jsonify({"error": "Request body required"}), 400

    stats = data.get("stats", {})
    records = data.get("records", [])

    prompt = f"""You are a DPDP Act compliance expert. Generate a formal compliance report.

Statistics:
- Total Records: {stats.get('total', 0)}
- Granted: {stats.get('granted', 0)}
- Revoked: {stats.get('revoked', 0)}
- Pending: {stats.get('pending', 0)}
- Expired: {stats.get('expired', 0)}

Sample Records: {len(records)} records provided.

Respond with ONLY a JSON object in this exact format:
{{
  "title": "DPDP Act Compliance Report",
  "summary": "2-3 sentence executive summary",
  "overview": "detailed paragraph about the consent landscape",
  "key_items": [
    "key finding 1",
    "key finding 2",
    "key finding 3"
  ],
  "recommendations": [
    "recommendation 1",
    "recommendation 2",
    "recommendation 3"
  ]
}}"""

    content, is_fallback = call_groq(
        [{"role": "user", "content": prompt}],
        max_tokens=1500
    )

    if is_fallback or not content:
        return jsonify({
            "title": "DPDP Act Compliance Report",
            "summary": "Report generation unavailable at this time.",
            "overview": "Please try again later.",
            "key_items": [],
            "recommendations": [],
            "isFallback": True
        })

    try:
        import json
        clean = content.strip().replace("```json", "").replace("```", "").strip()
        result = json.loads(clean)
        result["isFallback"] = False
        return jsonify(result)
    except Exception:
        return jsonify({
            "title": "DPDP Act Compliance Report",
            "summary": content[:200],
            "overview": content,
            "key_items": [],
            "recommendations": [],
            "isFallback": False
        })