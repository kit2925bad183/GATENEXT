# GateNest Enterprise — Production Deployment & Operations Guide

## 1. System Architecture Overview

GateNest is an enterprise-grade Apartment Visitor Management System designed with clean separation of concerns and high-security standards:

- **Frontend**: React 18, TypeScript, Tailwind CSS, Lucide Icons, Vite, bilingual English / தமிழ் (Tamil) localization, responsive layout, and installable PWA with offline shell fallback.
- **Backend**: Spring Boot 3.4.3 on Java 21, 100% pure parameterized Spring JDBC (strictly zero JPA/Hibernate), custom Session Context persistence, and Flyway versioned database migrations.
- **Security**: Double-submit CSRF cookie token protection (`XSRF-TOKEN`), BCrypt (strength 12) adaptive password hashing, Request correlation IDs via SLF4J MDC, sensitive endpoint `no-store` cache controls, and role-based authority matching (`ADMINISTRATOR`, `SECURITY`, `RESIDENT`).
- **Database**: MySQL 8.4 LTS persistence with zero-setup H2 MySQL-compatibility mode for local development.

```
                  +----------------------------------------------+
                  |               Client Browsers                |
                  |     (Desktop, Mobile Chrome, iOS Safari)     |
                  +----------------------+-----------------------+
                                         |
                                         | HTTPS (Port 443 / 80)
                                         v
                  +----------------------------------------------+
                  |         Nginx Reverse Proxy & PWA            |
                  |     (Static Asset Caching, TLS Term)         |
                  +----------------------+-----------------------+
                                         |
                                         | Proxy Pass /api/
                                         v
                  +----------------------------------------------+
                  |       Spring Boot 3.4.3 Application          |
                  |    (Pure Parameterized JDBC Repositories)    |
                  +----------------------+-----------------------+
                                         |
                                         | JDBC (Port 3306)
                                         v
                  +----------------------------------------------+
                  |            MySQL 8.4 LTS Database            |
                  |      (Flyway Migrations V1 -> V4)            |
                  +----------------------------------------------+
```

---

## 2. Environment Variables & Secrets Reference

| Variable Name | Default Value (Dev) | Production Recommendation | Description |
|---|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `prod` | Active Spring profile (`dev` uses embedded H2 file, `prod` uses MySQL). |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/gatenest_db` | `jdbc:mysql://db:3306/gatenest_db?useSSL=false&serverTimezone=UTC` | JDBC connection URL. |
| `SPRING_DATASOURCE_USERNAME` | `gatenest_user` | Strong service username | Database authentication user. |
| `SPRING_DATASOURCE_PASSWORD` | `gatenest_password` | 32+ char random secret | Database authentication password. |
| `DB_ROOT_PASSWORD` | `root_gatenest_secure_2026` | 32+ char random secret | MySQL root administrative password. |
| `APP_PUBLIC_URL` | `http://localhost:5173` | `https://gatenest.yourdomain.com` | Allowed CORS and reverse proxy origin. |
| `COOKIE_SECURE` | `false` | `true` | Enforces `Secure` flag on session cookies under HTTPS. |
| `GATENEST_BOOTSTRAP_TOKEN` | `bootstrap-gatenest-admin-secret-2026` | Custom secure token | Token used for initial administrator bootstrapping. |

---

## 3. Production Single-Server Deployment (Docker Compose)

### Prerequisites
- Docker Engine 24+ and Docker Compose v2+
- Domain name pointed to host IP (with SSL certificate configured in Nginx or Cloudflare)

### Steps to Deploy
1. **Clone repository and configure environment**:
   ```bash
   git clone https://github.com/your-org/gatenest.git
   cd gatenest
   cp .env.example .env
   # Edit .env to set strong database passwords and your public domain
   ```

2. **Launch all containers**:
   ```bash
   docker compose -f docker-compose.prod.yml up -d --build
   ```

3. **Verify health status**:
   ```bash
   docker compose -f docker-compose.prod.yml ps
   curl -s http://localhost/api/v1/health
   ```
   *Expected response:*
   ```json
   {
     "status": "UP",
     "app": "GateNest Apartment Security",
     "version": "1.2.0-pwa",
     "database": "CONNECTED",
     "timestamp": "2026-10-08T07:36:25Z"
   }
   ```

---

## 4. Progressive Web App (PWA) Operation

- **Installation**:
  - **Android / Chrome**: Users can click the native install prompt or tap 3-dots -> "Install app".
  - **iOS / iPadOS Safari**: Tap Share icon -> "Add to Home Screen" (visual step-by-step guidance available in the in-app "Install GateNest App" dialog).
  - **Desktop Chrome / Edge**: Click the install icon (⊕) in the browser URL bar.
- **Offline Safety Guarantee**:
  - Service worker caches only the static application shell (`index.html`, compiled CSS/JS, fonts, icons).
  - Sensitive API routes (`/api/**`), visitor records, QR codes, passes, and resident contact data are **NEVER** cached by the service worker.
  - When offline, a persistent amber status banner informs users that gate verification and admission mutations are disabled to maintain security integrity.
- **Controlled Updates**:
  - When a new version is deployed, the client detects the new service worker and presents a discreet "Update Available — [Update]" toast to allow safe reload without disrupting ongoing gate operations.

---

## 5. Database Backup & Restore Procedures

### Automated Backup
- **PowerShell (Windows)**:
  ```powershell
  .\scripts\backup-database.ps1 -ContainerName gatenest-prod-db -DbName gatenest_db
  ```
- **Bash (Linux/macOS)**:
  ```bash
  ./scripts/backup-database.sh gatenest-prod-db gatenest_db
  ```
*Backups are saved to `./backups/gatenest_backup_YYYYMMDD_HHMMSS.sql`.*

### Database Restoration
- **PowerShell (Windows)**:
  ```powershell
  .\scripts\restore-database.ps1 -BackupFile ./backups/gatenest_backup_20261008_073000.sql
  ```
- **Bash (Linux/macOS)**:
  ```bash
  ./scripts/restore-database.sh ./backups/gatenest_backup_20261008_073000.sql
  ```

---

## 6. Migration & Rollback Guidelines

1. **Flyway Migrations**:
   - `V1__init_schema.sql`: Core users, profiles, gates, blocks, flats, visits, and audit logs.
   - `V2__seed_initial_data.sql`: Greenfield Heights baseline community setup.
   - `V3__enterprise_features_schema.sql`: 20 operational tables (saved contacts, recurring schedules, group invitations, parcels, staff, parking, complaints, notices, assistance).
   - `V4__seed_enterprise_sample_data.sql`: Seed staff and parking allocations.
   - `V5__filtering_and_admin_security_schema.sql`: Advanced filtering indexes, visit event timeline audits, and admin query optimization.
2. **Rollback Principle**:
   - Application rollback does **not** automatically reverse database migrations.
   - Always run an automated backup prior to executing new schema migrations.
   - Schema updates maintain backward-compatible column additions.
