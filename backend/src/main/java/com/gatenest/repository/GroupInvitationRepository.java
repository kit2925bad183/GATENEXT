package com.gatenest.repository;

import com.gatenest.model.GroupInvitation;
import com.gatenest.model.GroupInvitationMember;
import com.gatenest.model.VisitStatus;
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
public class GroupInvitationRepository {

    private final JdbcTemplate jdbcTemplate;

    public GroupInvitationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<GroupInvitation> invitationRowMapper = (rs, rowNum) -> {
        GroupInvitation g = new GroupInvitation();
        g.setId(rs.getLong("id"));
        g.setHostUserId(rs.getLong("host_user_id"));
        g.setDestinationFlatId(rs.getLong("destination_flat_id"));
        g.setEventTitle(rs.getString("event_title"));
        g.setGroupReference(rs.getString("group_reference"));
        Timestamp winStart = rs.getTimestamp("arrival_window_start");
        g.setArrivalWindowStart(winStart != null ? winStart.toInstant() : null);
        Timestamp winEnd = rs.getTimestamp("arrival_window_end");
        g.setArrivalWindowEnd(winEnd != null ? winEnd.toInstant() : null);
        g.setMaxGuests(rs.getInt("max_guests"));
        g.setNotes(rs.getString("notes"));
        String statusStr = rs.getString("status");
        g.setStatus(statusStr != null ? VisitStatus.valueOf(statusStr) : VisitStatus.APPROVED);
        Timestamp createdAt = rs.getTimestamp("created_at");
        g.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        g.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            g.setDestinationFlatNumber(rs.getString("flat_number"));
            g.setDestinationBlockName(rs.getString("block_name"));
            g.setHostFullName(rs.getString("host_name"));
        } catch (Exception ignored) {}

        return g;
    };

    private final RowMapper<GroupInvitationMember> memberRowMapper = (rs, rowNum) -> {
        GroupInvitationMember m = new GroupInvitationMember();
        m.setId(rs.getLong("id"));
        m.setGroupInvitationId(rs.getLong("group_invitation_id"));
        m.setGuestName(rs.getString("guest_name"));
        m.setGuestMobile(rs.getString("guest_mobile"));
        m.setVehicleNumber(rs.getString("vehicle_number"));
        m.setPassCredentialHash(rs.getString("pass_credential_hash"));
        m.setStatus(rs.getString("status"));
        Timestamp inTime = rs.getTimestamp("check_in_time");
        m.setCheckInTime(inTime != null ? inTime.toInstant() : null);
        Timestamp outTime = rs.getTimestamp("check_out_time");
        m.setCheckOutTime(outTime != null ? outTime.toInstant() : null);
        long entryGate = rs.getLong("entry_gate_id");
        m.setEntryGateId(rs.wasNull() ? null : entryGate);
        long exitGate = rs.getLong("exit_gate_id");
        m.setExitGateId(rs.wasNull() ? null : exitGate);
        Timestamp createdAt = rs.getTimestamp("created_at");
        m.setCreatedAt(createdAt != null ? createdAt.toInstant() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        m.setUpdatedAt(updatedAt != null ? updatedAt.toInstant() : null);

        try {
            m.setEntryGateName(rs.getString("entry_gate_name"));
            m.setExitGateName(rs.getString("exit_gate_name"));
        } catch (Exception ignored) {}

        return m;
    };

    public List<GroupInvitation> findByHostUserId(Long hostUserId) {
        String sql = "SELECT g.*, f.flat_number, b.name AS block_name, u.full_name AS host_name " +
                "FROM group_invitations g " +
                "JOIN flats f ON g.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users u ON g.host_user_id = u.id " +
                "WHERE g.host_user_id = ? ORDER BY g.created_at DESC";
        List<GroupInvitation> list = jdbcTemplate.query(sql, invitationRowMapper, hostUserId);
        for (GroupInvitation inv : list) {
            inv.setMembers(findMembersByInvitationId(inv.getId()));
            inv.setInsideGuestsCount(countInsideGuests(inv.getId()));
            inv.setCheckedOutGuestsCount(countCheckedOutGuests(inv.getId()));
        }
        return list;
    }

    public Optional<GroupInvitation> findById(Long id) {
        String sql = "SELECT g.*, f.flat_number, b.name AS block_name, u.full_name AS host_name " +
                "FROM group_invitations g " +
                "JOIN flats f ON g.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users u ON g.host_user_id = u.id " +
                "WHERE g.id = ?";
        try {
            GroupInvitation inv = jdbcTemplate.queryForObject(sql, invitationRowMapper, id);
            if (inv != null) {
                inv.setMembers(findMembersByInvitationId(inv.getId()));
                inv.setInsideGuestsCount(countInsideGuests(inv.getId()));
                inv.setCheckedOutGuestsCount(countCheckedOutGuests(inv.getId()));
            }
            return Optional.ofNullable(inv);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<GroupInvitation> findByGroupReference(String ref) {
        String sql = "SELECT g.*, f.flat_number, b.name AS block_name, u.full_name AS host_name " +
                "FROM group_invitations g " +
                "JOIN flats f ON g.destination_flat_id = f.id " +
                "JOIN blocks b ON f.block_id = b.id " +
                "JOIN users u ON g.host_user_id = u.id " +
                "WHERE g.group_reference = ?";
        try {
            GroupInvitation inv = jdbcTemplate.queryForObject(sql, invitationRowMapper, ref.trim().toUpperCase());
            if (inv != null) {
                inv.setMembers(findMembersByInvitationId(inv.getId()));
                inv.setInsideGuestsCount(countInsideGuests(inv.getId()));
                inv.setCheckedOutGuestsCount(countCheckedOutGuests(inv.getId()));
            }
            return Optional.ofNullable(inv);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<GroupInvitationMember> findMembersByInvitationId(Long invitationId) {
        String sql = "SELECT m.*, g1.name AS entry_gate_name, g2.name AS exit_gate_name " +
                "FROM group_invitation_members m " +
                "LEFT JOIN gates g1 ON m.entry_gate_id = g1.id " +
                "LEFT JOIN gates g2 ON m.exit_gate_id = g2.id " +
                "WHERE m.group_invitation_id = ? ORDER BY m.id ASC";
        return jdbcTemplate.query(sql, memberRowMapper, invitationId);
    }

    public Optional<GroupInvitationMember> findMemberById(Long memberId) {
        String sql = "SELECT m.*, g1.name AS entry_gate_name, g2.name AS exit_gate_name " +
                "FROM group_invitation_members m " +
                "LEFT JOIN gates g1 ON m.entry_gate_id = g1.id " +
                "LEFT JOIN gates g2 ON m.exit_gate_id = g2.id " +
                "WHERE m.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, memberRowMapper, memberId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public GroupInvitation save(GroupInvitation inv) {
        if (inv.getId() == null) {
            String sql = "INSERT INTO group_invitations (host_user_id, destination_flat_id, event_title, group_reference, arrival_window_start, arrival_window_end, max_guests, notes, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, inv.getHostUserId());
                ps.setLong(2, inv.getDestinationFlatId());
                ps.setString(3, inv.getEventTitle().trim());
                ps.setString(4, inv.getGroupReference().trim().toUpperCase());
                ps.setTimestamp(5, Timestamp.from(inv.getArrivalWindowStart()));
                ps.setTimestamp(6, Timestamp.from(inv.getArrivalWindowEnd()));
                ps.setInt(7, inv.getMaxGuests());
                ps.setString(8, inv.getNotes());
                ps.setString(9, inv.getStatus() != null ? inv.getStatus().name() : VisitStatus.APPROVED.name());
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                inv.setId(id);
            }
        } else {
            String sql = "UPDATE group_invitations SET event_title = ?, arrival_window_start = ?, arrival_window_end = ?, max_guests = ?, notes = ?, status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, inv.getEventTitle().trim(), Timestamp.from(inv.getArrivalWindowStart()),
                    Timestamp.from(inv.getArrivalWindowEnd()), inv.getMaxGuests(), inv.getNotes(),
                    inv.getStatus() != null ? inv.getStatus().name() : VisitStatus.APPROVED.name(), inv.getId());
        }
        return inv;
    }

    public GroupInvitationMember saveMember(GroupInvitationMember m) {
        if (m.getId() == null) {
            String sql = "INSERT INTO group_invitation_members (group_invitation_id, guest_name, guest_mobile, vehicle_number, pass_credential_hash, status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(conn -> {
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, m.getGroupInvitationId());
                ps.setString(2, m.getGuestName().trim());
                ps.setString(3, m.getGuestMobile() != null ? m.getGuestMobile().trim() : null);
                ps.setString(4, m.getVehicleNumber() != null ? m.getVehicleNumber().trim().toUpperCase() : null);
                ps.setString(5, m.getPassCredentialHash());
                ps.setString(6, m.getStatus() != null ? m.getStatus() : "EXPECTED");
                return ps;
            }, keyHolder);
            Long id = KeyHolderUtils.extractId(keyHolder);
            if (id != null) {
                m.setId(id);
            }
        } else {
            String sql = "UPDATE group_invitation_members SET guest_name = ?, guest_mobile = ?, vehicle_number = ?, pass_credential_hash = ?, status = ?, check_in_time = ?, check_out_time = ?, entry_gate_id = ?, exit_gate_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            jdbcTemplate.update(sql, m.getGuestName().trim(), m.getGuestMobile(), m.getVehicleNumber(),
                    m.getPassCredentialHash(), m.getStatus(),
                    m.getCheckInTime() != null ? Timestamp.from(m.getCheckInTime()) : null,
                    m.getCheckOutTime() != null ? Timestamp.from(m.getCheckOutTime()) : null,
                    m.getEntryGateId(), m.getExitGateId(), m.getId());
        }
        return m;
    }

    public int countInsideGuests(Long invitationId) {
        String sql = "SELECT COUNT(*) FROM group_invitation_members WHERE group_invitation_id = ? AND status = 'INSIDE'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, invitationId);
        return count != null ? count : 0;
    }

    public int countCheckedOutGuests(Long invitationId) {
        String sql = "SELECT COUNT(*) FROM group_invitation_members WHERE group_invitation_id = ? AND status = 'CHECKED_OUT'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, invitationId);
        return count != null ? count : 0;
    }
}
