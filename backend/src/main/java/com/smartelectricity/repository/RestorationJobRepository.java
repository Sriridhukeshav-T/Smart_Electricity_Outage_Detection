package com.smartelectricity.repository;

import com.smartelectricity.model.RestorationJob;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class RestorationJobRepository {
    private final JdbcTemplate jdbcTemplate;

    public RestorationJobRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<RestorationJob> rowMapper = new RowMapper<>() {
        @Override
        public RestorationJob mapRow(ResultSet rs, int rowNum) throws SQLException {
            RestorationJob job = new RestorationJob();
            job.setJobId(rs.getString("job_id"));
            job.setFaultId(rs.getString("fault_id"));
            job.setFeederId(rs.getString("feeder_id"));
            job.setPriorityScore(rs.getInt("priority_score"));
            job.setPriorityRank(rs.getInt("priority_rank"));
            job.setCriticalFacilitiesCount(rs.getInt("critical_facilities_count"));
            job.setAffectedConsumersCount(rs.getInt("affected_consumers_count"));
            job.setStatus(rs.getString("status"));
            job.setAssignedTeamId(rs.getString("assigned_team_id"));
            if (rs.getTimestamp("created_at") != null) {
                job.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                job.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            return job;
        }
    };

    public List<RestorationJob> findAll() {
        String sql = "SELECT * FROM restoration_jobs ORDER BY priority_rank ASC, created_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public List<RestorationJob> findActiveJobs() {
        String sql = "SELECT * FROM restoration_jobs WHERE status != 'RESTORED' ORDER BY priority_rank ASC, created_at ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<RestorationJob> findById(String jobId) {
        String sql = "SELECT * FROM restoration_jobs WHERE job_id = ?";
        List<RestorationJob> list = jdbcTemplate.query(sql, rowMapper, jobId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<RestorationJob> findByFaultId(String faultId) {
        String sql = "SELECT * FROM restoration_jobs WHERE fault_id = ?";
        List<RestorationJob> list = jdbcTemplate.query(sql, rowMapper, faultId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public int save(RestorationJob job) {
        String sql = "INSERT INTO restoration_jobs (job_id, fault_id, feeder_id, priority_score, priority_rank, critical_facilities_count, affected_consumers_count, status, assigned_team_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                job.getJobId(),
                job.getFaultId(),
                job.getFeederId(),
                job.getPriorityScore(),
                job.getPriorityRank(),
                job.getCriticalFacilitiesCount(),
                job.getAffectedConsumersCount(),
                job.getStatus(),
                job.getAssignedTeamId()
        );
    }

    public int update(RestorationJob job) {
        String sql = "UPDATE restoration_jobs SET priority_score = ?, priority_rank = ?, critical_facilities_count = ?, " +
                "affected_consumers_count = ?, status = ?, assigned_team_id = ? WHERE job_id = ?";
        return jdbcTemplate.update(sql,
                job.getPriorityScore(),
                job.getPriorityRank(),
                job.getCriticalFacilitiesCount(),
                job.getAffectedConsumersCount(),
                job.getStatus(),
                job.getAssignedTeamId(),
                job.getJobId()
        );
    }

    public int deleteById(String jobId) {
        String sql = "DELETE FROM restoration_jobs WHERE job_id = ?";
        return jdbcTemplate.update(sql, jobId);
    }

    public int deleteByFaultId(String faultId) {
        String sql = "DELETE FROM restoration_jobs WHERE fault_id = ?";
        return jdbcTemplate.update(sql, faultId);
    }
}
