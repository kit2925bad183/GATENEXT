# GateNest Enterprise API Documentation

GateNest provides a comprehensive RESTful API under the base path `/api/v1`.

---

## 1. Authentication & Global Headers

- **Session Management**: Persistent HTTP sessions via Spring Session JDBC (`JSESSIONID` HttpOnly cookie).
- **CSRF Protection**: Cookie-based CSRF token (`XSRF-TOKEN` cookie). Mutating HTTP methods (`POST`, `PUT`, `PATCH`, `DELETE`) must include the header `X-XSRF-TOKEN` matching the cookie value.
- **Content-Type**: `application/json` (or `multipart/form-data` for file uploads, `text/csv` for reports export).
- **Standard Envelope Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully",
    "data": { ... }
  }
  ```

---

## 2. Authentication & Profile Endpoints (`/api/v1/auth`, `/api/v1/profile`)

- `POST /api/v1/auth/bootstrap-admin`: Create initial administrator account if 0 admins exist.
- `POST /api/v1/auth/login`: Authenticate with email/password.
- `POST /api/v1/auth/logout`: Invalidate session and clear auth cookies.
- `POST /api/v1/auth/register-resident`: Self-registration for residents with flat selection.
- `GET /api/v1/auth/me`: Retrieve currently logged-in user profile.
- `PUT /api/v1/profile`: Update contact details or name.
- `PUT /api/v1/profile/password`: Update password.

---

## 3. Community Structure Endpoints (`/api/v1/community`)

- `GET /api/v1/community/public-structure`: Public list of blocks and flats for registration dropdowns.
- `GET /api/v1/community/blocks`: Full list of blocks.
- `GET /api/v1/community/blocks/{id}/flats`: Flats belonging to a specific block.
- `GET /api/v1/community/gates`: Configured security gates.

---

## 4. Visitor Management Endpoints (`/api/v1/visits`)

- `POST /api/v1/visits/pre-invitation`: Resident pre-invites a visitor; returns SHA-256 protected QR pass.
- `POST /api/v1/visits/walk-in`: Security registers an unexpected visitor at the gate.
- `GET /api/v1/visits/verify-pass?token=...`: Security validates a QR code or pass reference.
- `POST /api/v1/visits/{id}/check-in`: Security records physical visitor entry (with optional parking slot).
- `POST /api/v1/visits/{id}/check-out`: Security records visitor departure (releases allocated parking slot).
- `POST /api/v1/visits/{id}/approve`: Resident approves walk-in visitor.
- `POST /api/v1/visits/{id}/reject`: Resident rejects walk-in visitor.
- `POST /api/v1/visits/{id}/cancel`: Resident cancels an upcoming pre-invitation.
- `GET /api/v1/visits/currently-inside`: List of active visitors inside the community.
- `GET /api/v1/visits/waiting-approvals`: List of walk-ins pending resident approval.
- `GET /api/v1/visits/history`: Filterable and paginated visitor history.

---

## 5. Saved Contacts & 1-Click Repeat Invites (`/api/v1/resident/saved-contacts`)

- `GET /api/v1/resident/saved-contacts`: List frequent visitor contacts saved by the logged-in resident.
- `POST /api/v1/resident/saved-contacts`: Save a frequent visitor contact.
- `PUT /api/v1/resident/saved-contacts/{id}`: Update saved contact details.
- `DELETE /api/v1/resident/saved-contacts/{id}`: Delete a saved contact.

---

## 6. Recurring Visitor Rules (`/api/v1/recurring-rules`)

- `GET /api/v1/recurring-rules`: List recurring rules for current resident or all active rules for security.
- `POST /api/v1/recurring-rules`: Create recurring rule (weekdays, start/end dates, permitted hours).
- `PATCH /api/v1/recurring-rules/{id}/status?active=true/false`: Activate or deactivate recurring rule.

---

## 7. Group Invitations (`/api/v1/group-invitations`)

- `GET /api/v1/group-invitations`: List group events and party passes for resident or gate desk.
- `GET /api/v1/group-invitations/{id}`: Fetch group event details and guest attendance list.
- `POST /api/v1/group-invitations`: Create multi-guest party invitation.
- `POST /api/v1/group-invitations/members/{memberId}/check-in`: Admit individual group member (separate occupancy record).
- `POST /api/v1/group-invitations/members/{memberId}/check-out`: Check out individual group member.
- `PATCH /api/v1/group-invitations/{id}/cancel`: Cancel group invitation.

---

## 8. Parcel Management (`/api/v1/parcels`)

- `GET /api/v1/parcels`: List parcels (filtered by resident flat or active parcels for security).
- `POST /api/v1/parcels`: Security receives and logs a package for a resident.
- `PATCH /api/v1/parcels/{id}/delegate`: Resident authorizes a delegate to pick up package.
- `POST /api/v1/parcels/{id}/collect`: Security confirms parcel handover after identity verification.
- `GET /api/v1/parcels/{id}/events`: Audit history log of parcel status changes.

---

## 9. Domestic Staff & Attendance (`/api/v1/staff`)

- `GET /api/v1/staff`: List registered domestic staff members.
- `POST /api/v1/staff`: Admin/Security registers staff member (cleaner, cook, driver, etc.).
- `GET /api/v1/staff/authorizations`: List flat authorizations for staff.
- `POST /api/v1/staff/authorizations`: Resident authorizes a staff member for their flat.
- `DELETE /api/v1/staff/authorizations/{id}`: Resident revokes authorization.
- `POST /api/v1/staff/{id}/check-in`: Guard records staff entry (enforcing single active session constraint).
- `POST /api/v1/staff/{id}/check-out`: Guard records staff exit.
- `GET /api/v1/staff/attendance/active`: List staff currently on premises.

---

## 10. Vehicles & Parking Management (`/api/v1/parking`)

- `GET /api/v1/parking/slots`: Real-time parking slot grid with allocation status.
- `GET /api/v1/parking/vehicles`: List resident vehicles (filtered by flat for residents).
- `POST /api/v1/parking/vehicles`: Resident registers a personal vehicle.
- `POST /api/v1/parking/slots/{id}/allocate-visitor`: Guard allocates slot to visitor during check-in.
- `POST /api/v1/parking/slots/{id}/release`: Guard releases slot during check-out.

---

## 11. Complaints & Maintenance Requests (`/api/v1/complaints`)

- `GET /api/v1/complaints`: List complaints (resident sees their own, admin sees all).
- `POST /api/v1/complaints`: Resident files a complaint.
- `PATCH /api/v1/complaints/{id}/status`: Admin updates status (`OPEN` ➔ `ASSIGNED` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`).
- `POST /api/v1/complaints/{id}/comments`: Add comment to complaint timeline.
- `POST /api/v1/complaints/{id}/reopen`: Resident reopens a resolved complaint.
- `POST /api/v1/complaints/{id}/attachments`: Upload photo attachment with server-validated mime type.
- `GET /api/v1/complaints/attachments/{attachmentId}/download`: Authorized image download endpoint.

---

## 12. Community Announcements (`/api/v1/announcements`)

- `GET /api/v1/announcements`: List published notices targeting the user's community or block.
- `POST /api/v1/announcements`: Admin drafts or publishes a notice.
- `PUT /api/v1/announcements/{id}`: Admin edits notice.
- `DELETE /api/v1/announcements/{id}`: Admin archives notice.
- `POST /api/v1/announcements/{id}/read`: Mark notice as read.

---

## 13. Security Assistance & Shift Handover (`/api/v1/assistance`, `/api/v1/security/handover`)

- `POST /api/v1/assistance`: Resident triggers emergency assistance / SOS alert to all on-duty guards.
- `GET /api/v1/assistance/active`: Security fetches open assistance alerts.
- `PATCH /api/v1/assistance/{id}/status`: Guard acknowledges or resolves assistance request.
- `GET /api/v1/security/handover/notes`: List shift transition handover notes.
- `POST /api/v1/security/handover/notes`: Guard creates shift handover note.
- `POST /api/v1/security/handover/notes/{id}/acknowledge`: Incoming guard acknowledges handover note.

---

## 14. Reports & Analytics (`/api/v1/reports`)

- `GET /api/v1/reports/summary`: Community KPI counters.
- `GET /api/v1/reports/categories`: Visitor breakdown by category.
- `GET /api/v1/reports/gate-traffic`: Hourly and daily traffic per gate.
- `GET /api/v1/reports/daily-trends?days=7`: Multi-day check-in/out trends.
- `GET /api/v1/reports/export/visits.csv`: Export visitor history to CSV with formula injection sanitization.
