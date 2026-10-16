package com.smartelectricity.service;

import com.smartelectricity.model.DistributionNetwork;
import com.smartelectricity.model.FeederLine;
import com.smartelectricity.model.NetworkNode;
import com.smartelectricity.repository.FeederLineRepository;
import com.smartelectricity.repository.NetworkNodeRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Maintains and synchronizes the in-memory DistributionNetwork graph from database tables.
 */
@Service
public class DistributionNetworkService {
    private static final Logger log = LoggerFactory.getLogger(DistributionNetworkService.class);

    private final NetworkNodeRepository nodeRepository;
    private final FeederLineRepository feederRepository;
    private final DistributionNetwork network = new DistributionNetwork();

    public DistributionNetworkService(NetworkNodeRepository nodeRepository, FeederLineRepository feederRepository) {
        this.nodeRepository = nodeRepository;
        this.feederRepository = feederRepository;
    }

    @PostConstruct
    public synchronized void reloadNetworkFromDatabase() {
        log.info("Loading distribution network topology from database...");
        network.clear();

        List<NetworkNode> nodes = nodeRepository.findAll();
        for (NetworkNode node : nodes) {
            network.addNode(node);
        }

        List<FeederLine> feeders = feederRepository.findAll();
        for (FeederLine feeder : feeders) {
            network.addFeeder(feeder);
        }

        log.info("Loaded {} nodes and {} feeder lines into in-memory network graph.",
                nodes.size(), feeders.size());
    }

    public DistributionNetwork getNetwork() {
        return network;
    }
}
