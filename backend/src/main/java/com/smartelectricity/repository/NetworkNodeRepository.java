package com.smartelectricity.repository;

import com.smartelectricity.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class NetworkNodeRepository {
    private final JdbcTemplate jdbcTemplate;

    public NetworkNodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<NetworkNode> rowMapper = new RowMapper<>() {
        @Override
        public NetworkNode mapRow(ResultSet rs, int rowNum) throws SQLException {
            String nodeId = rs.getString("node_id");
            String name = rs.getString("name");
            String nodeType = rs.getString("node_type");
            String status = rs.getString("status");
            boolean isSource = rs.getBoolean("is_source");
            double capacityKva = rs.getDouble("capacity_kva");
            String consumerType = rs.getString("consumer_type");
            String criticalFacilityName = rs.getString("critical_facility_name");
            int consumerCount = rs.getInt("consumer_count");
            double locationX = rs.getDouble("location_x");
            double locationY = rs.getDouble("location_y");

            if ("SUBSTATION".equalsIgnoreCase(nodeType)) {
                return new Substation(nodeId, name, status, locationX, locationY, isSource, capacityKva);
            } else {
                if ("CRITICAL".equalsIgnoreCase(consumerType)) {
                    return new CriticalArea(nodeId, name, status, locationX, locationY, consumerCount, criticalFacilityName);
                } else {
                    return new GeneralArea(nodeId, name, status, locationX, locationY, consumerCount);
                }
            }
        }
    };

    public List<NetworkNode> findAll() {
        String sql = "SELECT * FROM network_nodes ORDER BY node_type DESC, node_id ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<NetworkNode> findById(String nodeId) {
        String sql = "SELECT * FROM network_nodes WHERE node_id = ?";
        List<NetworkNode> list = jdbcTemplate.query(sql, rowMapper, nodeId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public boolean existsById(String nodeId) {
        String sql = "SELECT COUNT(*) FROM network_nodes WHERE node_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nodeId);
        return count != null && count > 0;
    }

    public int save(NetworkNode node) {
        String sql = "INSERT INTO network_nodes (node_id, name, node_type, status, is_source, capacity_kva, consumer_type, critical_facility_name, consumer_count, location_x, location_y) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        boolean isSubstation = node instanceof Substation;
        boolean isSource = isSubstation && ((Substation) node).isSource();
        double capacityKva = isSubstation ? ((Substation) node).getCapacityKva() : 0.0;
        String consumerType = null;
        String criticalFacility = null;
        int consumerCount = 0;

        if (node instanceof ConsumerArea ca) {
            consumerType = ca.getConsumerType();
            criticalFacility = ca.getCriticalFacilityName();
            consumerCount = ca.getConsumerCount();
        }

        return jdbcTemplate.update(sql,
                node.getNodeId(),
                node.getName(),
                node.getNodeType(),
                node.getStatus(),
                isSource,
                capacityKva,
                consumerType,
                criticalFacility,
                consumerCount,
                node.getLocationX(),
                node.getLocationY()
        );
    }

    public int update(NetworkNode node) {
        String sql = "UPDATE network_nodes SET name = ?, status = ?, is_source = ?, capacity_kva = ?, " +
                "consumer_type = ?, critical_facility_name = ?, consumer_count = ?, location_x = ?, location_y = ? " +
                "WHERE node_id = ?";

        boolean isSubstation = node instanceof Substation;
        boolean isSource = isSubstation && ((Substation) node).isSource();
        double capacityKva = isSubstation ? ((Substation) node).getCapacityKva() : 0.0;
        String consumerType = null;
        String criticalFacility = null;
        int consumerCount = 0;

        if (node instanceof ConsumerArea ca) {
            consumerType = ca.getConsumerType();
            criticalFacility = ca.getCriticalFacilityName();
            consumerCount = ca.getConsumerCount();
        }

        return jdbcTemplate.update(sql,
                node.getName(),
                node.getStatus(),
                isSource,
                capacityKva,
                consumerType,
                criticalFacility,
                consumerCount,
                node.getLocationX(),
                node.getLocationY(),
                node.getNodeId()
        );
    }

    public int updateStatus(String nodeId, String status) {
        String sql = "UPDATE network_nodes SET status = ? WHERE node_id = ?";
        return jdbcTemplate.update(sql, status, nodeId);
    }

    public int deleteById(String nodeId) {
        String sql = "DELETE FROM network_nodes WHERE node_id = ?";
        return jdbcTemplate.update(sql, nodeId);
    }
}
