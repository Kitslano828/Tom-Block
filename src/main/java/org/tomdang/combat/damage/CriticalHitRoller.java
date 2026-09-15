package org.tomdang.combat.damage;

@FunctionalInterface
public interface CriticalHitRoller {
	boolean isCritical(double criticalChancePercent);
}
