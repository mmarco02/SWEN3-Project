# Projekt Paperless

Document Management System mit Spring Boot Backend und Vue.js Frontend.

Geplanter Funktionsumfang:
- Dokumente hochladen, per OCR auslesen und in der Datenbank speichern
- Automatische Zusammenfassung jedes Dokuments mit Generativer AI
- Volltextsuche über Elasticsearch
- Jeder User kann im Frontend seine Dokumente ansehen und verwalten

## Architektur / Technologien
**Backend:** Java 25, Spring Boot 4, Spring Data JPA, PostgreSQL
**Geplant:** Vue.js + nginx (Frontend), RabbitMQ, Elasticsearch, MinIO, Google Gemini

# Setup

## Voraussetzungen
- Docker Desktop
- JDK 25 (nur für lokale Entwicklung ohne Docker)

## Projekt starten (alles in Docker)
```bash
docker compose up --build
```
Startet REST-Server (Port 8080), PostgreSQL (Port 5432) und Elasticsearch (Port 9200).

Stoppen:
```bash
docker compose down
```

## Lokale Entwicklung in IntelliJ
1. Nur Datenbank und Elasticsearch starten:
   ```bash
   docker compose up postgres elasticsearch
   ```
2. `PaperlessApplication` in `src/main/java/fh/swen/paperless` starten.

# Endpoints
Die API läuft auf `http://localhost:8080`.

## User (`/api/users`)
- `GET /api/users` – alle User
- `GET /api/users/{id}` – User per ID
- `GET /api/users/username/{username}` – User per Username
- `GET /api/users/{id}/documents?title=` – alle Dokumente eines Users, `title` optional als Filter
- `POST /api/users/register` – neuen User registrieren
- `POST /api/users/login` – Login mit Username und Passwort
- `DELETE /api/users/{id}` – User und seine Dokumente löschen

## Dokumente (`/api/documents`)
- `GET /api/documents` – alle Dokumente
- `GET /api/documents/{id}` – Dokument per ID
- `GET /api/documents/search?title=` – Dokumente nach Titel suchen
- `POST /api/documents` – neues Dokument anlegen
- `PUT /api/documents/{id}` – Dokument bearbeiten
- `DELETE /api/documents/{id}` – Dokument löschen
- `POST /api/documents/{id}/tags/{tagId}` – Tag zu Dokument hinzufügen
- `DELETE /api/documents/{id}/tags/{tagId}` – Tag von Dokument entfernen

## Tags (`/api/tags`)
- `GET /api/tags` – alle Tags
- `GET /api/tags/{id}` – Tag per ID
- `GET /api/tags/{id}/documents` – alle Dokumente mit diesem Tag
- `POST /api/tags` – neuen Tag anlegen
- `DELETE /api/tags/{id}` – Tag löschen (wird automatisch von allen Dokumenten entfernt)

## Beispiel-Requests
```bash
# User registrieren (Username 3–50 Zeichen, Passwort 8–72 Zeichen)
curl -X POST localhost:8080/api/users/register -H "Content-Type: application/json" -d '{"username":"maxmuster","password":"geheim123"}'

# Dokument anlegen
curl -X POST localhost:8080/api/documents -H "Content-Type: application/json" -d '{"title":"Stromrechnung","ownerId":1}'

# Tag anlegen und dem Dokument zuweisen
curl -X POST localhost:8080/api/tags -H "Content-Type: application/json" -d '{"name":"Rechnung"}'
curl -X POST localhost:8080/api/documents/1/tags/1
```

# Tests ausführen
```bash
./mvnw test
```
Die Unit-Tests mocken die Datenbank mit Mockito und brauchen keine laufende PostgreSQL-Instanz.

# Integrationstest
Folgt in Sprint 6.