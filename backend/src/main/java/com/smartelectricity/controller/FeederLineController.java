package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.dto.CreateFeederRequest;
import com.smartelectricity.dto.UpdateFeederRequest;
import com.smartelectricity.model.FeederLine;
import com.smartelectricity.service.NetworkManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feeders")
public class FeederLineController {
    private final NetworkManagementService networkManagementService;

    public FeederLineController(NetworkManagementService networkManagementService) {
        this.networkManagementService = networkManagementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FeederLine>>> getAllFeeders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isBackup) {

        List<FeederLine> feeders = networkManagementService.getAllFeeders();

        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            feeders = feeders.stream()
                    .filter(f -> f.getFeederId().toLowerCase().contains(s) ||
                            f.getName().toLowerCase().contains(s) ||
                            f.getSourceNodeId().toLowerCase().contains(s) ||
                            f.getTargetNodeId().toLowerCase().contains(s))
                    .toList();
        }
        if (status != null && !status.isBlank()) {
            feeders = feeders.stream()
                    .filter(f -> f.getStatus().equalsIgnoreCase(status))
                    .toList();
        }
        if (isBackup != null) {
            feeders = feeders.stream()
                    .filter(f -> f.isBackup() == isBackup)
                    .toList();
        }

        return ResponseEntity.ok(ApiResponse.ok("Feeder lines retrieved successfully.", feeders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeederLine>> getFeederById(@PathVariable String id) {
        return networkManagementService.getFeederById(id)
                .map(feeder -> ResponseEntity.ok(ApiResponse.ok("Feeder line found.", feeder)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Feeder line not found: " + id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeederLine>> createFeeder(@Valid @RequestBody CreateFeederRequest req) {
        FeederLine created = networkManagementService.createFeeder(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Feeder line created successfully.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeederLine>> updateFeeder(@PathVariable String id,
                                                                @Valid @RequestBody UpdateFeederRequest req) {
        FeederLine updated = networkManagementService.updateFeeder(id, req);
        return ResponseEntity.ok(ApiResponse.ok("Feeder line updated successfully.", updated));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateFeederStatus(@PathVariable String id,
                                                                @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || (!status.equalsIgnoreCase("WORKING") && !status.equalsIgnoreCase("FAILED"))) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Status must be WORKING or FAILED"));
        }
        networkManagementService.setFeederStatus(id, status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Feeder status updated to " + status.toUpperCase(), null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeeder(@PathVariable String id) {
        networkManagementService.deleteFeeder(id);
        return ResponseEntity.ok(ApiResponse.ok("Feeder deleted successfully: " + id, null));
    }
}
