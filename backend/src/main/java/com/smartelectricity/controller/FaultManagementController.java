package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.dto.CreateFaultRequest;
import com.smartelectricity.model.FaultReport;
import com.smartelectricity.service.FaultManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faults")
public class FaultManagementController {
    private final FaultManagementService faultManagementService;

    public FaultManagementController(FaultManagementService faultManagementService) {
        this.faultManagementService = faultManagementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FaultReport>>> getAllFaults() {
        List<FaultReport> list = faultManagementService.getAllFaults();
        return ResponseEntity.ok(ApiResponse.ok("Fault reports retrieved.", list));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<FaultReport>>> getActiveFaults() {
        List<FaultReport> list = faultManagementService.getActiveFaults();
        return ResponseEntity.ok(ApiResponse.ok("Active fault reports retrieved.", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FaultReport>> getFaultById(@PathVariable String id) {
        return faultManagementService.getFaultById(id)
                .map(fault -> ResponseEntity.ok(ApiResponse.ok("Fault found.", fault)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Fault report not found: " + id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FaultReport>> reportFault(@Valid @RequestBody CreateFaultRequest req) {
        FaultReport report = faultManagementService.reportFault(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Fault report filed and line marked as FAILED.", report));
    }
}
