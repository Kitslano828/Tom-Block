package org.tomdang.bootstrap;

import org.bukkit.plugin.Plugin;
import org.tomdang.player.skill.menu.SkillMenuConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class SkillMenuBootstrap {
	private final SkillMenuConfiguration configuration;

	public SkillMenuBootstrap(Plugin plugin) {
		if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
		try (InputStream stream = plugin.getResource("skills/skills-menu.yml")) {
			if (stream == null) throw new IllegalStateException("TomBlock.jar is missing skills-menu.yml");
			configuration = SkillMenuConfiguration.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read skills-menu.yml", exception);
		}
	}

	public SkillMenuConfiguration configuration() { return configuration; }
}
