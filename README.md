# DPDP Act — Consent Audit Trail

An AI-powered web application for managing and auditing consent records under India's Digital Personal Data Protection Act 2023. Built as a capstone internship project (Tool-94) by a 5-member team over a 20-working-day sprint.

## What This Project Does

The DPDP Act 2023 requires every organization to get explicit consent before collecting personal data. This system is the internal tool used by compliance officers to create and track consent records from citizens to organizations, monitor consent status across PENDING, GRANTED, REVOKED and EXPIRED states, get AI-generated descriptions and compliance scores for each record, view the full audit trail of every change made to every record, and generate compliance reports and export data as CSV.

## Architecture
┌─────────────────────┐     HTTP/JWT      ┌──────────────────────────┐
│   React 18 + Vite   │ ───────────────▶  │   Spring Boot 3.2.5      │
│   Tailwind CSS      │                   │   Java 17                │
│   Recharts          │                   │   JWT Auth               │
│   Port 5173         │                   │   Flyway Migrations      │
└─────────────────────┘                   │   Port 8081              │
└──────────┬───────────────┘
│
┌────────────────┼────────────────┐
▼                                 ▼
┌──────────────────┐             ┌─────────────────┐
│  PostgreSQL 15   │             │  Flask 3.0      │
│  Port 5433       │             │  Python 3.12    │
│  consentdb       │             │  Groq API       │
└──────────────────┘             │  LLaMA-3.3-70b  │
│  Port 5000      │
└─────────────────┘

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, Vite, Tailwind CSS, Axios, Recharts |
| Backend | Java 17, Spring Boot 3.2.5, Spring Security, JWT |
| Database | PostgreSQL 15, Flyway migrations |
| AI Service | Python 3.12, Flask 3.0, Groq API (LLaMA-3.3-70b) |
| Testing | JUnit 5, Mockito, pytest |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Infrastructure | Docker, Docker Compose |

## Project Structure
dpdp-act-consent-audit-trail/
├── backend/
│   ├── src/main/java/com/internship/tool/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   ├── src/main/resources/
│   │   ├── db/migration/
│   │   └── application.yml
│   ├── src/test/
│   ├── Dockerfile
│   └── pom.xml
├── ai-service/
│   ├── routes/
│   ├── services/
│   ├── app.py
│   ├── test_routes.py
│   ├── Dockerfile
│   └── requirements.txt
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── context/
│   │   ├── pages/
│   │   └── services/
│   ├── Dockerfile
│   └── package.json
├── docker-compose.yml
├── .env.example
├── SECURITY.md
└── README.md

## Database Schema

### consent_record

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| data_principal_id | VARCHAR(100) | Citizen ID |
| data_principal_name | VARCHAR(255) | Citizen name |
| data_principal_email | VARCHAR(255) | Citizen email |
| data_fiduciary_id | VARCHAR(100) | Organization ID |
| data_fiduciary_name | VARCHAR(255) | Organization name |
| purpose | VARCHAR(500) | Why data is collected |
| data_categories | VARCHAR(500) | What data is collected |
| consent_status | VARCHAR(20) | PENDING/GRANTED/REVOKED/EXPIRED |
| ai_description | TEXT | AI-generated description |
| ai_score | INTEGER | Compliance score 1-100 |
| is_fallback | BOOLEAN | Whether AI used fallback |
| consent_date | TIMESTAMP | When consent was given |
| expiry_date | TIMESTAMP | When consent expires |
| is_active | BOOLEAN | Soft delete flag |
| created_at | TIMESTAMP | Auto-set on create |
| updated_at | TIMESTAMP | Auto-set on update |

### audit_log

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL | Primary key |
| consent_record_id | BIGINT | FK to consent_record |
| action | VARCHAR(20) | CREATE/UPDATE/DELETE/STATUS_CHANGE |
| performed_by | VARCHAR(100) | Username who made the change |
| performed_at | TIMESTAMP | When the change happened |
| old_value | TEXT | Previous value |
| new_value | TEXT | New value |
| remarks | VARCHAR(500) | Additional notes |

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 18+
- Python 3.11+
- PostgreSQL 15
- Docker + Docker Compose (optional)

## Setup Without Docker

### 1. Clone the repository

```bash
git clone https://github.com/tecsxpert/dpdp-act-consent-audit-trail.git
cd dpdp-act-consent-audit-trail
```

### 2. Create the database

```bash
psql -U postgres -p 5433
CREATE DATABASE consentdb;
\q
```

### 3. Create .env file in ai-service folder
GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=llama-3.3-70b-versatile
FLASK_PORT=5000

Get a free Groq API key at https://console.groq.com

### 4. Start the backend

```bash
cd backend
mvn spring-boot:run
```

Backend starts on http://localhost:8081
Swagger UI: http://localhost:8081/swagger-ui/index.html

### 5. Start the AI service

```bash
cd ai-service
pip install -r requirements.txt
python app.py
```

AI service starts on http://localhost:5000
Health check: http://localhost:5000/health

### 6. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend starts on http://localhost:5173

## Setup With Docker

```bash
cp .env.example .env
docker-compose up --build
```

Full stack starts on:
- Frontend: http://localhost
- Backend: http://localhost:8081
- AI Service: http://localhost:5000

## Default Login Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| officer | officer123 | USER |

## API Reference

### Auth Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | Login, returns JWT token |
| GET | /api/auth/me | Get current user info |

### Consent Record Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/consent-records | Get all records paginated and filtered |
| GET | /api/consent-records/{id} | Get single record |
| POST | /api/consent-records | Create new record |
| PUT | /api/consent-records/{id} | Update record |
| DELETE | /api/consent-records/{id} | Soft delete record |
| GET | /api/consent-records/stats | Dashboard statistics |
| GET | /api/consent-records/export | Download CSV |

### Query Parameters for GET /api/consent-records

| Parameter | Type | Description |
|-----------|------|-------------|
| page | int | Page number, default 0 |
| size | int | Page size, default 10 |
| q | string | Search by name, purpose, fiduciary |
| status | string | Filter by PENDING/GRANTED/REVOKED/EXPIRED |
| from | date | Filter from date YYYY-MM-DD |
| to | date | Filter to date YYYY-MM-DD |

### AI Service Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /describe | Generate AI description and compliance score |
| POST | /recommend | Get 3 actionable compliance recommendations |
| POST | /generate-report | Generate full compliance report |
| GET | /health | Service health, model name, uptime |

### Example POST /describe Request

```json
{
  "dataPrincipalName": "Rahul Sharma",
  "dataFiduciaryName": "HDFC Bank",
  "purpose": "Credit score assessment",
  "dataCategories": "Financial data, Identity documents",
  "consentStatus": "GRANTED"
}
```

### Example POST /describe Response

```json
{
  "description": "Rahul Sharma has granted HDFC Bank consent to access financial data and identity documents for credit score assessment purposes.",
  "score": 85,
  "summary": "High compliance consent record",
  "isFallback": false
}
```

## Running Tests

### JUnit Tests

```bash
cd backend
mvn test
```

10 tests covering getById, create, update, delete, getStats, updateAiFields, getAllRecords, inactive record filtering, and not found exceptions. All passing.

### pytest Tests

```bash
cd ai-service
python -m pytest test_routes.py -v
```

8 tests covering health endpoint, describe fallback, describe 400 validation, describe Groq response parsing, recommend fallback, recommend 400 validation, recommend response parsing, and report fallback. Groq API is fully mocked so tests run without live network access.

## Environment Variables Reference

| Variable | Default | Description |
|----------|---------|-------------|
| DB_HOST | localhost | PostgreSQL host |
| DB_PORT | 5433 | PostgreSQL port |
| DB_NAME | consentdb | Database name |
| DB_USERNAME | postgres | DB username |
| DB_PASSWORD | postgres | DB password |
| JWT_SECRET | long string | JWT signing secret |
| REDIS_HOST | localhost | Redis host |
| REDIS_PORT | 6379 | Redis port |
| GROQ_API_KEY | required | Groq API key from console.groq.com |
| GROQ_MODEL | llama-3.3-70b-versatile | Groq model name |
| FLASK_PORT | 5000 | Flask service port |

## Team

| Role | Member |
|------|--------|
| Java Developer 1 | Yogesh |
| Java Developer 2 | Jayanth C |
| AI Developer 1 | - |
| AI Developer 2 | - |
| Security Reviewer | - |
