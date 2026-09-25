package org.tomdang.entityai.navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.tomdang.entityai.core.AiAgent;
import org.tomdang.entityai.core.AiCapability;

/** Adapter contract for native mob pathfinders; platform agents execute the request. */
public final class NativeGroundNavigator implements Navigator {
    private final Map<UUID, NavigationDiagnostics> diagnostics = new HashMap<>();

    @Override public NavigationStatus navigate(AiAgent raw, NavigationRequest request, long tick) {
        NavigationStatus status;
        if (!(raw instanceof NativeGroundAgent agent) || !agent.valid() || !agent.has(AiCapability.GROUND)) {
            status = NavigationStatus.INVALID;
        } else if (raw.position().distanceSquared(request.destination())
                <= request.arrivalRadius() * request.arrivalRadius()) {
            status = NavigationStatus.ARRIVED;
        } else {
            status = agent.navigateNative(request) ? NavigationStatus.RUNNING : NavigationStatus.BLOCKED;
        }
        diagnostics.put(raw.id(), new NavigationDiagnostics(status, request, tick, status == NavigationStatus.RUNNING));
        return status;
    }

    @Override public void cancel(AiAgent agent) {
        if (agent instanceof NativeGroundAgent ground) ground.stopNativeNavigation();
        diagnostics.computeIfPresent(agent.id(), (ignored, value) -> value.stopped());
    }
    @Override public Optional<NavigationDiagnostics> diagnostics(AiAgent agent) {
        return Optional.ofNullable(diagnostics.get(agent.id()));
    }

    public interface NativeGroundAgent extends AiAgent {
        boolean navigateNative(NavigationRequest request);
        void stopNativeNavigation();
    }
}
