package com.smartelectricity.controller;

import com.smartelectricity.dto.ApiResponse;
import com.smartelectricity.dto.CreateNodeRequest;
import com.smartelectricity.dto.UpdateNodeRequest;
import com.smartelectricity.model.NetworkNode;
import com.smartelectricity.service.NetworkManagementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/nodes")
public class NetworkNodeController {
    private final NetworkManagementService networkManagementService;

    public NetworkNodeController(NetworkManagementService networkManagementService) {
        this.networkManagementService = networkManagementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NetworkNode>>> getAllNodes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        List<NetworkNode> nodes = networkManagementService.getAllNodes();

        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            nodes = nodes.stream()
                    .filter(n -> n.getNodeId().toLowerCase().contains(s) || n.getName().toLowerCase().contains(s))
                    .toList();
        }
        if (type != null && !type.isBlank()) {
            nodes = nodes.stream()
                    .filter(n -> n.getNodeType().equalsIgnoreCase(type))
                    .toList();
        }
        if (status != null && !status.isBlank()) {
            nodes = nodes.stream()
                    .filter(n -> n.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        return ResponseEntity.ok(ApiResponse.ok("Nodes retrieved successfully.", nodes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NetworkNode>> getNodeById(@PathVariable String id) {
        return networkManagementService.getNodeById(id)
                .map(node -> ResponseEntity.ok(ApiResponse.ok("Node found.", node)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Node not found: " + id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NetworkNode>> createNode(@Valid @RequestBody CreateNodeRequest req) {
        NetworkNode created = networkManagementService.createNode(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Network node created successfully.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NetworkNode>> updateNode(@PathVariable String id,
                                                               @Valid @RequestBody UpdateNodeRequest req) {
        NetworkNode updated = networkManagementService.updateNode(id, req);
        return ResponseEntity.ok(ApiResponse.ok("Network node updated successfully.", updated));
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<ApiResponse<Void>> toggleNodeStatus(@PathVariable String id,
                                                              @RequestBody(required = false) Map<String, String> body) {
        String targetStatus = body != null ? body.get("status") : null;
        networkManagementService.toggleNodeStatus(id, targetStatus);
        return ResponseEntity.ok(ApiResponse.ok("Node status updated successfully.", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNode(@PathVariable String id) {
        networkManagementService.deleteNode(id);
        return ResponseEntity.ok(ApiResponse.ok("Node deleted successfully: " + id, null));
    }
}
