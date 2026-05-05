import time
from flask import Blueprint, jsonify
from services.groq_client import MODEL, get_avg_response_time, START_TIME

health_bp = Blueprint("health", __name__)

@health_bp.route("/health", methods=["GET"])
def health():
    uptime_seconds = round(time.time() - START_TIME, 2)
    return jsonify({
        "status": "ok",
        "model": MODEL,
        "avg_response_time_seconds": get_avg_response_time(),
        "uptime_seconds": uptime_seconds
    })