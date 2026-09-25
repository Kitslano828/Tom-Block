package org.tomdang.entityai.navigation;

/** Last known navigation request and outcome for inspection tooling. */
public record NavigationDiagnostics(NavigationStatus status, NavigationRequest request, long updatedTick, boolean active) {
    public NavigationDiagnostics {
        if (status == null || request == null || updatedTick < 0) {
            throw new IllegalArgumentException("Navigation diagnostics are invalid");
        }
    }

    public NavigationDiagnostics stopped() { return new NavigationDiagnostics(status, request, updatedTick, false); }
}
