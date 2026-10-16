package com.smartelectricity.repository;

import com.smartelectricity.model.ApprovedBackupConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ApprovedBackupRepository {
    private final JdbcTemplate jdbcTemplate;

    public ApprovedBackupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ApprovedBackupConnection> rowMapper = new RowMapper<>() {
        @Override
        public ApprovedBackupConnection mapRow(ResultSet rs, int rowNum) throws SQLException {
            ApprovedBackupConnection backup = new ApprovedBackupConnection();
            backup.setBackupId(rs.getString("backup_id"));
            backup.setName(rs.getString("name"));
            backup.setPrimaryFeederId(rs.getString("primary_feeder_id"));
            backup.setAlternateFeederId(rs.getString("alternate_feeder_id"));
            backup.setDescription(rs.getString("description"));
            backup.setApproved(rs.getBoolean("is_approved"));
            if (rs.getTimestamp("created_at") != null) {
                backup.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return backup;
        }
    };

    public List<ApprovedBackupConnection> findAll() {
        String sql = "SELECT * FROM approved_backup_connections ORDER BY backup_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<ApprovedBackupConnection> findById(String backupId) {
        String sql = "SELECT * FROM approved_backup_connections WHERE backup_id = ?";
        List<ApprovedBackupConnection> list = jdbcTemplate.query(sql, rowMapper, backupId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsById(String backupId) {
        String sql = "SELECT COUNT(*) FROM approved_backup_connections WHERE backup_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, backupId);
        return count != null && count > 0;
    }

    public int countByFeederId(String feederId) {
        String sql = "SELECT COUNT(*) FROM approved_backup_connections WHERE primary_feeder_id = ? OR alternate_feeder_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, feederId, feederId);
        return count != null ? count : 0;
    }

    public int save(ApprovedBackupConnection backup) {
        String sql = "INSERT INTO approved_backup_connections (backup_id, name, primary_feeder_id, alternate_feeder_id, description, is_approved) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                backup.getBackupId(),
                backup.getName(),
                backup.getPrimaryFeederId(),
                backup.getAlternateFeederId(),
                backup.getDescription(),
                backup.isApproved()
        );
    }

    public int deleteById(String backupId) {
        String sql = "DELETE FROM approved_backup_connections WHERE backup_id = ?";
        return jdbcTemplate.update(sql, backupId);
    }
}
