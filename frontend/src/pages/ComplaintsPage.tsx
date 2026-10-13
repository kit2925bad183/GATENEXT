import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { Complaint, ComplaintComment, ComplaintAttachment } from '../types';
import { Modal } from '../components/Modal';

export const ComplaintsPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [complaints, setComplaints] = useState<Complaint[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Modals
  const [showCreateModal, setShowCreateModal] = useState<boolean>(false);
  const [showDetailModal, setShowDetailModal] = useState<boolean>(false);
  const [showStatusModal, setShowStatusModal] = useState<boolean>(false);
  const [showReopenModal, setShowReopenModal] = useState<boolean>(false);

  const [selectedComplaint, setSelectedComplaint] = useState<Complaint | null>(null);

  // Create form
  const [category, setCategory] = useState<string>('PLUMBING');
  const [subject, setSubject] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [locationDetails, setLocationDetails] = useState<string>('');
  const [priority, setPriority] = useState<string>('MEDIUM');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  // Status update form
  const [newStatus, setNewStatus] = useState<string>('IN_PROGRESS');
  const [reopenReason, setReopenReason] = useState<string>('');
  const [commentText, setCommentText] = useState<string>('');

  const isAdmin = user?.role === 'ADMINISTRATOR';

  const fetchComplaints = async () => {
    setLoading(true);
    setError(null);
    try {
      const endpoint = isAdmin ? '/complaints' : '/complaints/my-complaints';
      const res = await api.get<{ data: Complaint[] }>(endpoint);
      setComplaints(res.data?.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load complaints');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchComplaints();
  }, [user]);

  const handleCreateComplaint = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post<{ data: Complaint }>('/complaints', {
        category,
        subject,
        description,
        locationDetails,
        priority
      });

      const created = res.data?.data;
      if (created && selectedFile) {
        const formData = new FormData();
        formData.append('file', selectedFile);
        await api.post(`/complaints/${created.id}/attachments`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
      }

      setShowCreateModal(false);
      setSubject('');
      setDescription('');
      setLocationDetails('');
      setSelectedFile(null);
      fetchComplaints();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to submit complaint');
    }
  };

  const openDetail = async (complaint: Complaint) => {
    try {
      const res = await api.get<{ data: Complaint }>(`/complaints/${complaint.id}`);
      setSelectedComplaint(res.data?.data || complaint);
      setShowDetailModal(true);
    } catch (err) {
      setSelectedComplaint(complaint);
      setShowDetailModal(true);
    }
  };

  const handleUpdateStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedComplaint) return;
    try {
      await api.patch(`/complaints/${selectedComplaint.id}/status`, {
        status: newStatus
      });
      setShowStatusModal(false);
      openDetail(selectedComplaint);
      fetchComplaints();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to update status');
    }
  };

  const handleReopen = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedComplaint) return;
    try {
      await api.post(`/complaints/${selectedComplaint.id}/reopen`, {
        reason: reopenReason
      });
      setShowReopenModal(false);
      setReopenReason('');
      openDetail(selectedComplaint);
      fetchComplaints();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to reopen complaint');
    }
  };

  const handleAddComment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedComplaint || !commentText.trim()) return;
    try {
      await api.post(`/complaints/${selectedComplaint.id}/comments`, {
        commentText,
        internalNote: false
      });
      setCommentText('');
      openDetail(selectedComplaint);
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to add comment');
    }
  };

  const getPriorityBadgeColor = (p: string) => {
    switch (p) {
      case 'URGENT': return { bg: '#FEF2F2', text: '#991B1B' };
      case 'HIGH': return { bg: '#FFF7ED', text: '#C2410C' };
      case 'MEDIUM': return { bg: '#EFF6FF', text: '#1D4ED8' };
      default: return { bg: '#F1F5F9', text: '#475569' };
    }
  };

  const getStatusBadgeColor = (s: string) => {
    switch (s) {
      case 'OPEN': return { bg: '#FEF3C7', text: '#92400E' };
      case 'IN_PROGRESS': return { bg: '#E0E7FF', text: '#3730A3' };
      case 'RESOLVED': return { bg: '#DEF7EC', text: '#03543F' };
      case 'CLOSED': return { bg: '#F1F5F9', text: '#475569' };
      default: return { bg: '#F1F5F9', text: '#334155' };
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('complaint.title')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Maintenance requests, plumbing, electrical, and society issues
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          style={{
            padding: '8px 16px',
            borderRadius: '8px',
            border: 'none',
            backgroundColor: '#1E40AF',
            color: '#FFFFFF',
            fontWeight: 600,
            fontSize: '13px',
            cursor: 'pointer'
          }}
        >
          + {t('complaint.new')}
        </button>
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : complaints.length === 0 ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '48px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '40px', marginBottom: '12px' }}>🔧</div>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#1E293B', margin: '0 0 6px 0' }}>No complaints found</h3>
          <p style={{ fontSize: '14px', color: '#64748B', margin: 0 }}>
            No maintenance or facility issues currently registered.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {complaints.map((c) => {
            const pColors = getPriorityBadgeColor(c.priority);
            const sColors = getStatusBadgeColor(c.status);
            return (
              <div
                key={c.id}
                onClick={() => openDetail(c)}
                style={{
                  backgroundColor: '#FFFFFF',
                  border: '1px solid #E2E8F0',
                  borderRadius: '12px',
                  padding: '18px 20px',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  cursor: 'pointer',
                  transition: 'border-color 0.15s ease',
                  boxShadow: '0 1px 3px rgba(0,0,0,0.02)'
                }}
              >
                <div style={{ flex: 1, paddingRight: '20px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
                    <span style={{ fontSize: '12px', fontWeight: 600, color: '#0F766E', backgroundColor: '#F0FDFA', padding: '2px 8px', borderRadius: '4px' }}>
                      {c.category}
                    </span>
                    <span style={{ fontSize: '11px', fontWeight: 600, backgroundColor: pColors.bg, color: pColors.text, padding: '2px 8px', borderRadius: '4px' }}>
                      {c.priority}
                    </span>
                    <span style={{ fontSize: '12px', color: '#64748B' }}>
                      #{c.id} • {new Date(c.createdAt).toLocaleDateString()}
                    </span>
                  </div>

                  <h3 style={{ fontSize: '16px', fontWeight: 600, color: '#0F172A', margin: '0 0 4px 0' }}>
                    {c.subject}
                  </h3>
                  <p style={{ fontSize: '13px', color: '#475569', margin: 0, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '650px' }}>
                    {c.description}
                  </p>
                  <div style={{ fontSize: '12px', color: '#94A3B8', marginTop: '6px' }}>
                    {c.blockName} - {c.flatNumber} • By {c.residentName}
                  </div>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <span style={{ display: 'inline-block', fontSize: '12px', fontWeight: 600, padding: '4px 10px', borderRadius: '12px', backgroundColor: sColors.bg, color: sColors.text, marginBottom: '8px' }}>
                    {c.status}
                  </span>
                  {c.reopenedCount > 0 && (
                    <div style={{ fontSize: '11px', color: '#DC2626', fontWeight: 600 }}>
                      Reopened ({c.reopenedCount}x)
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Detail Modal */}
      {showDetailModal && selectedComplaint && (
        <Modal title={`Complaint #${selectedComplaint.id}: ${selectedComplaint.subject}`} onClose={() => setShowDetailModal(false)}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', maxHeight: '75vh', overflowY: 'auto' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', gap: '8px' }}>
                <span style={{ fontSize: '12px', fontWeight: 600, color: '#0F766E', backgroundColor: '#F0FDFA', padding: '3px 8px', borderRadius: '4px' }}>
                  {selectedComplaint.category}
                </span>
                <span style={{ fontSize: '12px', fontWeight: 600, color: '#1E40AF', backgroundColor: '#EFF6FF', padding: '3px 8px', borderRadius: '4px' }}>
                  Priority: {selectedComplaint.priority}
                </span>
              </div>
              <span style={{ fontSize: '12px', fontWeight: 600, padding: '4px 12px', borderRadius: '12px', backgroundColor: getStatusBadgeColor(selectedComplaint.status).bg, color: getStatusBadgeColor(selectedComplaint.status).text }}>
                {selectedComplaint.status}
              </span>
            </div>

            <div style={{ fontSize: '14px', color: '#334155', backgroundColor: '#F8FAFC', padding: '14px', borderRadius: '8px', border: '1px solid #E2E8F0' }}>
              <div style={{ fontWeight: 600, marginBottom: '4px', color: '#0F172A' }}>Description:</div>
              <div style={{ whiteSpace: 'pre-wrap' }}>{selectedComplaint.description}</div>
              {selectedComplaint.locationDetails && (
                <div style={{ marginTop: '8px', fontSize: '13px', color: '#64748B' }}>
                  <strong>Location:</strong> {selectedComplaint.locationDetails}
                </div>
              )}
            </div>

            {/* Attachments */}
            {selectedComplaint.attachments && selectedComplaint.attachments.length > 0 && (
              <div>
                <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#0F172A', margin: '0 0 8px 0' }}>Attachments</h4>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
                  {selectedComplaint.attachments.map((att) => (
                    <a
                      key={att.id}
                      href={`/api/v1/complaints/attachments/${att.id}/download`}
                      target="_blank"
                      rel="noreferrer"
                      style={{
                        padding: '6px 12px',
                        backgroundColor: '#FFFFFF',
                        border: '1px solid #CBD5E1',
                        borderRadius: '6px',
                        fontSize: '12px',
                        color: '#1E40AF',
                        textDecoration: 'none',
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '6px'
                      }}
                    >
                      📎 {att.originalFileName} ({Math.round(att.fileSizeBytes / 1024)} KB)
                    </a>
                  ))}
                </div>
              </div>
            )}

            {/* Timeline Comments */}
            <div>
              <h4 style={{ fontSize: '14px', fontWeight: 600, color: '#0F172A', margin: '0 0 10px 0' }}>Activity Timeline</h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {selectedComplaint.comments && selectedComplaint.comments.length > 0 ? (
                  selectedComplaint.comments.map((cm) => (
                    <div key={cm.id} style={{ borderLeft: '3px solid #CBD5E1', paddingLeft: '12px', fontSize: '13px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', color: '#64748B', fontSize: '11px' }}>
                        <span><strong>{cm.authorNameSnapshot}</strong> ({cm.authorRole})</span>
                        <span>{new Date(cm.createdAt).toLocaleString()}</span>
                      </div>
                      <div style={{ color: '#1E293B', marginTop: '3px' }}>{cm.commentText}</div>
                    </div>
                  ))
                ) : (
                  <div style={{ fontSize: '13px', color: '#94A3B8' }}>No updates logged yet.</div>
                )}
              </div>
            </div>

            {/* Add Comment Form */}
            <form onSubmit={handleAddComment} style={{ display: 'flex', gap: '8px' }}>
              <input
                type="text"
                placeholder="Add a progress update or note..."
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                style={{ flex: 1, padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '13px' }}
              />
              <button
                type="submit"
                style={{ padding: '8px 14px', backgroundColor: '#1E40AF', color: '#FFFFFF', border: 'none', borderRadius: '6px', fontWeight: 600, cursor: 'pointer', fontSize: '13px' }}
              >
                Post
              </button>
            </form>

            {/* Actions */}
            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', borderTop: '1px solid #E2E8F0', paddingTop: '14px' }}>
              {isAdmin && (
                <button
                  type="button"
                  onClick={() => setShowStatusModal(true)}
                  style={{ padding: '8px 16px', backgroundColor: '#059669', color: '#FFFFFF', border: 'none', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}
                >
                  Change Status
                </button>
              )}

              {!isAdmin && (selectedComplaint.status === 'RESOLVED' || selectedComplaint.status === 'CLOSED') && (
                <button
                  type="button"
                  onClick={() => setShowReopenModal(true)}
                  style={{ padding: '8px 16px', backgroundColor: '#DC2626', color: '#FFFFFF', border: 'none', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}
                >
                  Reopen Issue
                </button>
              )}
            </div>
          </div>
        </Modal>
      )}

      {/* Create Complaint Modal */}
      {showCreateModal && (
        <Modal title="Submit New Complaint / Request" onClose={() => setShowCreateModal(false)}>
          <form onSubmit={handleCreateComplaint} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Category *
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="PLUMBING">Plumbing (Water leakage, tap, pipe)</option>
                <option value="ELECTRICAL">Electrical (Power socket, wiring, lighting)</option>
                <option value="CARPENTRY">Carpentry (Doors, windows, locks)</option>
                <option value="SECURITY">Security & Access</option>
                <option value="NOISE">Noise Disturbance</option>
                <option value="COMMON_AREA">Common Area / Corridor Maintenance</option>
                <option value="LIFT">Elevator / Lift Malfunction</option>
                <option value="OTHER">Other Issue</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Subject *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Master bathroom tap dripping constantly"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Detailed Description *
              </label>
              <textarea
                required
                rows={4}
                placeholder="Describe the problem, when it started, and any relevant details..."
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px', fontFamily: 'inherit' }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Location Details
                </label>
                <input
                  type="text"
                  placeholder="e.g. Kitchen sink, Balcony"
                  value={locationDetails}
                  onChange={(e) => setLocationDetails(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Priority
                </label>
                <select
                  value={priority}
                  onChange={(e) => setPriority(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="URGENT">Urgent</option>
                </select>
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Photo / Attachment (Optional, max 10MB)
              </label>
              <input
                type="file"
                accept="image/*,.pdf,.doc,.docx"
                onChange={(e) => setSelectedFile(e.target.files ? e.target.files[0] : null)}
                style={{ width: '100%', fontSize: '13px', color: '#64748B' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowCreateModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Submit Complaint
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Update Status Modal */}
      {showStatusModal && (
        <Modal title="Update Complaint Status" onClose={() => setShowStatusModal(false)}>
          <form onSubmit={handleUpdateStatus} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Select New Status *
              </label>
              <select
                value={newStatus}
                onChange={(e) => setNewStatus(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="OPEN">OPEN</option>
                <option value="ASSIGNED">ASSIGNED</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="CLOSED">CLOSED</option>
              </select>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowStatusModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#059669', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Save Status
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Reopen Modal */}
      {showReopenModal && (
        <Modal title="Reopen Resolved Issue" onClose={() => setShowReopenModal(false)}>
          <form onSubmit={handleReopen} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <p style={{ fontSize: '13px', color: '#64748B', margin: 0 }}>
              Explain why this issue was not fully resolved so maintenance staff can follow up.
            </p>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Reason for Reopening *
              </label>
              <textarea
                required
                rows={3}
                placeholder="e.g. Tap started leaking again after 2 hours"
                value={reopenReason}
                onChange={(e) => setReopenReason(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px', fontFamily: 'inherit' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowReopenModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#DC2626', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Confirm Reopen
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
