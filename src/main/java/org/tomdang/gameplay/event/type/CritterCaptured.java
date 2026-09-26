package org.tomdang.gameplay.event.type;

import org.tomdang.gameplay.event.GameplayEvent;
import java.util.UUID;
import org.tomdang.critter.hunting.HuntGrade;

public record CritterCaptured(UUID playerId, String critterId, HuntGrade grade) implements GameplayEvent {
	public CritterCaptured(UUID playerId, String critterId) { this(playerId, critterId, HuntGrade.STANDARD); }
	public CritterCaptured {
		if (playerId == null) throw new IllegalArgumentException("playerId cannot be null");
		if (critterId == null || critterId.isBlank()) throw new IllegalArgumentException("critterId cannot be blank");
		if (grade == null) throw new IllegalArgumentException("grade cannot be null");
	}
}
