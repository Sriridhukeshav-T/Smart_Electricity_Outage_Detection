package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.dto.AssignTeamRequest;
import com.smartelectricity.model.RestorationJob;
import com.smartelectricity.service.RestorationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restoration")
public class RestorationController {
    private final RestorationService restorationService;

    public RestorationController(RestorationService restorationService) {
        this.restorationService = restorationService;
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<List<RestorationJob>>> getRestorationQueue() {
        List<RestorationJob> activeJobs = restorationService.getActiveJobs();
        return ResponseEntity.ok(ApiResponse.ok("Prioritized restoration queue retrieved.", activeJobs));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<RestorationJob>>> getAllJobs() {
        List<RestorationJob> jobs = restorationService.getAllJobs();
        return ResponseEntity.ok(ApiResponse.ok("All restoration jobs retrieved.", jobs));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<ApiResponse<List<RestorationJob>>> recalculatePriorities() {
        List<RestorationJob> jobs = restorationService.recalculatePriorities();
        return ResponseEntity.ok(ApiResponse.ok("Restoration priority ranking recalculated using PriorityQueue.", jobs));
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<RestorationJob>> assignRepairTeam(@Valid @RequestBody AssignTeamRequest req) {
        RestorationJob job = restorationService.assignRepairTeam(req.getIdentifier(), req.getTeamId());
        return ResponseEntity.ok(ApiResponse.ok("Repair team assigned successfully to restoration job.", job));
    }

    @PostMapping("/complete/{identifier}")
    public ResponseEntity<ApiResponse<RestorationJob>> completeRestoration(@PathVariable String identifier) {
        RestorationJob job = restorationService.completeRestoration(identifier);
        return ResponseEntity.ok(ApiResponse.ok("Feeder repaired, grid restored, and repair team released.", job));
    }
}
