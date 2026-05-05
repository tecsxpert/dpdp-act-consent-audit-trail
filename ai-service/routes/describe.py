from flask import Blueprint, request, jsonify
from services.groq_client import call_groq

describe_bp = Blueprint("describe", __name__)

@describe_bp.route("/describe", methods=["POST"])
def describe():
    data = request.get_json()

    if not data:
        return jsonify({"error": "Request body required"}), 400

    prompt = f"""You are a DPDP Act compliance expert. Analyze this consent record and provide a clear description.

Consent Record:
- Principal: {data.get('dataPrincipalName', 'Unknown')}
- Organization: {data.get('dataFiduciaryName', 'Unknown')}
- Purpose: {data.get('purpose', 'Unknown')}
- Data Categories: {data.get('dataCategories', 'Unknown')}
- Status: {data.get('consentStatus', 'Unknown')}

Respond with ONLY a JSON object in this exact format:
{{
  "description": "2-3 sentence plain English description of what this consent means",
  "score": <integer 1-100 representing compliance strength>,
  "summary": "one line summary"
}}"""

    content, is_fallback = call_groq([{"role": "user", "content": prompt}])

    if is_fallback or not content:
        return jsonify({
            "description": "AI description unavailable at this time.",
            "score": 50,
            "summary": "Fallback response",
            "isFallback": True
        })

    try:
        import json
        # strip markdown code blocks if present
        clean = content.strip().replace("```json", "").replace("```", "").strip()
        result = json.loads(clean)
        result["isFallback"] = False
        return jsonify(result)
    except Exception:
        return jsonify({
            "description": content,
            "score": 50,
            "summary": "Parsed from raw response",
            "isFallback": False
        })