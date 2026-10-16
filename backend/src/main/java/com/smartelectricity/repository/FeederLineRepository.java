package com.smartelectricity.repository;

import com.smartelectricity.model.FeederLine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class FeederLineRepository {
    private final JdbcTemplate jdbcTemplate;

    public FeederLineRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FeederLine> rowMapper = new RowMapper<>() {
        @Override
        public FeederLine mapRow(ResultSet rs, int rowNum) throws SQLException {
            FeederLine feeder = new FeederLine();
            feeder.setFeederId(rs.getString("feeder_id"));
            feeder.setName(rs.getString("name"));
            feeder.setSourceNodeId(rs.getString("source_node_id"));
            feeder.setTargetNodeId(rs.getString("target_node_id"));
            feeder.setStatus(rs.getString("status"));
            feeder.setCapacityAmps(rs.getDouble("capacity_amps"));
            feeder.setLengthKm(rs.getDouble("length_km"));
            feeder.setBackup(rs.getBoolean("is_backup"));
            if (rs.getTimestamp("created_at") != null) {
                feeder.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                feeder.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            return feeder;
        }
    };

    public List<FeederLine> findAll() {
        String sql = "SELECT * FROM feeder_lines ORDER BY feeder_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<FeederLine> findById(String feederId) {
        String sql = "SELECT * FROM feeder_lines WHERE feeder_id = ?";
        List<FeederLine> list = jdbcTemplate.query(sql, rowMapper, feederId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsById(String feederId) {
        String sql = "SELECT COUNT(*) FROM feeder_lines WHERE feeder_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, feederId);
        return count != null && count > 0;
    }

    public List<FeederLine> findByNodeId(String nodeId) {
        String sql = "SELECT * FROM feeder_lines WHERE source_node_id = ? OR target_node_id = ?";
        return jdbcTemplate.query(sql, rowMapper, nodeId, nodeId);
    }

    public int countByNodeId(String nodeId) {
        String sql = "SELECT COUNT(*) FROM feeder_lines WHERE source_node_id = ? OR target_node_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nodeId, nodeId);
        return count != null ? count : 0;
    }

    public int save(FeederLine feeder) {
        String sql = "INSERT INTO feeder_lines (feeder_id, name, source_node_id, target_node_id, status, capacity_amps, length_km, is_backup) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                feeder.getFeederId(),
                feeder.getName(),
                feeder.getSourceNodeId(),
                feeder.getTargetNodeId(),
                feeder.getStatus(),
                feeder.getCapacityAmps(),
                feeder.getLengthKm(),
                feeder.isBackup()
        );
    }

    public int update(FeederLine feeder) {
        String sql = "UPDATE feeder_lines SET name = ?, status = ?, capacity_amps = ?, length_km = ?, is_backup = ? WHERE feeder_id = ?";
        return jdbcTemplate.update(sql,
                feeder.getName(),
                feeder.getStatus(),
                feeder.getCapacityAmps(),
                feeder.getLengthKm(),
                feeder.isBackup(),
                feeder.getFeederId()
        );
    }

    public int updateStatus(String feederId, String status) {
        String sql = "UPDATE feeder_lines SET status = ? WHERE feeder_id = ?";
        return jdbcTemplate.update(sql, status, feederId);
    }

    public int deleteById(String feederId) {
        String sql = "DELETE FROM feeder_lines WHERE feeder_id = ?";
        return jdbcTemplate.update(sql, feederId);
    }
}
