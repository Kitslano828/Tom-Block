package org.tomdang.combat.eligibility;

import java.util.Set;

public record AttackSource(AttackDelivery delivery, Set<AttackCapability> attackCapabilities) {

	public AttackSource {
		if (delivery == null) throw new IllegalArgumentException("delivery cannot be null");
		if (attackCapabilities == null) throw new IllegalArgumentException("attackCapabilities cannot be null");

		attackCapabilities = Set.copyOf(attackCapabilities);
	}

}
