# Projekt Paperless
Das Projekt besteht aus einem SpringBoot Backend und einem Vue.js Frontend
Man kann Dokumente Hochladen, welche mit OCR gescannt und in der Datenbank gespeichert werden.
Im Frontend kann sich jeder User seine Dokumente ansehen und diese Verwalten.
Zusätzlich wird eine Zusammenfassung jedes Dokuments erstellt mithilfe von Generativer AI
Diese Dokumente können dann im Frontend mit FullText-Search durchsucht werden

## Architektur / Dependencies:
- Spring Boot
- Vue.js
- RabbitMQ
- ElasticSearch (Spring Boot)
- MinIO

# Setup

## Voraussetzungen
- Docker
- JDK 25

## Projekt Starten
im Terminal:
``docker-compose up``

SpringBoot-Application im `/src/java/fh.swen.paperless` Directory starten: \
```PaperlessApplication```

# Endpoints
Die API läuft auf `http://localhost:8080`

## User (`/api/users`)
- `GET /api/users` - Alle User
- `GET /api/users/{id}` - User per ID
- `GET /api/users/username/{username}` - User per Username
- `GET /api/users/{id}/documents?title=` - Alle Dokumente eines Users, `title` als filter
- `POST /api/users/register` - Neuen User registrieren
- `POST /api/users/login` - Login mit Username und Passwort
- `DELETE /api/users/{id}` - User und seine Dokumente löschen

## Dokumente (`/api/documents`)
- `GET /api/documents` - Alle Dokumente
- `GET /api/documents/{id}` - Dokument per ID
- `GET /api/documents/search?title=` - Dokumente nach Titel suchen
- `POST /api/documents` - Neues Dokument anlegen
- `PUT /api/documents/{id}` - Dokument bearbeiten
- `DELETE /api/documents/{id}` - Dokument löschen

# Tests ausführen


# Integrationstest




