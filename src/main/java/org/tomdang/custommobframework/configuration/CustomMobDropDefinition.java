package org.tomdang.custommobframework.configuration;

public record CustomMobDropDefinition(String itemId, int amount, double chance) {
	public CustomMobDropDefinition {
		if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId cannot be blank");
		if (amount < 1) throw new IllegalArgumentException("amount must be at least 1");
		if (!Double.isFinite(chance) || chance < 0 || chance > 100) {
			throw new IllegalArgumentException("chance must be finite and between 0 and 100");
		}
	}
}
