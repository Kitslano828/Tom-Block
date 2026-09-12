package org.tomdang.mining.configuration.miningblock;

public record MiningDropDefinition(
		String customItemId,
		int amount,
		double chance,
		boolean affectedByFortune
) {
}
