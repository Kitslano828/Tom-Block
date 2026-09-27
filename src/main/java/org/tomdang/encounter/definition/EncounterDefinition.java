package org.tomdang.encounter.definition;
import java.time.Duration;
import java.util.Map;
import org.tomdang.activity.ActivityPolicy;
public record EncounterDefinition(String id, String behavior, EncounterMode mode, Duration timeout,
		Duration disconnectGrace, Map<String, String> parameters, ActivityPolicy activityPolicy) {
	public EncounterDefinition(String id, String behavior, EncounterMode mode, Duration timeout,
			Duration disconnectGrace, Map<String, String> parameters) {
		this(id, behavior, mode, timeout, disconnectGrace, parameters, ActivityPolicy.PRIVATE_SOLO);
	}
	public EncounterDefinition {
		if (id == null || id.isBlank() || behavior == null || behavior.isBlank()) throw new IllegalArgumentException("Encounter id and behavior are required");
		if (mode == null || timeout == null || timeout.isNegative() || timeout.isZero()
				|| disconnectGrace == null || disconnectGrace.isNegative()) throw new IllegalArgumentException("Invalid encounter timing");
		parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
		if (activityPolicy == null) throw new IllegalArgumentException("Activity policy is required");
	}
}
