package com.smartelectricity.repository;

import com.smartelectricity.model.RepairTeam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class RepairTeamRepository {
    private final JdbcTemplate jdbcTemplate;

    public RepairTeamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RepairTeam> rowMapper = new RowMapper<>() {
        @Override
        public RepairTeam mapRow(ResultSet rs, int rowNum) throws SQLException {
            RepairTeam team = new RepairTeam();
            team.setTeamId(rs.getString("team_id"));
            team.setTeamName(rs.getString("team_name"));
            team.setContactNumber(rs.getString("contact_number"));
            team.setStatus(rs.getString("status"));
            team.setCurrentAssignmentId(rs.getString("current_assignment_id"));
            if (rs.getTimestamp("created_at") != null) {
                team.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return team;
        }
    };

    public List<RepairTeam> findAll() {
        String sql = "SELECT * FROM repair_teams ORDER BY team_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<RepairTeam> findById(String teamId) {
        String sql = "SELECT * FROM repair_teams WHERE team_id = ?";
        List<RepairTeam> list = jdbcTemplate.query(sql, rowMapper, teamId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<RepairTeam> findAvailable() {
        String sql = "SELECT * FROM repair_teams WHERE status = 'AVAILABLE' ORDER BY team_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public int updateStatus(String teamId, String status, String currentAssignmentId) {
        String sql = "UPDATE repair_teams SET status = ?, current_assignment_id = ? WHERE team_id = ?";
        return jdbcTemplate.update(sql, status, currentAssignmentId, teamId);
    }

    public int save(RepairTeam team) {
        String sql = "INSERT INTO repair_teams (team_id, team_name, contact_number, status, current_assignment_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                team.getTeamId(),
                team.getTeamName(),
                team.getContactNumber(),
                team.getStatus(),
                team.getCurrentAssignmentId()
        );
    }
}
