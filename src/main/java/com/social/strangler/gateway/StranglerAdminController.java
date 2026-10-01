package com.social.strangler.gateway;

import com.social.strangler.model.TargetEngine;
import com.social.strangler.service.StranglerRouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Control plane for the Strangler Fig API Gateway.
 * Allows DevOps operators to migrate individual routes, check status, or trigger emergency rollbacks.
 */
@RestController
@RequestMapping("/gateway/admin")
public class StranglerAdminController {

    private final StranglerRouteService routeService;

    public StranglerAdminController(StranglerRouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getRoutes() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("gateway_status", "ACTIVE");
        
        Map<String, Object> routeDetails = new LinkedHashMap<>();
        routeService.getAllRoutes().forEach((path, target) -> {
            Map<String, String> meta = new LinkedHashMap<>();
            meta.put("target_engine", target.getEngineName());
            meta.put("migration_status", target.getStatus());
            routeDetails.put(path, meta);
        });
        response.put("routes", routeDetails);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/route")
    public ResponseEntity<Map<String, Object>> updateRoute(
            @RequestParam("path") String path,
            @RequestParam("target") String targetStr) {
        TargetEngine target = TargetEngine.fromString(targetStr);
        routeService.setRoute(path, target);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "SUCCESS");
        response.put("updated_path", path);
        response.put("new_target", target.getEngineName());
        response.put("migration_status", target.getStatus());
        response.put("message", "Route updated in Strangler Gateway with ZERO downtime.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rollback")
    public ResponseEntity<Map<String, Object>> emergencyRollback(
            @RequestParam(value = "path", required = false) String path) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ROLLBACK_EXECUTED");

        if (path != null && !path.isBlank()) {
            routeService.rollbackRoute(path);
            response.put("target_path", path);
            response.put("active_engine", TargetEngine.LEGACY_V1.getEngineName());
            response.put("message", "Emergency rollback executed for " + path + " -> Reverted to Legacy V1.");
        } else {
            routeService.rollbackAll();
            response.put("target_path", "ALL_ROUTES");
            response.put("active_engine", TargetEngine.LEGACY_V1.getEngineName());
            response.put("message", "GLOBAL EMERGENCY ROLLBACK EXECUTED. All routes reverted to Legacy V1 with ZERO downtime.");
        }
        return ResponseEntity.ok(response);
    }
}
