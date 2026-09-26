package org.tomdang.guiframework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class GuiSessionRegistry {
    private final Map<UUID, GuiSession> sessions = new HashMap<>();
    public GuiSession open(UUID playerId, GuiRoute route) {
        GuiSession session = new GuiSession(playerId, route);
        sessions.put(playerId, session);
        return session;
    }
    public Optional<GuiSession> find(UUID playerId) { return Optional.ofNullable(sessions.get(playerId)); }
    public boolean close(UUID playerId, UUID token) {
        GuiSession current = sessions.get(playerId);
        if (current == null || !current.token().equals(token)) return false;
        sessions.remove(playerId);
        return true;
    }
    public void close(UUID playerId) { sessions.remove(playerId); }
    public void clear() { sessions.clear(); }
    public int size() { return sessions.size(); }
    public List<GuiSession> all() { return List.copyOf(sessions.values()); }
}
