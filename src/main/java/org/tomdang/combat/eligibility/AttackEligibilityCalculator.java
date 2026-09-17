package org.tomdang.combat.eligibility;

import java.util.Set;

public class AttackEligibilityCalculator {

	public boolean canDamage(AttackEligibilityRule rule, AttackSource source) {
		if (rule == null) throw new IllegalArgumentException("rule cannot be null");
		if (source == null) throw new IllegalArgumentException("source cannot be null");

		Set<AttackCapability> acceptedCapabilities = rule.acceptedCapabilities();
		Set<AttackCapability> sourceCapabilities = source.attackCapabilities();

		if (rule.acceptedCapabilities().isEmpty()) return true;

		for (AttackCapability capability : acceptedCapabilities) {
			if (sourceCapabilities.contains(capability)) return true;
		}

		return false;
	}

}
