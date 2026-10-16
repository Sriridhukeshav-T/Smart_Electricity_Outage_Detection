package com.smartelectricity.repository;

import com.smartelectricity.model.FaultReport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FaultReportRepository {
    private final JdbcTemplate jdbcTemplate;

    public FaultReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FaultReport> rowMapper = new RowMapper<>() {
        @Override
        public FaultReport mapRow(ResultSet rs, int rowNum) throws SQLException {
            FaultReport fault = new FaultReport();
            fault.setFaultId(rs.getString("fault_id"));
            fault.setFeederId(rs.getString("feeder_id"));
            if (rs.getTimestamp("reported_at") != null) {
                fault.setReportedAt(rs.getTimestamp("reported_at").toLocalDateTime());
            }
            fault.setFaultCause(rs.getString("fault_cause"));
            fault.setSeverity(rs.getString("severity"));
            fault.setStatus(rs.getString("status"));
            fault.setAssignedTeamId(rs.getString("assigned_team_id"));
            if (rs.getTimestamp("resolved_at") != null) {
                fault.setResolvedAt(rs.getTimestamp("resolved_at").toLocalDateTime());
            }
            fault.setNotes(rs.getString("notes"));
            return fault;
        }
    };

    public List<FaultReport> findAll() {
        String sql = "SELECT * FROM fault_reports ORDER BY reported_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<FaultReport> findById(String faultId) {
        String sql = "SELECT * FROM fault_reports WHERE fault_id = ?";
        List<FaultReport> list = jdbcTemplate.query(sql, rowMapper, faultId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<FaultReport> findActiveFaultByFeederId(String feederId) {
        String sql = "SELECT * FROM fault_reports WHERE feeder_id = ? AND status != 'RESOLVED' ORDER BY reported_at DESC LIMIT 1";
        List<FaultReport> list = jdbcTemplate.query(sql, rowMapper, feederId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<FaultReport> findActiveFaults() {
        String sql = "SELECT * FROM fault_reports WHERE status != 'RESOLVED' ORDER BY reported_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public int save(FaultReport fault) {
        String sql = "INSERT INTO fault_reports (fault_id, feeder_id, reported_at, fault_cause, severity, status, assigned_team_id, resolved_at, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                fault.getFaultId(),
                fault.getFeederId(),
                Timestamp.valueOf(fault.getReportedAt() != null ? fault.getReportedAt() : LocalDateTime.now()),
                fault.getFaultCause(),
                fault.getSeverity(),
                fault.getStatus(),
                fault.getAssignedTeamId(),
                fault.getResolvedAt() != null ? Timestamp.valueOf(fault.getResolvedAt()) : null,
                fault.getNotes()
        );
    }

    public int updateStatus(String faultId, String status, String assignedTeamId, LocalDateTime resolvedAt) {
        String sql = "UPDATE fault_reports SET status = ?, assigned_team_id = ?, resolved_at = ? WHERE fault_id = ?";
        return jdbcTemplate.update(sql,
                status,
                assignedTeamId,
                resolvedAt != null ? Timestamp.valueOf(resolvedAt) : null,
                faultId
        );
    }

    public int deleteById(String faultId) {
        String sql = "DELETE FROM fault_reports WHERE fault_id = ?";
        return jdbcTemplate.update(sql, faultId);
    }
}
