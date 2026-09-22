package org.tomdang.foraging;

import java.util.HashMap;
import java.util.Map;

public final class ForagingToolRegistry {
	private final Map<String, ForagingToolDefinition> tools = new HashMap<>();
	public void register(ForagingToolDefinition tool) {
		if (tools.putIfAbsent(tool.customItemId(), tool) != null) throw new IllegalArgumentException("Duplicate foraging tool: " + tool.customItemId());
	}
	public ForagingToolDefinition find(String customItemId) { return tools.get(customItemId); }
}
