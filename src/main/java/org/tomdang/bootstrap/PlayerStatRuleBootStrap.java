package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.TomBlock;
import org.tomdang.player.stats.rule.PlayerStatRule;
import org.tomdang.player.stats.rule.PlayerStatRuleConfigurationLoader;
import org.tomdang.player.stats.rule.PlayerStatRuleRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PlayerStatRuleBootStrap {

	@Getter
	private final PlayerStatRuleRegistry registry;

	public PlayerStatRuleBootStrap(TomBlock instance) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");

		List<PlayerStatRule> rules;
		try (InputStream configurationStream = instance.getResource("stats/stat-rules.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain stat-rules.yml");
			}
			rules = new PlayerStatRuleConfigurationLoader().load(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled stat-rules.yml resource", exception);
		}

		registry = new PlayerStatRuleRegistry();
		registry.registerAll(rules);
	}
}
