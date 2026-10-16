package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.model.EventHistory;
import com.smartelectricity.repository.EventHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class EventHistoryController {
    private final EventHistoryRepository historyRepository;

    public EventHistoryController(EventHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventHistory>>> getHistory(
            @RequestParam(defaultValue = "50") int limit) {
        List<EventHistory> history = historyRepository.findAll(Math.min(limit, 200));
        return ResponseEntity.ok(ApiResponse.ok("Event and restoration audit history retrieved.", history));
    }
}
