import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { SavedVisitorContact, VisitorCategory } from '../types';
import { Modal } from '../components/Modal';

export const SavedContactsPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();
  const navigate = useNavigate();

  const [contacts, setContacts] = useState<SavedVisitorContact[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Modal
  const [showModal, setShowModal] = useState<boolean>(false);
  const [editingContact, setEditingContact] = useState<SavedVisitorContact | null>(null);

  // Form
  const [visitorName, setVisitorName] = useState<string>('');
  const [visitorMobile, setVisitorMobile] = useState<string>('');
  const [visitorCategory, setVisitorCategory] = useState<VisitorCategory>('GUEST');
  const [defaultPurpose, setDefaultPurpose] = useState<string>('Personal Guest Visit');
  const [defaultVehicleNumber, setDefaultVehicleNumber] = useState<string>('');
  const [notes, setNotes] = useState<string>('');

  const fetchContacts = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<{ data: SavedVisitorContact[] }>('/resident/saved-contacts');
      setContacts(res.data?.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load saved contacts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchContacts();
  }, []);

  const openAddModal = () => {
    setEditingContact(null);
    setVisitorName('');
    setVisitorMobile('');
    setVisitorCategory('GUEST');
    setDefaultPurpose('Personal Guest Visit');
    setDefaultVehicleNumber('');
    setNotes('');
    setShowModal(true);
  };

  const openEditModal = (c: SavedVisitorContact) => {
    setEditingContact(c);
    setVisitorName(c.visitorName);
    setVisitorMobile(c.visitorMobile);
    setVisitorCategory(c.visitorCategory);
    setDefaultPurpose(c.defaultPurpose || '');
    setDefaultVehicleNumber(c.defaultVehicleNumber || '');
    setNotes(c.notes || '');
    setShowModal(true);
  };

  const handleSaveContact = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingContact) {
        await api.put(`/resident/saved-contacts/${editingContact.id}`, {
          visitorName,
          visitorMobile,
          visitorCategory,
          defaultPurpose,
          defaultVehicleNumber,
          notes
        });
      } else {
        await api.post('/resident/saved-contacts', {
          visitorName,
          visitorMobile,
          visitorCategory,
          defaultPurpose,
          defaultVehicleNumber,
          notes
        });
      }
      setShowModal(false);
      fetchContacts();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to save contact');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this saved contact?')) return;
    try {
      await api.delete(`/resident/saved-contacts/${id}`);
      fetchContacts();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to delete contact');
    }
  };

  const handleInviteAgain = (contact: SavedVisitorContact) => {
    // Navigate to Invite Visitor page with prefilled query state
    navigate(`/invite-visitor?name=${encodeURIComponent(contact.visitorName)}&mobile=${encodeURIComponent(contact.visitorMobile)}&cat=${contact.visitorCategory}&purpose=${encodeURIComponent(contact.defaultPurpose || '')}`);
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1100px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('nav.savedContacts')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Frequently visiting friends, family, and regular drivers for 1-click invitations
          </p>
        </div>

        <button
          onClick={openAddModal}
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
          + Add New Contact
        </button>
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : contacts.length === 0 ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '48px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '40px', marginBottom: '12px' }}>👥</div>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#1E293B', margin: '0 0 6px 0' }}>No saved contacts</h3>
          <p style={{ fontSize: '14px', color: '#64748B', margin: '0 0 16px 0' }}>
            Save frequent visitors to invite them again with one click.
          </p>
          <button
            onClick={openAddModal}
            style={{ padding: '8px 16px', backgroundColor: '#1E40AF', color: '#FFFFFF', border: 'none', borderRadius: '6px', fontWeight: 600, cursor: 'pointer' }}
          >
            Add Your First Contact
          </button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
          {contacts.map((c) => (
            <div
              key={c.id}
              style={{
                backgroundColor: '#FFFFFF',
                border: '1px solid #E2E8F0',
                borderRadius: '12px',
                padding: '20px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.02)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                  <span style={{ fontSize: '12px', fontWeight: 600, color: '#0F766E', backgroundColor: '#F0FDFA', padding: '2px 8px', borderRadius: '4px' }}>
                    {c.visitorCategory}
                  </span>
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <button
                      onClick={() => openEditModal(c)}
                      style={{ padding: '3px 8px', fontSize: '12px', color: '#475569', backgroundColor: '#F1F5F9', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                    >
                      Edit
                    </button>
                    <button
                      onClick={() => handleDelete(c.id)}
                      style={{ padding: '3px 8px', fontSize: '12px', color: '#DC2626', backgroundColor: '#FEF2F2', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                    >
                      ✕
                    </button>
                  </div>
                </div>

                <h3 style={{ fontSize: '17px', fontWeight: 600, color: '#0F172A', margin: '0 0 4px 0' }}>
                  {c.visitorName}
                </h3>
                <div style={{ fontSize: '13px', color: '#64748B', marginBottom: '10px' }}>
                  📞 {c.visitorMobile}
                </div>

                <div style={{ fontSize: '13px', color: '#475569', lineHeight: '1.5' }}>
                  {c.defaultPurpose && <div><strong>Default Purpose:</strong> {c.defaultPurpose}</div>}
                  {c.defaultVehicleNumber && <div><strong>Vehicle:</strong> {c.defaultVehicleNumber}</div>}
                  {c.notes && <div style={{ fontSize: '12px', color: '#94A3B8', marginTop: '4px' }}>"{c.notes}"</div>}
                </div>
              </div>

              <div style={{ marginTop: '16px', borderTop: '1px solid #F1F5F9', paddingTop: '12px' }}>
                <button
                  onClick={() => handleInviteAgain(c)}
                  style={{
                    width: '100%',
                    padding: '8px',
                    fontSize: '13px',
                    fontWeight: 600,
                    color: '#FFFFFF',
                    backgroundColor: '#1E40AF',
                    border: 'none',
                    borderRadius: '6px',
                    cursor: 'pointer'
                  }}
                >
                  ⚡ Invite Again
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add / Edit Contact Modal */}
      {showModal && (
        <Modal title={editingContact ? 'Edit Saved Contact' : 'Save Visitor Contact'} onClose={() => setShowModal(false)}>
          <form onSubmit={handleSaveContact} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Visitor Full Name *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Anand Mahindra"
                value={visitorName}
                onChange={(e) => setVisitorName(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Mobile Number *
              </label>
              <input
                required
                type="text"
                placeholder="+91 98 4500 0000"
                value={visitorMobile}
                onChange={(e) => setVisitorMobile(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Category
              </label>
              <select
                value={visitorCategory}
                onChange={(e) => setVisitorCategory(e.target.value as VisitorCategory)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              >
                <option value="GUEST">{t('cat.GUEST')}</option>
                <option value="SERVICE">{t('cat.SERVICE')}</option>
                <option value="DELIVERY">{t('cat.DELIVERY')}</option>
                <option value="CAB">{t('cat.CAB')}</option>
                <option value="OTHER">{t('cat.OTHER')}</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Default Purpose
              </label>
              <input
                type="text"
                placeholder="e.g. Family dinner, Tutoring"
                value={defaultPurpose}
                onChange={(e) => setDefaultPurpose(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Vehicle Registration Number (Optional)
              </label>
              <input
                type="text"
                placeholder="e.g. KA-05-MM-1234"
                value={defaultVehicleNumber}
                onChange={(e) => setDefaultVehicleNumber(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Save Contact
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
