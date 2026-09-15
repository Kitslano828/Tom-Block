package org.tomdang.combat.damage;

public record PlayerAttackResult(double damage, boolean critical) {
	public PlayerAttackResult {
		if (!Double.isFinite(damage) || damage < 0) throw new IllegalArgumentException("damage must be finite and non-negative");
	}
}
