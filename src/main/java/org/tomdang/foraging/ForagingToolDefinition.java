package org.tomdang.foraging;

public record ForagingToolDefinition(String customItemId, int requiredForagingLevel) {
	public ForagingToolDefinition {
		if (customItemId == null || customItemId.isBlank() || requiredForagingLevel < 0) throw new IllegalArgumentException("Invalid foraging tool");
	}
}
