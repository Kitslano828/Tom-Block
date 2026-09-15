package org.tomdang.player.stats.rule;

import org.tomdang.player.stats.PlayerStatType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerStatRuleRegistry {

	private final Map<PlayerStatType, PlayerStatRule> rules = new EnumMap<>(PlayerStatType.class);

	public void register(PlayerStatRule rule) {
		if (rule == null) throw new IllegalArgumentException("rule cannot be null");
		if (rules.putIfAbsent(rule.getStatType(), rule) != null) {
			throw new IllegalStateException("Rule already registered for " + rule.getStatType().name());
		}
	}

	public void registerAll(List<PlayerStatRule> entries) {
		if (entries == null) throw new IllegalArgumentException("entries cannot be null");
		for (PlayerStatRule entry : entries) register(entry);
	}

	public PlayerStatRule get(PlayerStatType statType) {
		if (statType == null) throw new IllegalArgumentException("statType cannot be null");
		PlayerStatRule rule = rules.get(statType);
		if (rule == null) throw new IllegalStateException("No rule registered for " + statType.name());
		return rule;
	}

	public Map<PlayerStatType, PlayerStatRule> asMap() {
		return Collections.unmodifiableMap(rules);
	}
}
