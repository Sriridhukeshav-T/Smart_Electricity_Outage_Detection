package com.smartelectricity.service;

import com.smartelectricity.dto.CreateFaultRequest;
import com.smartelectricity.model.EventHistory;
import com.smartelectricity.model.FaultReport;
import com.smartelectricity.model.FeederLine;
import com.smartelectricity.repository.EventHistoryRepository;
import com.smartelectricity.repository.FaultReportRepository;
import com.smartelectricity.repository.FeederLineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service managing fault reports and feeder failure lifecycles.
 */
@Service
public class FaultManagementService {
    private static final Logger log = LoggerFactory.getLogger(FaultManagementService.class);

    private final FaultReportRepository faultRepository;
    private final FeederLineRepository feederRepository;
    private final EventHistoryRepository historyRepository;
    private final RestorationService restorationService;
    private final DistributionNetworkService networkService;

    public FaultManagementService(FaultReportRepository faultRepository,
                                  FeederLineRepository feederRepository,
                                  EventHistoryRepository historyRepository,
                                  RestorationService restorationService,
                                  DistributionNetworkService networkService) {
        this.faultRepository = faultRepository;
        this.feederRepository = feederRepository;
        this.historyRepository = historyRepository;
        this.restorationService = restorationService;
        this.networkService = networkService;
    }

    @Transactional
    public synchronized FaultReport reportFault(CreateFaultRequest request) {
        String feederId = request.getFeederId();

        // 1. Validate feeder exists
        Optional<FeederLine> feederOpt = feederRepository.findById(feederId);
        if (feederOpt.isEmpty()) {
            throw new IllegalArgumentException("Feeder line not found: " + feederId);
        }

        // 2. Prevent duplicate active fault reports for the same feeder
        Optional<FaultReport> existingActive = faultRepository.findActiveFaultByFeederId(feederId);
        if (existingActive.isPresent()) {
            throw new IllegalStateException(String.format(
                    "Feeder '%s' already has an active unresolved fault report (%s: %s). Duplicate active faults are not allowed.",
                    feederId, existingActive.get().getFaultId(), existingActive.get().getFaultCause()
            ));
        }

        // 3. Generate unique Fault ID
        String faultId = "FLT-" + (System.currentTimeMillis() % 1000000);

        // 4. Update feeder status to FAILED
        feederRepository.updateStatus(feederId, "FAILED");
        FeederLine feederInMem = networkService.getNetwork().getFeeder(feederId);
        if (feederInMem != null) {
            feederInMem.setStatus("FAILED");
        }

        // 5. Create and save FaultReport
        FaultReport report = new FaultReport(
                faultId,
                feederId,
                request.getFaultCause(),
                request.getSeverity(),
                "REPORTED",
                null,
                request.getNotes()
        );
        report.setReportedAt(LocalDateTime.now());
        faultRepository.save(report);

        // 6. Recalculate restoration priority queue
        restorationService.recalculatePriorities();

        // 7. Audit log event
        historyRepository.save(new EventHistory(
                "FAULT_REPORTED",
                faultId,
                String.format("Fault reported on feeder %s: %s [Severity: %s]",
                        feederId, request.getFaultCause(), request.getSeverity()),
                "Operator"
        ));

        log.info("Fault {} successfully recorded on feeder {}.", faultId, feederId);
        return report;
    }

    public List<FaultReport> getAllFaults() {
        return faultRepository.findAll();
    }

    public List<FaultReport> getActiveFaults() {
        return faultRepository.findActiveFaults();
    }

    public Optional<FaultReport> getFaultById(String faultId) {
        return faultRepository.findById(faultId);
    }
}
