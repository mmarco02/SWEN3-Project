# Projekt Paperless

Document Management System mit Spring Boot Backend und Vue.js Frontend.

Geplanter Funktionsumfang:
- Dokumente hochladen, per OCR auslesen und in der Datenbank speichern
- Automatische Zusammenfassung jedes Dokuments mit Generativer AI
- Volltextsuche über Elasticsearch
- Jeder User kann im Frontend seine Dokumente ansehen und verwalten

## Architektur / Technologien
**Backend:** Java 25, Spring Boot 4, Spring Data JPA, PostgreSQL, MinIO (Datei-Speicher)
**Frontend:** Vue.js + nginx
**Geplant:** RabbitMQ, Elasticsearch, Google Gemini

# Setup

## Voraussetzungen
- Docker Desktop
- JDK 25 (nur für lokale Entwicklung ohne Docker)

## Projekt starten (alles in Docker)
```bash
docker compose up --build
```
Startet Frontend (http://localhost, Port 80), REST-Server (Port 8080), PostgreSQL (Port 5432), Elasticsearch (Port 9200) und MinIO (API Port 9000, Konsole http://localhost:9090, Login `paperless` / `paperless-secret`).

Hochgeladene Dateien liegen im MinIO-Bucket `documents` (wird beim ersten Upload angelegt), in der Datenbank wird nur der Verweis darauf gespeichert. Das MinIO-Image kommt von Chainguard (`cgr.dev/chainguard/minio`), weil `minio/minio` nicht mehr auf Docker Hub veröffentlicht wird.

Stoppen:
```bash
docker compose down
```

## Lokale Entwicklung in IntelliJ
1. Nur Datenbank, Elasticsearch und MinIO starten:
   ```bash
   docker compose up postgres elasticsearch minio
   ```
2. `PaperlessApplication` in `src/main/java/fh/swen/paperless` starten.

## Frontend (Vue.js)
Liegt in `/frontend` (Vue 3, Vite, Vue Router). Im Docker-Setup liefert nginx das gebaute Frontend aus und leitet `/api` an den REST-Server weiter.

Lokal entwickeln (Node.js 24, Backend läuft auf Port 8080):
```bash
cd frontend
npm install
npm run dev
```
Der Vite-Dev-Server (http://localhost:5173) leitet `/api` an `localhost:8080` weiter.

Seiten: Login, Registrierung, eigene Dokumente (hochladen, filtern, löschen), Dokument-Detail (Datei ansehen, Titel bearbeiten, Tags zuweisen), Tags verwalten, Account löschen.

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
- `POST /api/documents` – Dokument hochladen (`multipart/form-data` mit `title`, `ownerId` und `file`; PDF, PNG, JPEG, GIF oder WebP, max. 20 MB)
- `GET /api/documents/{id}/file` – hochgeladene Datei abrufen
- `PUT /api/documents/{id}` – Titel/Owner bearbeiten (JSON)
- `DELETE /api/documents/{id}` – Dokument samt Datei löschen
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

# Dokument hochladen und Datei wieder abrufen
curl -X POST localhost:8080/api/documents -F title=Stromrechnung -F ownerId=1 -F "file=@stromrechnung.pdf;type=application/pdf"
curl localhost:8080/api/documents/1/file -o download.pdf

# Tag anlegen und dem Dokument zuweisen
curl -X POST localhost:8080/api/tags -H "Content-Type: application/json" -d '{"name":"Rechnung"}'
curl -X POST localhost:8080/api/documents/1/tags/1
```

# Tests ausführen
```bash
./mvnw test
```
Die Unit-Tests mocken Datenbank und MinIO mit Mockito und brauchen weder PostgreSQL noch MinIO.

# Integrationstest
Folgt in Sprint 6.