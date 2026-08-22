package org.tomdang.actorframework.audience;

import java.util.UUID;

public record ActorAudienceKey(ActorAudienceScope scope, UUID audienceID) {

	public ActorAudienceKey {
		if (scope == null) throw new IllegalArgumentException("scope cannot be null!");
		if (scope == ActorAudienceScope.GLOBAL && audienceID != null) throw new IllegalArgumentException("A global scope cannot exist with an existing audienceID");
		if (scope != ActorAudienceScope.GLOBAL && audienceID == null) throw new IllegalArgumentException("Scope is not global but audience ID is null");
	}

	public static ActorAudienceKey global() {
		return new ActorAudienceKey(ActorAudienceScope.GLOBAL, null);
	}

	public static ActorAudienceKey player(UUID playerID) {
		return new ActorAudienceKey(ActorAudienceScope.PLAYER, playerID);
	}

	public static ActorAudienceKey party(UUID partyID) {
		return new ActorAudienceKey(ActorAudienceScope.PARTY, partyID);
	}

	public static ActorAudienceKey encounter(UUID encounterID) {
		return new ActorAudienceKey(ActorAudienceScope.ENCOUNTER, encounterID);
	}

}
