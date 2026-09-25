package org.tomdang.entityai.core;

public record AiGoalDiagnostics(String id, int priority, boolean active) {
    public AiGoalDiagnostics {
        if (id == null || id.isBlank() || priority < 0) throw new IllegalArgumentException("Invalid goal diagnostics");
    }
}
