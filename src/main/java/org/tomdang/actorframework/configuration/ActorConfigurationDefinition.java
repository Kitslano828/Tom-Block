package org.tomdang.actorframework.configuration;

import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;
import org.tomdang.actorframework.nameplate.ActorNameplateLineRole;

import java.util.List;
import java.util.Objects;

public record ActorConfigurationDefinition(
		String actorID,
		String displayName,
		ActorAudienceScope audienceScope,
		String presentationTypeID,
		String interactionID,
		ActorDamagePolicy damagePolicy,
		ActorCollisionPolicy collisionPolicy,
		List<ActorNameplateLineConfigurationDefinition> nameplateLines
) {
	public ActorConfigurationDefinition {
		if (actorID == null || actorID.isBlank()) {
			throw new IllegalArgumentException("actorID cannot be null or blank");
		}
		if (displayName == null || displayName.isBlank()) {
			throw new IllegalArgumentException("displayName cannot be null or blank");
		}
		if (audienceScope == null) {
			throw new IllegalArgumentException("audienceScope cannot be null");
		}
		if (presentationTypeID == null || presentationTypeID.isBlank()) {
			throw new IllegalArgumentException("presentationTypeID cannot be null or blank");
		}
		if (interactionID != null && interactionID.isBlank()) {
			throw new IllegalArgumentException("interactionID cannot be blank when present");
		}
		if (damagePolicy == null) {
			throw new IllegalArgumentException("damagePolicy cannot be null");
		}
		if (collisionPolicy == null) {
			throw new IllegalArgumentException("collisionPolicy cannot be null");
		}
		if (nameplateLines == null) {
			throw new IllegalArgumentException("nameplateLines cannot be null");
		}
		if (nameplateLines.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("nameplateLines cannot contain null elements");
		}

		// Validate NAME line invariants
		long nameLineCount = nameplateLines.stream()
				.filter(line -> line.role() == ActorNameplateLineRole.NAME)
				.count();

		if (nameLineCount != 1) {
			throw new IllegalArgumentException("nameplateLines must contain exactly one NAME line (found: " + nameLineCount + ")");
		}

		ActorNameplateLineConfigurationDefinition nameLine = nameplateLines.stream()
				.filter(line -> line.role() == ActorNameplateLineRole.NAME)
				.findFirst()
				.orElseThrow();

		if (!nameLine.visibleWhileMoving()) {
			throw new IllegalArgumentException("The NAME line must have visibleWhileMoving set to true");
		}

		// Defensively copy to enforce immutability
		nameplateLines = List.copyOf(nameplateLines);
	}
}
