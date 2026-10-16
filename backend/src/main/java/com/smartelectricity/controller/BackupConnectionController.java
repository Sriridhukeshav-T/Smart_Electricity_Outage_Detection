package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.dto.BackupPathStatusDto;
import com.smartelectricity.dto.CreateBackupRequest;
import com.smartelectricity.model.ApprovedBackupConnection;
import com.smartelectricity.service.BackupConnectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backups")
public class BackupConnectionController {
    private final BackupConnectionService backupConnectionService;

    public BackupConnectionController(BackupConnectionService backupConnectionService) {
        this.backupConnectionService = backupConnectionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApprovedBackupConnection>>> getAllBackups() {
        List<ApprovedBackupConnection> list = backupConnectionService.getAllBackups();
        return ResponseEntity.ok(ApiResponse.ok("Approved backup connections retrieved.", list));
    }

    @GetMapping("/routes")
    public ResponseEntity<ApiResponse<List<BackupPathStatusDto>>> getBackupRouteStatuses() {
        List<BackupPathStatusDto> routes = backupConnectionService.evaluateAllBackupRoutes();
        return ResponseEntity.ok(ApiResponse.ok("Evaluated approved backup routes against current network state.", routes));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovedBackupConnection>> createBackup(@Valid @RequestBody CreateBackupRequest req) {
        ApprovedBackupConnection created = backupConnectionService.createBackup(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Approved backup connection registered successfully.", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBackup(@PathVariable String id) {
        backupConnectionService.deleteBackup(id);
        return ResponseEntity.ok(ApiResponse.ok("Backup connection deleted successfully: " + id, null));
    }
}
