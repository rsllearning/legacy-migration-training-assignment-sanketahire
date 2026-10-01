package com.social.strangler.model;

public enum TargetEngine {
    MODERN_V2("Modern-V2-Service", "Strangled"),
    LEGACY_V1("Legacy-V1-DAO", "Legacy-Unmigrated");

    private final String engineName;
    private final String status;

    TargetEngine(String engineName, String status) {
        this.engineName = engineName;
        this.status = status;
    }

    public String getEngineName() {
        return engineName;
    }

    public String getStatus() {
        return status;
    }

    public static TargetEngine fromString(String value) {
        if (value == null) return LEGACY_V1;
        String val = value.trim().toUpperCase();
        if (val.contains("V2") || val.contains("MODERN")) {
            return MODERN_V2;
        }
        return LEGACY_V1;
    }
}
