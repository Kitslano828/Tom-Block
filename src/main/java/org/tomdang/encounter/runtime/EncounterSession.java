package org.tomdang.encounter.runtime;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
public record EncounterSession(UUID instanceId, String definitionId, UUID ownerId, Set<UUID> participants,
		EncounterState state, Instant startedAt, Instant updatedAt, Instant expiresAt,
		Instant disconnectDeadline, long revision, String failureReason) {
	public EncounterSession {
		if (instanceId == null || ownerId == null || definitionId == null || definitionId.isBlank()) throw new IllegalArgumentException("Encounter identity is required");
		participants = Set.copyOf(participants);
		if (!participants.contains(ownerId)) throw new IllegalArgumentException("Encounter owner must be a participant");
	}
	public boolean terminal() { return state == EncounterState.COMPLETED || state == EncounterState.FAILED; }
}
