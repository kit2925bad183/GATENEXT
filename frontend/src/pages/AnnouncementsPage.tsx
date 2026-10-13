import React, { useState, useEffect } from 'react';
import { useAuth } from '../app/AuthContext';
import { useI18n } from '../i18n/I18nContext';
import { api } from '../lib/api';
import { Announcement, Block } from '../types';
import { Modal } from '../components/Modal';

export const AnnouncementsPage: React.FC = () => {
  const { user } = useAuth();
  const { t } = useI18n();

  const [announcements, setAnnouncements] = useState<Announcement[]>([]);
  const [blocks, setBlocks] = useState<Block[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Publish form
  const [showPublishModal, setShowPublishModal] = useState<boolean>(false);
  const [title, setTitle] = useState<string>('');
  const [content, setContent] = useState<string>('');
  const [announcementType, setAnnouncementType] = useState<string>('GENERAL');
  const [priority, setPriority] = useState<string>('NORMAL');
  const [audienceType, setAudienceType] = useState<string>('COMMUNITY');
  const [targetBlockId, setTargetBlockId] = useState<string>('');
  const [isPinned, setIsPinned] = useState<boolean>(false);

  const isAdmin = user?.role === 'ADMINISTRATOR';

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [annRes, blocksRes] = await Promise.all([
        api.get<{ data: Announcement[] }>('/announcements/feed'),
        api.get<{ data: Block[] }>('/community/blocks')
      ]);
      setAnnouncements(annRes.data?.data || []);
      setBlocks(blocksRes.data?.data || []);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load announcements');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [user]);

  const handleMarkAsRead = async (id: number) => {
    try {
      await api.post(`/announcements/${id}/read`);
      setAnnouncements(prev => prev.map(a => a.id === id ? { ...a, readByCurrentUser: true } : a));
    } catch (err) {
      console.error('Failed to mark read', err);
    }
  };

  const handlePublish = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/announcements', {
        title,
        content,
        announcementType,
        priority,
        audienceType,
        targetBlockId: targetBlockId ? Number(targetBlockId) : null,
        pinned: isPinned,
        published: true
      });
      setShowPublishModal(false);
      setTitle('');
      setContent('');
      setIsPinned(false);
      fetchData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Failed to publish announcement');
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'EMERGENCY': return { bg: '#FEF2F2', text: '#991B1B' };
      case 'WATER_POWER': return { bg: '#EFF6FF', text: '#1D4ED8' };
      case 'MAINTENANCE': return { bg: '#FFF7ED', text: '#C2410C' };
      case 'EVENT': return { bg: '#FDF4FF', text: '#86198F' };
      default: return { bg: '#F1F5F9', text: '#334155' };
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '1000px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
        <div>
          <h1 style={{ fontSize: '24px', fontWeight: 700, color: '#0F172A', margin: 0 }}>
            {t('announcement.title')}
          </h1>
          <p style={{ fontSize: '13px', color: '#64748B', margin: '4px 0 0 0' }}>
            Official notices, maintenance schedules, and community events
          </p>
        </div>

        {isAdmin && (
          <button
            onClick={() => setShowPublishModal(true)}
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
            + {t('announcement.publish')}
          </button>
        )}
      </div>

      {error && (
        <div style={{ backgroundColor: '#FEF2F2', border: '1px solid #F87171', color: '#991B1B', padding: '12px 16px', borderRadius: '8px', marginBottom: '20px', fontSize: '14px' }}>
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ padding: '40px', textAlign: 'center', color: '#64748B' }}>{t('common.loading')}</div>
      ) : announcements.length === 0 ? (
        <div style={{ backgroundColor: '#FFFFFF', borderRadius: '12px', padding: '48px', textAlign: 'center', border: '1px solid #E2E8F0' }}>
          <div style={{ fontSize: '40px', marginBottom: '12px' }}>📢</div>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#1E293B', margin: '0 0 6px 0' }}>No announcements</h3>
          <p style={{ fontSize: '14px', color: '#64748B', margin: 0 }}>
            There are no active notices published on the bulletin board.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {announcements.map((ann) => {
            const tColors = getTypeColor(ann.announcementType);
            return (
              <div
                key={ann.id}
                onClick={() => handleMarkAsRead(ann.id)}
                style={{
                  backgroundColor: '#FFFFFF',
                  border: ann.pinned ? '2px solid #3B82F6' : '1px solid #E2E8F0',
                  borderRadius: '12px',
                  padding: '20px',
                  boxShadow: ann.pinned ? '0 4px 6px -1px rgba(59, 130, 246, 0.1)' : '0 1px 3px rgba(0,0,0,0.02)',
                  position: 'relative'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {ann.pinned && (
                      <span style={{ fontSize: '11px', fontWeight: 700, color: '#FFFFFF', backgroundColor: '#2563EB', padding: '2px 8px', borderRadius: '10px' }}>
                        📌 PINNED
                      </span>
                    )}
                    <span style={{ fontSize: '12px', fontWeight: 600, backgroundColor: tColors.bg, color: tColors.text, padding: '2px 8px', borderRadius: '4px' }}>
                      {ann.announcementType}
                    </span>
                    <span style={{ fontSize: '12px', color: '#64748B' }}>
                      Audience: {ann.audienceType === 'COMMUNITY' ? 'Entire Community' : `Block ${ann.targetBlockName || ''}`}
                    </span>
                  </div>

                  <span style={{ fontSize: '12px', color: '#94A3B8' }}>
                    {new Date(ann.publishedAt).toLocaleDateString()}
                  </span>
                </div>

                <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#0F172A', margin: '0 0 8px 0' }}>
                  {ann.title}
                </h3>

                <p style={{ fontSize: '14px', color: '#334155', lineHeight: '1.6', margin: 0, whiteSpace: 'pre-wrap' }}>
                  {ann.content}
                </p>

                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '14px', paddingTop: '10px', borderTop: '1px solid #F1F5F9', fontSize: '12px', color: '#94A3B8' }}>
                  <span>Published by <strong>{ann.authorName || 'Management Office'}</strong></span>
                  {ann.readByCurrentUser ? (
                    <span style={{ color: '#059669', fontWeight: 500 }}>✓ Read</span>
                  ) : (
                    <span style={{ color: '#2563EB', fontWeight: 600 }}>● New notice</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Publish Announcement Modal */}
      {showPublishModal && (
        <Modal title="Publish Notice / Announcement" onClose={() => setShowPublishModal(false)}>
          <form onSubmit={handlePublish} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Announcement Title *
              </label>
              <input
                required
                type="text"
                placeholder="e.g. Water tank cleaning schedule for Block A & B"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Notice Category
                </label>
                <select
                  value={announcementType}
                  onChange={(e) => setAnnouncementType(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                >
                  <option value="GENERAL">General Announcement</option>
                  <option value="MAINTENANCE">Maintenance Notice</option>
                  <option value="WATER_POWER">Water / Power Interruption</option>
                  <option value="EVENT">Community Event</option>
                  <option value="EMERGENCY">Urgent / Emergency Alert</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Target Audience
                </label>
                <select
                  value={audienceType}
                  onChange={(e) => setAudienceType(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                >
                  <option value="COMMUNITY">Entire Community</option>
                  <option value="BLOCK">Specific Block Only</option>
                </select>
              </div>
            </div>

            {audienceType === 'BLOCK' && (
              <div>
                <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                  Select Block
                </label>
                <select
                  value={targetBlockId}
                  onChange={(e) => setTargetBlockId(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px' }}
                >
                  <option value="">-- Choose Target Block --</option>
                  {blocks.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.name}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 600, color: '#334155', marginBottom: '4px' }}>
                Notice Content *
              </label>
              <textarea
                required
                rows={5}
                placeholder="Write the full notice details..."
                value={content}
                onChange={(e) => setContent(e.target.value)}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '14px', fontFamily: 'inherit' }}
              />
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <input
                type="checkbox"
                id="pinNotice"
                checked={isPinned}
                onChange={(e) => setIsPinned(e.target.checked)}
                style={{ width: '16px', height: '16px' }}
              />
              <label htmlFor="pinNotice" style={{ fontSize: '14px', color: '#334155', cursor: 'pointer' }}>
                Pin this notice to the top of the board
              </label>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setShowPublishModal(false)}
                style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #CBD5E1', backgroundColor: '#FFFFFF', cursor: 'pointer' }}
              >
                Cancel
              </button>
              <button
                type="submit"
                style={{ padding: '8px 18px', borderRadius: '6px', border: 'none', backgroundColor: '#1E40AF', color: '#FFFFFF', fontWeight: 600, cursor: 'pointer' }}
              >
                Publish Notice
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
};
