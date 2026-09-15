package org.tomdang.player.stats.modifier;

import org.tomdang.player.stats.PlayerStatBlock;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatCalculation;
import org.tomdang.player.stats.rule.PlayerStatRule;
import org.tomdang.player.stats.rule.PlayerStatRuleRegistry;
import org.tomdang.player.stats.modifier.cap.PlayerStatCapModifier;

import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;

public class PlayerStatModifierCalculator {
	private final PlayerStatRuleRegistry ruleRegistry;

	public PlayerStatModifierCalculator(PlayerStatRuleRegistry ruleRegistry) {
		if (ruleRegistry == null) throw new IllegalArgumentException("ruleRegistry cannot be null");
		this.ruleRegistry = ruleRegistry;
	}

	public double calculate(PlayerStatBlock statBlock, PlayerStatType statType, Collection<PlayerStatModifier> statModifiers) {
		return calculateResult(statBlock, statType, statModifiers).effectiveValue();
	}

	public double calculate(PlayerStatBlock statBlock, PlayerStatType statType,
	                        Collection<PlayerStatModifier> statModifiers,
	                        Collection<PlayerStatCapModifier> capModifiers) {
		return calculateResult(statBlock, statType, statModifiers, capModifiers).effectiveValue();
	}

	public PlayerStatCalculation calculateResult(PlayerStatBlock statBlock, PlayerStatType statType,
	                                             Collection<PlayerStatModifier> statModifiers) {
		return calculateResult(statBlock, statType, statModifiers, List.of());
	}

	public PlayerStatCalculation calculateResult(PlayerStatBlock statBlock, PlayerStatType statType,
	                                             Collection<PlayerStatModifier> statModifiers,
	                                             Collection<PlayerStatCapModifier> capModifiers) {
		if (statBlock == null) throw new IllegalArgumentException("statBlock cannot be null");
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (statModifiers == null) throw new IllegalArgumentException("statModifiers cannot be null");
		if (capModifiers == null) throw new IllegalArgumentException("capModifiers cannot be null");

		double rawValue = statBlock.get(statType);
		for (PlayerStatModifier statModifier : statModifiers) {
			if (statModifier == null) throw new IllegalArgumentException("statModifiers cannot contain null");
			if (statModifier.getStatType() != statType) continue;
			rawValue += statModifier.getAmount();
			if (!Double.isFinite(rawValue)) throw new IllegalStateException("calculated stat total must be finite");
		}

		double effectiveValue = Math.max(rawValue, statType.getMinimumValue());
		PlayerStatRule rule = ruleRegistry.get(statType);
		OptionalDouble configuredCap = rule.getCap();
		double capModifierTotal = 0;
		for (PlayerStatCapModifier capModifier : capModifiers) {
			if (capModifier == null) throw new IllegalArgumentException("capModifiers cannot contain null");
			if (capModifier.statType() != statType || configuredCap.isEmpty()) continue;
			capModifierTotal += capModifier.amount();
			if (!Double.isFinite(capModifierTotal)) {
				throw new IllegalStateException("calculated cap modifier total must be finite");
			}
		}

		OptionalDouble effectiveCap = OptionalDouble.empty();
		if (configuredCap.isPresent()) {
			double adjustedCap = configuredCap.getAsDouble() + capModifierTotal;
			if (!Double.isFinite(adjustedCap)) throw new IllegalStateException("calculated stat cap must be finite");
			adjustedCap = Math.max(adjustedCap, statType.getMinimumValue());
			effectiveCap = OptionalDouble.of(adjustedCap);
			effectiveValue = Math.min(effectiveValue, adjustedCap);
		}

		return new PlayerStatCalculation(
				statType, rawValue, effectiveValue, configuredCap, effectiveCap, capModifierTotal
		);
	}

}
