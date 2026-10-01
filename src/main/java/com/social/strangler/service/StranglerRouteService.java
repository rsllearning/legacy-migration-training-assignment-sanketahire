package com.social.strangler.service;

import com.social.strangler.model.TargetEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the dynamic routing table for the Strangler Fig API Gateway.
 * Controls URL-by-URL migration state between Legacy V1 and Modern V2 services.
 */
@Service
public class StranglerRouteService {

    private static final Logger logger = LoggerFactory.getLogger(StranglerRouteService.class);

    // Dynamic Route Table: Path -> TargetEngine
    private final Map<String, TargetEngine> routeTable = new ConcurrentHashMap<>();

    public StranglerRouteService() {
        // Initial Strangler State (Incremental Migration in Progress):
        // /api/profile -> Strangled (Migrated to Modern V2)
        // /api/settings -> Strangled (Migrated to Modern V2)
        // /api/feed -> Legacy (Unmigrated, running on V1 DAO)
        routeTable.put("/api/profile", TargetEngine.MODERN_V2);
        routeTable.put("/api/settings", TargetEngine.MODERN_V2);
        routeTable.put("/api/feed", TargetEngine.LEGACY_V1);
        logger.info("[STRANGLER-GATEWAY] Initialized route table: {}", routeTable);
    }

    public TargetEngine getTarget(String path) {
        return routeTable.getOrDefault(path, TargetEngine.LEGACY_V1);
    }

    public void setRoute(String path, TargetEngine target) {
        TargetEngine old = routeTable.put(path, target);
        logger.warn("[STRANGLER-GATEWAY] Route updated for '{}': {} -> {}", path, old, target);
    }

    public void rollbackRoute(String path) {
        setRoute(path, TargetEngine.LEGACY_V1);
    }

    public void rollbackAll() {
        for (String path : routeTable.keySet()) {
            routeTable.put(path, TargetEngine.LEGACY_V1);
        }
        logger.warn("[STRANGLER-GATEWAY] EMERGENCY ROLLBACK: All routes redirected to LEGACY_V1.");
    }

    public void resetToDefault() {
        routeTable.put("/api/profile", TargetEngine.MODERN_V2);
        routeTable.put("/api/settings", TargetEngine.MODERN_V2);
        routeTable.put("/api/feed", TargetEngine.LEGACY_V1);
    }

    public Map<String, TargetEngine> getAllRoutes() {
        return Collections.unmodifiableMap(routeTable);
    }
}
