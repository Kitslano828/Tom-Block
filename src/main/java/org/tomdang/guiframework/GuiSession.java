package org.tomdang.guiframework;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;

public final class GuiSession {
    private final UUID playerId;
    private final UUID token = UUID.randomUUID();
    private final Deque<GuiRoute> history = new ArrayDeque<>();
    private GuiRoute current;
    private long revision;

    public GuiSession(UUID playerId, GuiRoute root) {
        if (playerId == null || root == null) throw new IllegalArgumentException("GUI session requires player and root route");
        this.playerId = playerId;
        this.current = root;
    }
    public void navigate(GuiRoute route) {
        if (route == null) throw new IllegalArgumentException("GUI route is required");
        history.push(current);
        current = route;
    }
    public boolean back() {
        if (history.isEmpty()) return false;
        current = history.pop();
        return true;
    }
    public void refresh(GuiRoute route) {
        if (route == null) throw new IllegalArgumentException("GUI route is required");
        current = route;
    }
    public long nextRevision() { return ++revision; }
    public UUID playerId() { return playerId; }
    public UUID token() { return token; }
    public GuiRoute current() { return current; }
    public long revision() { return revision; }
    public int historyDepth() { return history.size(); }
    public Optional<GuiRoute> previous() { return Optional.ofNullable(history.peek()); }
}
