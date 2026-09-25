package org.tomdang.encounter.definition;
import java.time.Duration;
import java.util.Map;
public record EncounterDefinition(String id, String behavior, EncounterMode mode, Duration timeout,
		Duration disconnectGrace, Map<String, String> parameters) {
	public EncounterDefinition {
		if (id == null || id.isBlank() || behavior == null || behavior.isBlank()) throw new IllegalArgumentException("Encounter id and behavior are required");
		if (mode == null || timeout == null || timeout.isNegative() || timeout.isZero()
				|| disconnectGrace == null || disconnectGrace.isNegative()) throw new IllegalArgumentException("Invalid encounter timing");
		parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
	}
}
