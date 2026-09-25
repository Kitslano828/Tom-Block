package org.tomdang.entityai.navigation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.tomdang.entityai.core.AiAgent;
import org.tomdang.entityai.core.AiCapability;
import org.tomdang.entityai.core.AiVector;

/** Local steering navigator for flying/display agents. It never loads chunks. */
public final class DirectFlightNavigator implements Navigator {
    private static final int STUCK_TICKS = 30;
    private final Map<UUID, Progress> progress = new HashMap<>();
    private final Map<UUID, NavigationDiagnostics> diagnostics = new HashMap<>();

    @Override public NavigationStatus navigate(AiAgent raw, NavigationRequest request, long tick) {
        if (!(raw instanceof MovableAiAgent agent) || !agent.valid()) return record(raw, request, tick, NavigationStatus.INVALID);
        if (!agent.has(AiCapability.FLYING) && !agent.has(AiCapability.HOVERING)) {
            return record(agent, request, tick, NavigationStatus.INVALID);
        }
        AiVector current = agent.position();
        if (current.distanceSquared(request.destination()) <= request.arrivalRadius() * request.arrivalRadius()) {
            progress.remove(agent.id());
            return record(agent, request, tick, NavigationStatus.ARRIVED);
        }
        if (request.maximumHomeDistance() > 0 && request.destination().distanceSquared(agent.home())
                > request.maximumHomeDistance() * request.maximumHomeDistance()) {
            return record(agent, request, tick, NavigationStatus.BLOCKED);
        }
        AiVector direction = request.destination().subtract(current).normalized();
        double step = Math.min(request.speed() / 20.0, Math.sqrt(current.distanceSquared(request.destination())));
        AiVector direct = current.add(direction.multiply(step));
        AiVector next = agent.canOccupy(direct) ? direct : detour(agent, current, direction, step);
        if (next == null) return record(agent, request, tick, NavigationStatus.BLOCKED);
        Progress old = progress.get(agent.id());
        if (old != null && current.distanceSquared(old.position) < 0.0004 && tick - old.tick >= STUCK_TICKS) {
            progress.remove(agent.id());
            return record(agent, request, tick, NavigationStatus.STUCK);
        }
        if (old == null || current.distanceSquared(old.position) >= 0.0004) {
            progress.put(agent.id(), new Progress(current, tick));
        }
        agent.move(next, direction);
        return record(agent, request, tick, NavigationStatus.RUNNING);
    }

    private AiVector detour(MovableAiAgent agent, AiVector current, AiVector direction, double step) {
        for (AiVector offset : List.of(new AiVector(0, step, 0), new AiVector(0, -step, 0),
                new AiVector(-direction.z() * step, 0, direction.x() * step),
                new AiVector(direction.z() * step, 0, -direction.x() * step))) {
            AiVector candidate = current.add(offset);
            if (agent.canOccupy(candidate)) return candidate;
        }
        return null;
    }

    private NavigationStatus record(AiAgent agent, NavigationRequest request, long tick, NavigationStatus status) {
        if (agent != null) diagnostics.put(agent.id(), new NavigationDiagnostics(status, request, tick,
                status == NavigationStatus.RUNNING));
        return status;
    }

    @Override public void cancel(AiAgent agent) {
        progress.remove(agent.id());
        diagnostics.computeIfPresent(agent.id(), (ignored, value) -> value.stopped());
    }
    @Override public Optional<NavigationDiagnostics> diagnostics(AiAgent agent) {
        return Optional.ofNullable(diagnostics.get(agent.id()));
    }

    private record Progress(AiVector position, long tick) {}
}
