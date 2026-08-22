package org.tomdang.actorframework.definition;

import lombok.Getter;
import org.tomdang.actorframework.audience.ActorAudienceScope;
import org.tomdang.actorframework.collision.ActorCollisionPolicy;
import org.tomdang.actorframework.combat.ActorDamagePolicy;

public class ActorDefinition {

	@Getter
	private final String actorID;
	@Getter
	private final String displayName;
	@Getter
	private final ActorAudienceScope audienceScope;
	@Getter
	private final String presentationTypeID;
	@Getter
	private final String interactionID;
	@Getter
	private final ActorDamagePolicy damagePolicy;
	@Getter
	private final ActorCollisionPolicy actorCollisionPolicy;


	public ActorDefinition(String actorID, String displayName, ActorAudienceScope actorAudienceScope,
						   String presentationTypeID, String interactionID, ActorDamagePolicy damagePolicy,
						   ActorCollisionPolicy actorCollisionPolicy
	) {
		if (actorID == null) throw new IllegalArgumentException("actor id cannot be null");
		if (displayName == null) throw new IllegalArgumentException("Name cannot be null");
		if (actorID.isBlank()) throw new IllegalArgumentException("Actor ID cannot be blank!");
		if (displayName.isBlank()) throw new IllegalArgumentException("Display name cannot be blank");
		if (actorAudienceScope == null) throw new IllegalArgumentException("Scope cannot be null");
		if (presentationTypeID == null) throw new IllegalArgumentException("Presentation Type ID cannot be null");
		if (presentationTypeID.isBlank()) throw new IllegalArgumentException("Presentation Type ID cannot be blank");
		if (interactionID != null && interactionID.isBlank()) {
			throw new IllegalArgumentException("Interaction ID cannot be blank");
		}
		if (damagePolicy == null) throw new IllegalArgumentException("Damage policy cannot be null");
		if (actorCollisionPolicy == null) throw new IllegalArgumentException("Actor Collision Policy cannot be null");

		this.actorID = actorID;
		this.displayName = displayName;
		this.audienceScope = actorAudienceScope;
		this.presentationTypeID = presentationTypeID;
		this.interactionID = interactionID;
		this.damagePolicy = damagePolicy;
		this.actorCollisionPolicy = actorCollisionPolicy;
	}

	public boolean hasInteraction() {
		return this.interactionID != null && !this.interactionID.isBlank();
	}

}
