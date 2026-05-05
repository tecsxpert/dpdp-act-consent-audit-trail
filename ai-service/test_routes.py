import json
import pytest
from unittest.mock import patch
from app import app

@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client

# --- Health endpoint ---

def test_health_returns_ok(client):
    response = client.get("/health")
    assert response.status_code == 200
    data = json.loads(response.data)
    assert data["status"] == "ok"
    assert "model" in data
    assert "uptime_seconds" in data

# --- Describe endpoint ---

def test_describe_returns_fallback_when_groq_fails(client):
    with patch("routes.describe.call_groq", return_value=(None, True)):
        response = client.post("/describe",
            json={
                "dataPrincipalName": "Rahul Sharma",
                "dataFiduciaryName": "HDFC Bank",
                "purpose": "Credit assessment",
                "dataCategories": "Financial data",
                "consentStatus": "GRANTED"
            })
        assert response.status_code == 200
        data = json.loads(response.data)
        assert data["isFallback"] is True
        assert "description" in data

def test_describe_returns_400_when_no_body(client):
    response = client.post("/describe",
        content_type="application/json",
        data="")
    assert response.status_code == 400

def test_describe_parses_groq_response(client):
    mock_response = json.dumps({
        "description": "Test description",
        "score": 85,
        "summary": "Test summary"
    })
    with patch("routes.describe.call_groq", return_value=(mock_response, False)):
        response = client.post("/describe",
            json={
                "dataPrincipalName": "Test User",
                "dataFiduciaryName": "Test Bank",
                "purpose": "Test purpose",
                "dataCategories": "Test data",
                "consentStatus": "PENDING"
            })
        assert response.status_code == 200
        data = json.loads(response.data)
        assert data["description"] == "Test description"
        assert data["score"] == 85
        assert data["isFallback"] is False

# --- Recommend endpoint ---

def test_recommend_returns_fallback_when_groq_fails(client):
    with patch("routes.recommend.call_groq", return_value=(None, True)):
        response = client.post("/recommend",
            json={
                "dataPrincipalName": "Rahul Sharma",
                "dataFiduciaryName": "HDFC Bank",
                "purpose": "Credit assessment",
                "dataCategories": "Financial data",
                "consentStatus": "GRANTED"
            })
        assert response.status_code == 200
        data = json.loads(response.data)
        assert isinstance(data, list)
        assert len(data) > 0

def test_recommend_returns_400_when_no_body(client):
    response = client.post("/recommend",
        content_type="application/json",
        data="")
    assert response.status_code == 400

def test_recommend_parses_groq_response(client):
    mock_response = json.dumps([
        {
            "action_type": "REVIEW",
            "description": "Review this record",
            "priority": "HIGH"
        }
    ])
    with patch("routes.recommend.call_groq", return_value=(mock_response, False)):
        response = client.post("/recommend",
            json={
                "dataPrincipalName": "Test User",
                "dataFiduciaryName": "Test Bank",
                "purpose": "Test purpose",
                "dataCategories": "Test data",
                "consentStatus": "PENDING"
            })
        assert response.status_code == 200
        data = json.loads(response.data)
        assert data[0]["action_type"] == "REVIEW"
        assert data[0]["priority"] == "HIGH"

# --- Report endpoint ---

def test_report_returns_fallback_when_groq_fails(client):
    with patch("routes.report.call_groq", return_value=(None, True)):
        response = client.post("/generate-report",
            json={
                "stats": {
                    "total": 10,
                    "granted": 5,
                    "revoked": 2,
                    "pending": 2,
                    "expired": 1
                },
                "records": []
            })
        assert response.status_code == 200
        data = json.loads(response.data)
        assert data["isFallback"] is True
        assert "title" in data