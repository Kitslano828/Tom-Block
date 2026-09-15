package org.tomdang.player.stats.modifier;

import org.tomdang.player.stats.PlayerStatBlock;
import org.tomdang.player.stats.PlayerStatType;
import org.tomdang.player.stats.evaluation.PlayerStatCalculation;
import org.tomdang.player.stats.rule.PlayerStatRule;
import org.tomdang.player.stats.rule.PlayerStatRuleRegistry;

import java.util.Collection;

public class PlayerStatModifierCalculator {
	private final PlayerStatRuleRegistry ruleRegistry;

	public PlayerStatModifierCalculator(PlayerStatRuleRegistry ruleRegistry) {
		if (ruleRegistry == null) throw new IllegalArgumentException("ruleRegistry cannot be null");
		this.ruleRegistry = ruleRegistry;
	}

	public double calculate(PlayerStatBlock statBlock, PlayerStatType statType, Collection<PlayerStatModifier> statModifiers) {
		return calculateResult(statBlock, statType, statModifiers).effectiveValue();
	}

	public PlayerStatCalculation calculateResult(PlayerStatBlock statBlock, PlayerStatType statType,
	                                             Collection<PlayerStatModifier> statModifiers) {
		if (statBlock == null) throw new IllegalArgumentException("statBlock cannot be null");
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (statModifiers == null) throw new IllegalArgumentException("statModifiers cannot be null");

		double rawValue = statBlock.get(statType);
		for (PlayerStatModifier statModifier : statModifiers) {
			if (statModifier == null) throw new IllegalArgumentException("statModifiers cannot contain null");
			if (statModifier.getStatType() != statType) continue;
			rawValue += statModifier.getAmount();
			if (!Double.isFinite(rawValue)) throw new IllegalStateException("calculated stat total must be finite");
		}

		double effectiveValue = Math.max(rawValue, statType.getMinimumValue());
		PlayerStatRule rule = ruleRegistry.get(statType);
		if (rule.getCap().isPresent()) {
			effectiveValue = Math.min(effectiveValue, rule.getCap().getAsDouble());
		}

		return new PlayerStatCalculation(statType, rawValue, effectiveValue, rule.getCap());
	}

}
