package com.social.rollback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe Feature Flag Service managing runtime routing between
 * Legacy V1 and Modern V2 implementations.
 */
@Service
public class FeatureFlagService {

    private static final Logger logger = LoggerFactory.getLogger(FeatureFlagService.class);

    // Global master switch: true = Modern V2, false = Legacy V1 (Rollback state)
    private final AtomicBoolean v2MasterEnabled = new AtomicBoolean(true);

    // Per-endpoint granular switches (optional overrides)
    private final Map<String, Boolean> endpointFlags = new ConcurrentHashMap<>();

    public FeatureFlagService() {
        endpointFlags.put("profile", true);
        endpointFlags.put("settings", true);
        endpointFlags.put("feed", true);
    }

    public boolean isV2Enabled() {
        return v2MasterEnabled.get();
    }

    public boolean isV2EnabledForEndpoint(String endpoint) {
        if (!v2MasterEnabled.get()) {
            return false;
        }
        return endpointFlags.getOrDefault(endpoint.toLowerCase(), true);
    }

    public void setV2Enabled(boolean enabled) {
        boolean previous = v2MasterEnabled.getAndSet(enabled);
        logger.warn("[ROLLBACK-CONTROL] Master V2 flag updated from {} to {}. Active routing engine: {}",
                previous, enabled, enabled ? "V2-Modern-Service" : "V1-Legacy-DAO");
    }

    public void setEndpointV2Enabled(String endpoint, boolean enabled) {
        endpointFlags.put(endpoint.toLowerCase(), enabled);
        logger.warn("[ROLLBACK-CONTROL] Endpoint '{}' V2 flag updated to {}", endpoint, enabled);
    }

    public Map<String, Object> getStatus() {
        return Map.of(
                "master_v2_enabled", v2MasterEnabled.get(),
                "active_engine", v2MasterEnabled.get() ? "V2-Modern-Service" : "V1-Legacy-DAO",
                "endpoint_overrides", endpointFlags
        );
    }
}
