package org.tomdang.player.stats.modifier;

import org.tomdang.player.stats.PlayerStatBlock;
import org.tomdang.player.stats.PlayerStatType;

import java.util.Collection;

public class PlayerStatModifierCalculator {

	public double calculate(PlayerStatBlock statBlock, PlayerStatType statType, Collection<PlayerStatModifier> statModifiers) {
		if (statBlock == null) throw new IllegalArgumentException("statBlock cannot be null");
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		if (statModifiers == null) throw new IllegalArgumentException("statModifiers cannot be null");

		double total = statBlock.get(statType);
		for (PlayerStatModifier statModifier : statModifiers) {
			if (statModifier == null) throw new IllegalArgumentException("statModifiers cannot contain null");
			if (statModifier.getStatType() != statType) continue;
			total += statModifier.getAmount();
			if (!Double.isFinite(total)) throw new IllegalStateException("calculated stat total must be finite");
		}

		if (total < statType.getMinimumValue()) total = statType.getMinimumValue();

		return total;
	}

}
