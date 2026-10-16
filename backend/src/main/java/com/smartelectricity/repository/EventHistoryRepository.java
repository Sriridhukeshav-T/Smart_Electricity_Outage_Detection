package com.smartelectricity.repository;

import com.smartelectricity.model.EventHistory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EventHistoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public EventHistoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<EventHistory> rowMapper = new RowMapper<>() {
        @Override
        public EventHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventHistory history = new EventHistory();
            history.setHistoryId(rs.getInt("history_id"));
            history.setEventType(rs.getString("event_type"));
            history.setTargetId(rs.getString("target_id"));
            history.setDetails(rs.getString("details"));
            history.setPerformedBy(rs.getString("performed_by"));
            if (rs.getTimestamp("timestamp") != null) {
                history.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
            }
            return history;
        }
    };

    public List<EventHistory> findAll(int limit) {
        String sql = "SELECT * FROM event_history ORDER BY history_id DESC LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, limit);
    }

    public int save(EventHistory history) {
        String sql = "INSERT INTO event_history (event_type, target_id, details, performed_by) VALUES (?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                history.getEventType(),
                history.getTargetId(),
                history.getDetails(),
                history.getPerformedBy()
        );
    }
}
