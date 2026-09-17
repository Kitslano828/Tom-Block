package org.tomdang.combat.eligibility;

import java.util.Set;

public record AttackEligibilityRule(Set<AttackCapability> acceptedCapabilities) {

	public AttackEligibilityRule {
		if (acceptedCapabilities == null) throw new IllegalArgumentException("acceptedCapabilities cannot be null");

		acceptedCapabilities = Set.copyOf(acceptedCapabilities);
	}

}
