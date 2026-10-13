export type Role = 'ADMINISTRATOR' | 'SECURITY' | 'RESIDENT';

export type UserStatus = 'ACTIVE' | 'PENDING' | 'SUSPENDED' | 'DEACTIVATED';

export type OccupancyType = 'OWNER' | 'TENANT';

export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type VisitorCategory = 'GUEST' | 'DELIVERY' | 'SERVICE' | 'CAB' | 'OTHER';

export type VisitStatus =
  | 'PENDING_APPROVAL'
  | 'APPROVED'
  | 'INSIDE'
  | 'EXITED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'EXPIRED';

export type VisitEventType =
  | 'CREATED'
  | 'APPROVED'
  | 'REJECTED'
  | 'CHECKED_IN'
  | 'CHECKED_OUT'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'PASS_REGENERATED'
  | 'OVERRIDDEN'
  | 'NOTE_ADDED';

export type NotificationType =
  | 'VISITOR_REQUEST'
  | 'VISITOR_APPROVED'
  | 'VISITOR_REJECTED'
  | 'VISITOR_CHECKED_IN'
  | 'VISITOR_CHECKED_OUT'
  | 'RESIDENT_APPROVED'
  | 'RESIDENT_REJECTED'
  | 'PARCEL_RECEIVED'
  | 'PARCEL_COLLECTED'
  | 'COMPLAINT_UPDATE'
  | 'ANNOUNCEMENT_NEW'
  | 'ASSISTANCE_ALERT'
  | 'ASSISTANCE_RESOLVED'
  | 'STAFF_ENTRY';

export type Language = 'en' | 'ta';

export interface User {
  id: number;
  email: string;
  fullName: string;
  mobile: string;
  role: Role;
  status: UserStatus;
  lastLoginAt?: string;
  createdAt: string;
  residentProfileId?: number;
  flatId?: number;
  flatNumber?: string;
  blockName?: string;
  occupancyType?: OccupancyType;
  verificationStatus?: VerificationStatus;
  rejectionReason?: string;
  assignedGateId?: number;
  assignedGateName?: string;
  assignedGateCode?: string;
}

export type UserDto = User;

export interface ResidentProfile {
  id: number;
  userId: number;
  flatId: number;
  occupancyType: OccupancyType;
  verificationStatus: VerificationStatus;
  rejectionReason?: string;
  reviewedByUserId?: number;
  reviewedAt?: string;
  emergencyContact?: string;
  createdAt: string;
  updatedAt: string;
  userFullName?: string;
  userEmail?: string;
  userMobile?: string;
  blockName?: string;
  flatNumber?: string;
  floorNumber?: number;
}

export interface Block {
  id: number;
  name: string;
  description?: string;
  totalFloors: number;
  active: boolean;
  totalFlats?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Flat {
  id: number;
  blockId: number;
  blockName?: string;
  flatNumber: string;
  floorNumber: number;
  extensionNumber?: string;
  active: boolean;
  activeResidentsCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface Gate {
  id: number;
  name: string;
  code: string;
  description?: string;
  gateType: 'GENERAL' | 'PEDESTRIAN' | 'VEHICLE' | 'SERVICE';
  active: boolean;
  activeGuardsCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface CommunitySettings {
  id: number;
  communityName: string;
  address: string;
  city: string;
  postalCode: string;
  defaultTimezone: string;
  contactPhone?: string;
  contactEmail?: string;
  visitorPassValidityHours: number;
  demoMode?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Visit {
  id: number;
  visitReference: string;
  hostUserId: number;
  destinationFlatId: number;
  destinationBlockName: string;
  destinationFlatNumber: string;
  hostNameSnapshot: string;
  hostMobileSnapshot: string;
  visitorName: string;
  visitorMobile: string;
  visitorCategory: VisitorCategory;
  purpose: string;
  vehicleNumber?: string;
  notes?: string;
  arrivalWindowStart: string;
  arrivalWindowEnd: string;
  expectedDurationMinutes: number;
  status: VisitStatus;
  passCredentialHash?: string;
  passGeneratedAt?: string;
  entryGateId?: number;
  exitGateId?: number;
  checkInTime?: string;
  checkOutTime?: string;
  registeredByUserId: number;
  approvedByUserId?: number;
  approvedAt?: string;
  rejectionReason?: string;
  adminOverrideReason?: string;
  adminOverrideByUserId?: number;
  createdAt: string;
  updatedAt: string;
  entryGateName?: string;
  exitGateName?: string;
  registeredByUserName?: string;
  approvedByUserName?: string;
  overstay?: boolean;
}

export interface VisitEvent {
  id: number;
  visitId: number;
  eventType: VisitEventType;
  actorUserId?: number;
  gateId?: number;
  notes?: string;
  metadataJson?: string;
  createdAt: string;
  actorName?: string;
  actorRole?: string;
  gateName?: string;
}

export interface Notification {
  id: number;
  userId: number;
  title: string;
  message: string;
  linkUrl?: string;
  notificationType: NotificationType;
  read: boolean;
  createdAt: string;
}

export interface AuditLog {
  id: number;
  actorUserId?: number;
  actorEmail?: string;
  actorRole?: string;
  action: string;
  entityType: string;
  entityId?: string;
  ipAddress?: string;
  detailsJson?: string;
  createdAt: string;
}

export interface PassCredential {
  visitReference: string;
  passToken: string;
  qrCodeDataUrl?: string;
  arrivalWindowStart: string;
  arrivalWindowEnd: string;
  visitorName: string;
  destinationFlatNumber: string;
  destinationBlockName: string;
  hostName: string;
}

export interface PassVerificationResponse {
  valid: boolean;
  reason?: string;
  visit?: Visit;
  canCheckIn: boolean;
  canCheckOut: boolean;
  withinArrivalWindow: boolean;
  arrivalWindowStart?: string;
  arrivalWindowEnd?: string;
}

// Enterprise Additions
export interface SavedVisitorContact {
  id: number;
  residentUserId: number;
  visitorName: string;
  visitorMobile: string;
  visitorCategory: VisitorCategory;
  defaultPurpose?: string;
  defaultVehicleNumber?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface RecurringVisitRule {
  id: number;
  residentUserId: number;
  destinationFlatId: number;
  visitorName: string;
  visitorMobile: string;
  visitorCategory: string;
  purpose: string;
  allowedWeekdays: string;
  validFromDate: string;
  validToDate: string;
  allowedStartTime: string;
  allowedEndTime: string;
  expectedDurationMinutes: number;
  active: boolean;
  notes?: string;
  flatNumber?: string;
  blockName?: string;
  residentName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface GroupInvitation {
  id: number;
  hostUserId: number;
  destinationFlatId: number;
  eventTitle: string;
  groupReference: string;
  arrivalWindowStart: string;
  arrivalWindowEnd: string;
  maxGuests: number;
  notes?: string;
  status: 'APPROVED' | 'CANCELLED' | 'EXPIRED' | 'COMPLETED';
  flatNumber?: string;
  blockName?: string;
  hostName?: string;
  members?: GroupInvitationMember[];
  insideGuestsCount?: number;
  checkedOutGuestsCount?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface GroupInvitationMember {
  id: number;
  groupInvitationId: number;
  guestName: string;
  guestMobile?: string;
  vehicleNumber?: string;
  passCredentialHash?: string;
  status: 'EXPECTED' | 'INSIDE' | 'CHECKED_OUT' | 'CANCELLED';
  checkInTime?: string;
  checkOutTime?: string;
  entryGateId?: number;
  exitGateId?: number;
  entryGateName?: string;
  exitGateName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export type ParcelStatus = 'RECEIVED' | 'READY_FOR_COLLECTION' | 'COLLECTED' | 'RETURNED';

export interface Parcel {
  id: number;
  recipientUserId: number;
  destinationFlatId: number;
  courierName: string;
  trackingNumber?: string;
  description?: string;
  storageLocation: string;
  receivedByUserId: number;
  receivedAt: string;
  status: ParcelStatus;
  collectedByName?: string;
  collectedByUserId?: number;
  verifiedByUserId?: number;
  collectedAt?: string;
  delegateCollectorName?: string;
  delegateNotes?: string;
  recipientName?: string;
  recipientMobile?: string;
  flatNumber?: string;
  blockName?: string;
  receivedByName?: string;
  verifiedByName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ParcelEvent {
  id: number;
  parcelId: number;
  eventType: string;
  actorUserId: number;
  notes?: string;
  actorName?: string;
  createdAt: string;
}

export interface StaffMember {
  id: number;
  fullName: string;
  mobile: string;
  staffType: string;
  idProofType?: string;
  idProofNumber?: string;
  photoUrl?: string;
  active: boolean;
  notes?: string;
  authorizedFlatsCount?: number;
  currentlyInside?: boolean;
  activeSessionId?: number;
  checkInTime?: string;
  entryGateName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface StaffAuthorization {
  id: number;
  staffMemberId: number;
  residentUserId: number;
  flatId: number;
  scheduleDescription?: string;
  active: boolean;
  authorizedAt: string;
  revokedAt?: string;
  staffName?: string;
  staffMobile?: string;
  staffType?: string;
  residentName?: string;
  flatNumber?: string;
  blockName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface StaffAttendanceSession {
  id: number;
  staffMemberId: number;
  checkInTime: string;
  checkOutTime?: string;
  entryGateId: number;
  exitGateId?: number;
  entryGuardUserId: number;
  exitGuardUserId?: number;
  active: boolean;
  notes?: string;
  staffName?: string;
  staffMobile?: string;
  staffType?: string;
  entryGateName?: string;
  exitGateName?: string;
  entryGuardName?: string;
  exitGuardName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ParkingSlot {
  id: number;
  slotNumber: string;
  slotType: 'TWO_WHEELER' | 'FOUR_WHEELER' | 'EV_CHARGING' | 'VISITOR';
  blockId?: number;
  floorLevel: string;
  reservedForResidents: boolean;
  active: boolean;
  blockName?: string;
  occupied?: boolean;
  currentAssignmentId?: number;
  occupiedByVehicleNumber?: string;
  occupiedByResidentName?: string;
  occupiedByVisitorName?: string;
  currentVisitReference?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Vehicle {
  id: number;
  ownerUserId: number;
  flatId: number;
  vehicleNumber: string;
  vehicleType: 'TWO_WHEELER' | 'FOUR_WHEELER' | 'EV';
  makeModel?: string;
  color?: string;
  stickerNumber?: string;
  active: boolean;
  ownerName?: string;
  flatNumber?: string;
  blockName?: string;
  assignedSlotNumber?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface ParkingAssignment {
  id: number;
  parkingSlotId: number;
  vehicleId?: number;
  visitId?: number;
  residentUserId?: number;
  assignedByUserId: number;
  assignedAt: string;
  releasedAt?: string;
  active: boolean;
  notes?: string;
  slotNumber?: string;
  slotType?: string;
  vehicleNumber?: string;
  visitReference?: string;
  visitorName?: string;
  residentName?: string;
  assignedByName?: string;
}

export interface Complaint {
  id: number;
  residentUserId: number;
  flatId: number;
  category: 'PLUMBING' | 'ELECTRICAL' | 'CARPENTRY' | 'SECURITY' | 'NOISE' | 'COMMON_AREA' | 'LIFT' | 'OTHER';
  subject: string;
  description: string;
  locationDetails?: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
  status: 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  assignedToUserId?: number;
  resolvedAt?: string;
  closedAt?: string;
  reopenedCount: number;
  residentName?: string;
  residentMobile?: string;
  flatNumber?: string;
  blockName?: string;
  assignedToName?: string;
  comments?: ComplaintComment[];
  attachments?: ComplaintAttachment[];
  createdAt: string;
  updatedAt: string;
}

export interface ComplaintComment {
  id: number;
  complaintId: number;
  authorUserId: number;
  authorNameSnapshot: string;
  authorRole: string;
  commentText: string;
  internalNote: boolean;
  createdAt: string;
}

export interface ComplaintAttachment {
  id: number;
  complaintId: number;
  fileName: string;
  originalFileName: string;
  fileSizeBytes: number;
  mimeType: string;
  storagePath: string;
  uploadedByUserId: number;
  createdAt: string;
}

export interface Announcement {
  id: number;
  authorUserId: number;
  title: string;
  content: string;
  announcementType: 'GENERAL' | 'MAINTENANCE' | 'WATER_POWER' | 'EVENT' | 'EMERGENCY';
  priority: 'NORMAL' | 'IMPORTANT' | 'URGENT';
  audienceType: 'COMMUNITY' | 'BLOCK';
  targetBlockId?: number;
  pinned: boolean;
  published: boolean;
  publishedAt: string;
  expiresAt?: string;
  archived: boolean;
  authorName?: string;
  targetBlockName?: string;
  readByCurrentUser?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface SecurityAssistanceRequest {
  id: number;
  residentUserId: number;
  flatId: number;
  category: 'MEDICAL_ALERT' | 'NOISE_DISTURBANCE' | 'SUSPICIOUS_ACTIVITY' | 'PARKING_BLOCK' | 'EMERGENCY_HELP' | 'GENERAL';
  locationDetails?: string;
  description?: string;
  status: 'SUBMITTED' | 'ACKNOWLEDGED' | 'RESOLVED' | 'CANCELLED';
  submittedAt: string;
  acknowledgedByUserId?: number;
  acknowledgedAt?: string;
  resolvedByUserId?: number;
  resolvedAt?: string;
  resolutionNotes?: string;
  residentName?: string;
  residentMobile?: string;
  flatNumber?: string;
  blockName?: string;
  acknowledgedByName?: string;
  resolvedByName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface SecurityHandoverNote {
  id: number;
  shiftName: string;
  gateId: number;
  authorUserId: number;
  noteText: string;
  acknowledged: boolean;
  acknowledgedByUserId?: number;
  acknowledgedAt?: string;
  gateName?: string;
  authorName?: string;
  acknowledgedByName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface UserNotificationPreferences {
  id: number;
  userId: number;
  visitorAlerts: boolean;
  parcelAlerts: boolean;
  complaintAlerts: boolean;
  noticeAlerts: boolean;
  assistanceAlerts: boolean;
}

export interface DashboardStats {
  activeResidents?: number;
  pendingRegistrations?: number;
  visitorsInside?: number;
  entriesToday?: number;
  exitsToday?: number;
  pendingVisitorApprovals?: number;
  waitingApprovals?: number;
  expectedVisitorsToday?: number;
  currentVisitors?: number;
  overdueVisits?: number;
  upcomingInvitations?: number;
  requestsAwaitingApproval?: number;
  ownVisitorsInside?: number;
  ownCompletedVisits?: number;
  pendingParcels?: number;
  openComplaints?: number;
  staffInside?: number;
  activeAssistance?: number;
  dailyEntries?: Array<{ entry_date: string; total_count: number }>;
}

export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  errors?: Record<string, string>;
  timestamp: string;
}

export interface PagedResponse<T> {
  items: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}
