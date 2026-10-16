package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.model.OutageDetectionResult;
import com.smartelectricity.service.OutageDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outages")
public class OutageDetectionController {
    private final OutageDetectionService outageDetectionService;

    public OutageDetectionController(OutageDetectionService outageDetectionService) {
        this.outageDetectionService = outageDetectionService;
    }

    @GetMapping("/detect")
    public ResponseEntity<ApiResponse<OutageDetectionResult>> detectOutages() {
        OutageDetectionResult result = outageDetectionService.detectOutages();
        return ResponseEntity.ok(ApiResponse.ok("Outage detection BFS completed successfully.", result));
    }
}
