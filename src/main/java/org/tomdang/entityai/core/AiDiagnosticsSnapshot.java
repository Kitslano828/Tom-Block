package org.tomdang.entityai.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.tomdang.entityai.navigation.NavigationDiagnostics;

/** Immutable view of one brain; safe for commands and future monitoring tools. */
public record AiDiagnosticsSnapshot(UUID id, String type, String worldId, AiVector position, AiVector home,
        Set<AiCapability> capabilities, List<AiGoalDiagnostics> goals, PerceptionSnapshot perception,
        Map<String, Object> memory, Optional<NavigationDiagnostics> navigation) {
    public AiDiagnosticsSnapshot {
        if (id == null || type == null || type.isBlank() || worldId == null || position == null || home == null
                || capabilities == null || goals == null || perception == null || memory == null || navigation == null) {
            throw new IllegalArgumentException("AI diagnostics snapshot is incomplete");
        }
        capabilities = Set.copyOf(capabilities);
        goals = List.copyOf(goals);
        memory = Map.copyOf(memory);
    }

    public Optional<AiGoalDiagnostics> activeGoal() { return goals.stream().filter(AiGoalDiagnostics::active).findFirst(); }
}
