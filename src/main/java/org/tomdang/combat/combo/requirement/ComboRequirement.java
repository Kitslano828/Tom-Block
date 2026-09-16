package org.tomdang.combat.combo.requirement;

import org.tomdang.customitemframework.combat.CombatDamageType;
import org.tomdang.customitemframework.combat.CombatWeightClass;

import java.util.Optional;

public record ComboRequirement(
		int requiredHits,
		Optional<CombatWeightClass> weightClass,
		Optional<CombatDamageType> damageType,
		Optional<String> itemId
) {
	public ComboRequirement {
		if (requiredHits < 1) throw new IllegalArgumentException("requiredHits must be at least 1");
		if (weightClass == null) throw new IllegalArgumentException("weightClass cannot be null");
		if (damageType == null) throw new IllegalArgumentException("damageType cannot be null");
		if (itemId == null) throw new IllegalArgumentException("itemId cannot be null");
		itemId.ifPresent(id -> {
			if (id.isBlank()) throw new IllegalArgumentException("itemId cannot be blank");
		});
	}

	public static ComboRequirement hits(int requiredHits) {
		return new ComboRequirement(requiredHits, Optional.empty(), Optional.empty(), Optional.empty());
	}
}
