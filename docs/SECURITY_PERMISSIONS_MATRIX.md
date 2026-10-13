# GateNest Security & RBAC Permissions Matrix

This document defines role-based access control (RBAC) and record-level authorization across the GateNest system.

---

## 🛡️ Role Matrix

| Resource / Action | Administrator | Security Staff | Resident | Anonymous |
| :--- | :---: | :---: | :---: | :---: |
| **Authentication & Registration** | | | | |
| Self Register (Resident) | ❌ | ❌ | ❌ | ✅ |
| Login / Logout | ✅ | ✅ | ✅ | ❌ |
| View / Edit Own Profile | ✅ | ✅ | ✅ | ❌ |
| Approve Resident Registrations | ✅ | ❌ | ❌ | ❌ |
| **Community Setup** | | | | |
| View Public Blocks/Flats | ✅ | ✅ | ✅ | ✅ |
| Manage Blocks, Flats & Gates | ✅ | ❌ | ❌ | ❌ |
| Manage Security Personnel | ✅ | ❌ | ❌ | ❌ |
| View Audit Logs | ✅ | ❌ | ❌ | ❌ |
| **Visitor & Pass Management** | | | | |
| Create Pre-Invitation Pass | ✅ | ❌ | ✅ (Own Flat Only) | ❌ |
| Verify Pass / QR Code | ✅ | ✅ | ❌ | ❌ |
| Register Walk-In Visitor | ❌ | ✅ | ❌ | ❌ |
| Approve / Reject Walk-In | ✅ (Override) | ❌ | ✅ (Own Flat Only) | ❌ |
| Check-In / Check-Out Visitor | ✅ | ✅ | ❌ | ❌ |
| View Active Visitors Inside | ✅ (All) | ✅ (All) | ✅ (Own Visitors) | ❌ |
| View Visitor History | ✅ (All) | ✅ (All) | ✅ (Own Visitors) | ❌ |
| Export Visitor History (CSV) | ✅ | ✅ | ❌ | ❌ |
| **Saved Contacts & Recurring Rules** | | | | |
| Manage Saved Contacts | ❌ | ❌ | ✅ (Own Contacts) | ❌ |
| Create / Toggle Recurring Rule | ❌ | ❌ | ✅ (Own Flat) | ❌ |
| Check-In Recurring Rule Visitor | ✅ | ✅ | ❌ | ❌ |
| **Group Event Passes** | | | | |
| Create Group Event Pass | ❌ | ❌ | ✅ (Own Flat) | ❌ |
| Check-In Group Member | ✅ | ✅ | ❌ | ❌ |
| **Parcel Locker** | | | | |
| Receive / Log Parcel | ✅ | ✅ | ❌ | ❌ |
| Authorize Delegate Collector | ❌ | ❌ | ✅ (Own Parcel) | ❌ |
| Handover / Collect Parcel | ✅ | ✅ | ❌ | ❌ |
| View Parcel Details | ✅ (All) | ✅ (All) | ✅ (Own Parcels) | ❌ |
| **Domestic Staff** | | | | |
| Register Staff Member | ✅ | ✅ | ❌ | ❌ |
| Authorize Staff for Flat | ❌ | ❌ | ✅ (Own Flat) | ❌ |
| Staff Entry / Exit Check-In | ✅ | ✅ | ❌ | ❌ |
| View Active Staff On-Premises | ✅ | ✅ | ✅ (Authorized Only) | ❌ |
| **Parking & Vehicles** | | | | |
| Register Resident Vehicle | ✅ | ❌ | ✅ (Own Flat) | ❌ |
| Allocate Visitor Parking Slot | ✅ | ✅ | ❌ | ❌ |
| Release Parking Slot | ✅ | ✅ | ❌ | ❌ |
| **Complaints & Maintenance** | | | | |
| Submit Complaint | ❌ | ❌ | ✅ | ❌ |
| Assign / Update Status | ✅ | ❌ | ❌ | ❌ |
| Add Comment / View Timeline | ✅ | ❌ | ✅ (Own Complaint) | ❌ |
| Reopen Resolved Complaint | ❌ | ❌ | ✅ (Within Window) | ❌ |
| Download Private Attachment | ✅ | ❌ | ✅ (Own Complaint) | ❌ |
| **Notice Board & Announcements** | | | | |
| Create / Publish / Pin Notice | ✅ | ❌ | ❌ | ❌ |
| Read Targeted Announcements | ✅ | ✅ | ✅ (Community / Block) | ❌ |
| **Emergency Assistance & Handover** | | | | |
| Trigger Assistance / SOS Alert | ❌ | ❌ | ✅ | ❌ |
| Acknowledge / Resolve Alert | ✅ | ✅ | ❌ | ❌ |
| Create / Acknowledge Handover Note | ✅ | ✅ | ❌ | ❌ |

---

## 🔒 Record-Level Security Constraints

1. **Resident Data Privacy**:
   - Residents can **never** query or access visitor invitations, parcels, complaints, vehicles, or saved contacts belonging to other flats.
   - All resident queries enforce `WHERE resident_user_id = :currentUserId` or `WHERE destination_flat_id = :residentFlatId`.
2. **Double-Collection Prevention**:
   - Parcel transitions require `status = 'RECEIVED'` or `'READY_FOR_COLLECTION'` under transaction lock to prevent duplicate handovers.
3. **Staff Single Active Session**:
   - Guards cannot check in a staff member who already has an open active session without checking out first.
4. **CSV Export Formula Injection Defense**:
   - All cell contents starting with `=`, `+`, `-`, `@`, `\t`, `\r` are prefixed with `'` to neutralize formula execution in Excel/Calc.
