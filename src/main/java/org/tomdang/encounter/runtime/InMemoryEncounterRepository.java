package org.tomdang.encounter.runtime;
import java.util.*;
public final class InMemoryEncounterRepository implements EncounterRepository {
	private final Map<UUID,EncounterSession> sessions = new LinkedHashMap<>();
	@Override public synchronized Collection<EncounterSession> loadOpen() { return sessions.values().stream().filter(value -> !value.terminal()).toList(); }
	@Override public synchronized void save(EncounterSession session) { sessions.put(session.instanceId(), session); }
}
