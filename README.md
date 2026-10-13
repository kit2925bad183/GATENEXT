# 🚪 GateNest — Enterprise Apartment Visitor & Community Management System

> **GateNest** is a comprehensive, production-grade Apartment Visitor and Community Management System engineered for residential gated societies. It pairs a clean, white-background user interface with responsive 3D Three.js architectural visuals, full English & Tamil (தமிழ்) internationalization, an installable PWA experience, and a high-performance Java 21 Spring Boot backend powered strictly by **Spring JDBC (no ORM / Hibernate)** and MySQL 8.4 LTS persistence.

---

## ✨ Features & Enterprise Modules

### 1. 🏢 Multi-Block & Flat Hierarchy
- **Greenfield Heights community**: Blocks A, B, C, multi-floor flats (101 to 504), and designated physical security gates (North Gate, South Gate, East Gate).
- **Three Core Personas**: Community Administrator, Gate Security Staff, and Resident Occupant (Owner / Tenant).

### 2. 🗓️ Visitor Scheduling, Saved Contacts & Repeat Visits
- **Visitor Pre-Invitations**: Arrival windows, expected durations, purpose, and cryptographically verified SHA-256 pass credentials.
- **Saved Contacts**: Residents maintain private frequent visitor profiles with 1-click "Invite Again" generation.
- **Credential Regeneration**: Arrival window modifications automatically invalidate old credentials and generate fresh passes.

### 3. 🔄 Recurring Service Visits
- **Service Pass Rules**: Supports domestic help, drivers, milk delivery, tutors, and recurring vendors.
- **Server-Side Validation**: Checks allowed weekdays (e.g. `MON,WED,FRI`), valid date ranges, and time windows (e.g. `08:00 - 18:00`) before admitting visitors.

### 4. 👥 Group Invitations & Individual Occupancy Tracking
- **Multi-Guest Event Passes**: Event title, maximum guest count, and named guest roster.
- **Per-Person Occupancy**: Each admitted guest receives a distinct occupancy record; organizer check-in does not falsely mark the entire group inside.

### 5. 🖥️ Security Gate Desk Console
- **Fast-Action Gate Interface**: QR scanner, pass verification, walk-in registration, parcel receipt, domestic staff check-in, and live waiting approvals.
- **Non-Auto Admitting**: QR scanning validates host, flat, and window validity before explicit guard check-in.

### 6. 📦 Digital Parcel Locker & Collection Delegation
- **Complete Parcel Lifecycle**: `RECEIVED` ➔ `READY_FOR_COLLECTION` ➔ `COLLECTED` / `RETURNED`.
- **Authorized Delegation**: Residents can delegate collection to family members, neighbors, or staff.
- **Atomic Double-Collection Prevention**: Database transactions ensure parcels cannot be double-collected.

### 7. 🧹 Domestic Staff Attendance
- **Daily Staff Roster**: Cleaners, cooks, drivers, gardeners, and recurring service personnel.
- **Single Open Session Constraint**: Guards cannot open multiple concurrent sessions for the same staff member.
- **Resident Authorization**: Residents manage authorizations for staff permitted to enter their flats.

### 8. 🚗 Vehicle & Visitor Parking Management
- **Resident Vehicles**: Two-wheeler and four-wheeler resident vehicle registry.
- **Visitor Slot Allocation**: Real-time slot grid; guards allocate available visitor slots during check-in and release them on checkout.

### 9. 🛠️ Complaints & Maintenance Ticketing
- **Lifecycle Tracking**: `OPEN` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`.
- **Secure File Attachments**: Validated mime-types, server-generated filenames, authorized private downloads.
- **Reopen Flow**: Residents can reopen resolved complaints within a configured window.

### 10. 📢 Notice Board & Announcements
- **Audience Targeting**: Broadcast notices to the entire community or specific blocks.
- **Notice States**: Draft, Published, Pinned, and Archived notices with automatic expiration dates.

### 11. 🚨 Security Assistance (SOS Alert) & Shift Handover
- **Resident Assistance Alert**: Instant alert to all on-duty gate staff with flat and location details.
- **Security Shift Handover**: Guards log shift transition notes, unresolved issues, and incoming guard acknowledgements.

### 12. 📊 Advanced Analytics & CSV Export
- **Operational Metrics**: Gate traffic, visitor categories, daily trends, parcel status, and complaint resolution rates.
- **Formula Injection Defense**: CSV exports sanitize leading `=`, `+`, `-`, `@`, `\t`, `\r` characters to protect spreadsheet applications.

### 13. 🌐 Full English & Tamil (தமிழ்) Internationalization
- **Seamless Language Switcher**: All forms, labels, tables, navigation items, statuses, and modals support English and Tamil with zero layout clipping.

### 14. 📱 Installable Progressive Web App (PWA)
- **Installable Web App**: Custom icons, standalone display mode, and service worker caching static assets only (no private data or credentials cached).

---

## 🛠️ Technology Stack

| Layer | Technology |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.4.3, Spring Security (Persistent Session JDBC, Cookie CSRF), Spring JDBC (`JdbcTemplate` / `NamedParameterJdbcTemplate`), MySQL Connector/J 8.4.0, Flyway Migrations, ZXing QR Engine |
| **Database** | MySQL 8.4 LTS (or zero-setup embedded H2 MySQL mode for dev), Flyway V1–V4 migrations |
| **Frontend** | React 18, TypeScript, Vite 6, Tailwind CSS 3 tokens, React Router v6, Lucide React, Date-fns |
| **Visuals** | Three.js interactive 3D apartment canvas with lightweight fallback |
| **PWA** | Web App Manifest, Service Worker (Strictly static asset caching) |
| **i18n** | Full English & Tamil (`en`, `ta`) localization dictionary |

---

## 🚀 Quick Start Guide

### Step 1: Clone and Navigate
```powershell
cd c:\gatenext.og
```

### Step 2: Run the Spring Boot Backend
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
*The backend runs on `http://localhost:8080`. Flyway automatically executes migrations V1 through V4 and initializes database tables, blocks, flats, gates, parking slots, and default users.*

### Step 3: Run the React Frontend
```powershell
cd frontend
npm install
npm run dev
```
*The frontend is accessible at **`http://localhost:5173`**.*

---

## 🔑 Demo Login Credentials

| Role | Email | Password | Access Level |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@greenfield.com` | `admin1234` | Full Community & Security Governance |
| **Security Guard** | `guard.north@greenfield.com` | `guard1234` | Gate Desk, Pass Verification, Parcels, Parking |
| **Resident** | `resident.101@greenfield.com` | `resident1234` | Invitations, Parcels, Staff, Complaints, Parking |

---

## 🧪 Testing Suite Execution

### Backend Automated Test Suite
```powershell
cd backend
.\mvnw.cmd test
```
*Executes all Spring JDBC service tests including Pass Credential SHA-256 cryptography, Recurring Visit schedule validation, and CSV formula injection sanitization.*

### Frontend Production Build Verification
```powershell
cd frontend
npm run build
```
*Executes full TypeScript typecheck (`tsc -b`) and Vite production bundle optimization.*

---

## 📂 Project Structure

```
c:\gatenext.og\
├── compose.yaml                    # Docker Compose MySQL 8.4 configuration
├── README.md                       # Main product guide
├── docs\                           # Comprehensive architecture docs
│   ├── DATABASE_SCHEMA.md          # Flyway V1-V4 table definitions & ERD
│   ├── API_DOCUMENTATION.md        # REST API endpoint specifications
│   └── SECURITY_PERMISSIONS_MATRIX.md # RBAC and record-level security matrix
│
├── backend\                        # Java 21 Spring Boot Backend
│   ├── pom.xml                     # Maven dependencies (Pure Spring JDBC)
│   ├── mvnw.cmd & mvnw             # Maven wrapper scripts
│   └── src\
│       ├── main\java\com\gatenest\
│       │   ├── config\             # Flyway & Session JDBC configurations
│       │   ├── controller\         # REST API Controllers (/api/v1/...)
│       │   ├── dto\                # Request/Response DTOs with validation
│       │   ├── exception\          # Global exception handlers
│       │   ├── model\              # Serializable domain records
│       │   ├── repository\         # Parameterized Spring JDBC Repositories
│       │   ├── security\           # Spring Security, CSRF, Rate Limiter
│       │   └── service\            # Core business logic & cryptographic passes
│       └── main\resources\
│           ├── application.yml     # Database & server configuration
│           └── db\migration\       # Flyway SQL migrations (V1, V2, V3, V4)
│
└── frontend\                       # React 18 + Vite 6 + TypeScript Frontend
    ├── package.json
    ├── vite.config.ts              # API proxy to backend (:8080)
    ├── public\                     # PWA manifest, service worker & icons
    └── src\
        ├── app\                    # App routing & AuthContext
        ├── components\             # Reusable UI components & TopBar
        ├── i18n\                   # English & Tamil dictionaries (en.ts, ta.ts)
        ├── lib\                    # Parameterized API client with CSRF
        ├── pages\                  # Admin, Security, Resident & Shared pages
        ├── scenes\                 # Three.js 3D apartment canvas
        ├── styles\                 # Curated white-background design tokens
        └── types\                  # TypeScript interface definitions
```

---

## 📄 License
GateNest is proprietary software designed for gated communities. All rights reserved.
