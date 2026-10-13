package com.gatenest.repository;

import com.gatenest.model.Complaint;
import com.gatenest.model.ComplaintAttachment;
import com.gatenest.model.ComplaintComment;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ComplaintRepository {

    private final JdbcTemplate jdbcTemplate;

    public ComplaintRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Complaint> complaintRowMapper = (rs, rowNum) -> {
        Complaint c = new Complaint();
        c.setId(rs.getLong("id"));
        c.setResidentUserId(rs.getLong("resident_user_id"));
        c.setFlatId(rs.getLong("flat_id"));
        c.setCategory(rs.getString("category"));
        c.setSubject(rs.getString("subject"));
        c.setDescription(rs.getString("description"));
        c.setLocationDetails(rs.getString("location_details"));
        c.setPriority(rs.getString("priority"));
        c.setStatus(rs.getString("status"));
        long assignedId = rs.getLong("assigned_to_user_id");
        c.setAssignedToUserId(rs.wasNull() ? null : assignedId);
        Timestamp resAt = rs.getTimestamp("resolved_at");
        c.setResolvedAt(resAt != null ? resAt.toInstant() : null);
        Timestamp clsAt = rs.getTimestamp("closed_at");
        c.setClosedAt(clsAt != null ? clsAt.toInstant() : null);
        c.setReopenedCount(rs.getInt("reopened_count"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        c.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        c.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            c.setResidentName(rs.getString("resident_name"));
            c.setResidentMobile(rs.getString("resident_mobile"));
            c.setFlatNumber(rs.getString("flat_number"));
            c.setBlockName(rs.getString("block_name"));
            c.setAssignedToName(rs.getString("assigned_to_name"));
        } catch (Exception ignored) {}

        return c;
    };

    private final RowMapper<ComplaintComment> commentRowMapper = (rs, rowNum) -> {
        ComplaintComment cc = new ComplaintComment();
        cc.setId(rs.getLong("id"));
        cc.setComplaintId(rs.getLong("complaint_id"));
        cc.setAuthorUserId(rs.getLong("author_user_id"));
        cc.setAuthorNameSnapshot(rs.getString("author_name_snapshot"));
        cc.setAuthorRole(rs.getString("author_role"));
        cc.setCommentText(rs.getString("comment_text"));
        cc.setInternalNote(rs.getBoolean("is_internal_note"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        cc.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        return cc;
    };

    private final RowMapper<ComplaintAttachment> attachmentRowMapper = (rs, rowNum) -> {
        ComplaintAttachment ca = new ComplaintAttachment();
        ca.setId(rs.getLong("id"));
        ca.setComplaintId(rs.getLong("complaint_id"));
        ca.setFileName(rs.getString("file_name"));
        ca.setOriginalFileName(rs.getString("original_file_name"));
        ca.setFileSizeBytes(rs.getLong("file_size_bytes"));
        ca.setMimeType(rs.getString("mime_type"));
        ca.setStoragePath(rs.getString("storage_path"));
        ca.setUploadedByUserId(rs.getLong("uploaded_by_user_id"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        ca.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        return ca;
    };

    private String baseSelectSql() {
        return "SELECT c.*, u.full_name AS resident_name, u.mobile AS resident_mobile, " +
                "f.flat_number, b.name AS block_name, " +
                "a.full_name AS assigned_to_name " +
                "FROM complaints c " +
                "JOIN users u ON c.resident_user_id = u.id " +
                "JOIN flats f ON c.flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "LEFT JOIN users a ON c.assigned_to_user_id = a.id ";
    }

    public List<Complaint> findByResidentUserId(Long residentUserId) {
        String sql = baseSelectSql() + "WHERE c.resident_user_id = ? ORDER BY c.created_at DESC";
        return jdbcTemplate.query(sql, complaintRowMapper, residentUserId);
    }

    public List<Complaint> findAll(String status, String category, int limit, int offset) {
        StringBuilder sb = new StringBuilder(baseSelectSql()).append("WHERE 1=1 ");
        if (status != null && !status.isBlank()) {
            sb.append("AND c.status = '").append(status.replace("'", "''")).append("' ");
        }
        if (category != null && !category.isBlank()) {
            sb.append("AND c.category = '").append(category.replace("'", "''")).append("' ");
        }
        sb.append("ORDER BY c.created_at DESC LIMIT ").append(limit).append(" OFFSET ").append(offset);
        return jdbcTemplate.query(sb.toString(), complaintRowMapper);
    }

    public Optional<Complaint> findById(Long id) {
        String sql = baseSelectSql() + "WHERE c.id = ?";
        try {
            Complaint c = jdbcTemplate.queryForObject(sql, complaintRowMapper, id);
            if (c != null) {
                c.setComments(findCommentsByComplaintId(c.getId()));
                c.setAttachments(findAttachmentsByComplaintId(c.getId()));
            }
            return Optional.ofNullable(c);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Complaint save(Complaint c) {
        if (c.getId() == null) {
            String sql = "INSERT INTO complaints (resident_user_id, flat_id, category, subject, description, location_details, priority, status, assigned_to_user_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, c.getResidentUserId());
                ps.setLong(2, c.getFlatId());
                ps.setString(3, c.getCategory());
                ps.setString(4, c.getSubject().trim());
                ps.setString(5, c.getDescription());
                ps.setString(6, c.getLocationDetails());
                ps.setString(7, c.getPriority() != null ? c.getPriority() : "MEDIUM");
                ps.setString(8, c.getStatus() != null ? c.getStatus() : "OPEN");
                if (c.getAssignedToUserId() != null) ps.setLong(9, c.getAssignedToUserId()); else ps.setNull(9, java.sql.Types.BIGINT);
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                c.setId(id);
            }
        } else {
            String sql = "UPDATE complaints SET category = ?, subject = ?, description = ?, location_details = ?, priority = ?, status = ?, assigned_to_user_id = ?, resolved_at = ?, closed_at = ?, reopened_count = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, c.getCategory(), c.getSubject().trim(), c.getDescription(),
                    c.getLocationDetails(), c.getPriority(), c.getStatus(), c.getAssignedToUserId(),
                    c.getResolvedAt() != null ? Timestamp.from(c.getResolvedAt()) : null,
                    c.getClosedAt() != null ? Timestamp.from(c.getClosedAt()) : null,
                    c.getReopenedCount(), c.getId());
        }
        return c;
    }

    public List<ComplaintComment> findCommentsByComplaintId(Long complaintId) {
        String sql = "SELECT * FROM complaint_comments WHERE complaint_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, commentRowMapper, complaintId);
    }

    public ComplaintComment saveComment(ComplaintComment cc) {
        String sql = "INSERT INTO complaint_comments (complaint_id, author_user_id, author_name_snapshot, author_role, comment_text, is_internal_note, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, cc.getComplaintId());
            ps.setLong(2, cc.getAuthorUserId());
            ps.setString(3, cc.getAuthorNameSnapshot());
            ps.setString(4, cc.getAuthorRole());
            ps.setString(5, cc.getCommentText());
            ps.setBoolean(6, cc.isInternalNote());
            return ps;
        }, keyHolder);
        Long id = KeyHolderUtils.extractId(keyHolder);
        if (id != null) {
            cc.setId(id);
        }
        return cc;
    }

    public List<ComplaintAttachment> findAttachmentsByComplaintId(Long complaintId) {
        String sql = "SELECT * FROM complaint_attachments WHERE complaint_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, attachmentRowMapper, complaintId);
    }

    public Optional<ComplaintAttachment> findAttachmentById(Long attachmentId) {
        String sql = "SELECT * FROM complaint_attachments WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, attachmentRowMapper, attachmentId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public ComplaintAttachment saveAttachment(ComplaintAttachment ca) {
        String sql = "INSERT INTO complaint_attachments (complaint_id, file_name, original_file_name, file_size_bytes, mime_type, storage_path, uploaded_by_user_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, ca.getComplaintId());
            ps.setString(2, ca.getFileName());
            ps.setString(3, ca.getOriginalFileName());
            ps.setLong(4, ca.getFileSizeBytes());
            ps.setString(5, ca.getMimeType());
            ps.setString(6, ca.getStoragePath());
            ps.setLong(7, ca.getUploadedByUserId());
            return ps;
        }, keyHolder);
        Long id = KeyHolderUtils.extractId(keyHolder);
        if (id != null) {
            ca.setId(id);
        }
        return ca;
    }

    public int countOpenComplaints() {
        String sql = "SELECT COUNT(*) FROM complaints WHERE status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS')";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
