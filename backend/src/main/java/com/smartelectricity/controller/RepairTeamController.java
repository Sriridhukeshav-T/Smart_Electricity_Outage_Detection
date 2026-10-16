package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.model.RepairTeam;
import com.smartelectricity.repository.RepairTeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class RepairTeamController {
    private final RepairTeamRepository teamRepository;

    public RepairTeamController(RepairTeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RepairTeam>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.ok("Repair teams retrieved.", teamRepository.findAll()));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RepairTeam>>> getAvailableTeams() {
        return ResponseEntity.ok(ApiResponse.ok("Available repair teams retrieved.", teamRepository.findAvailable()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RepairTeam>> getTeamById(@PathVariable String id) {
        return teamRepository.findById(id)
                .map(team -> ResponseEntity.ok(ApiResponse.ok("Team found.", team)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Repair team not found: " + id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RepairTeam>> createTeam(@RequestBody RepairTeam team) {
        if (team.getTeamId() == null || team.getTeamId().isBlank()) {
            team.setTeamId("TEAM-" + (System.currentTimeMillis() % 1000));
        }
        if (team.getStatus() == null || team.getStatus().isBlank()) {
            team.setStatus("AVAILABLE");
        }
        teamRepository.save(team);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Repair team created successfully.", team));
    }
}
