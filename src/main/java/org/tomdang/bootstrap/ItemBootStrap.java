package org.tomdang.bootstrap;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.tomdang.TomBlock;
import org.tomdang.customarmorframework.CustomArmorCreator;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customarmorframework.CustomArmorResolver;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.configuration.CustomItemConfigurationLoader;
import org.tomdang.customitemframework.configuration.CustomItemDefinition;
import org.tomdang.customitemframework.configuration.CustomItemDefinitionRegistrar;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ItemBootStrap {

	@Getter
	private final CustomItemRegistry customItemRegistry;
	@Getter
	private final CustomItemCreator customItemCreator;
	@Getter
	private final CustomItemResolver customItemResolver;
	@Getter
	private final CustomArmorRegistry customArmorRegistry;
	@Getter
	private final CustomArmorCreator customArmorCreator;
	@Getter
	private final CustomArmorResolver customArmorResolver;

	public ItemBootStrap(TomBlock instance, NamespacedKey customIDKey,
	                     PlayerStatPresentationRegistry statPresentations) {
		customItemRegistry = new CustomItemRegistry();
		customItemCreator = new CustomItemCreator(customIDKey, statPresentations);
		customItemResolver = new CustomItemResolver(customIDKey, customItemRegistry);

		CustomItemConfigurationLoader configurationLoader = new CustomItemConfigurationLoader();
		List<CustomItemDefinition> definitions;
		try (InputStream configurationStream = instance.getResource("items.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain items.yml");
			}
			definitions = configurationLoader.loadDefinitions(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled items.yml resource", exception);
		}
		new CustomItemDefinitionRegistrar(customItemRegistry).registerDefinitions(definitions);

		customArmorCreator = new CustomArmorCreator(customIDKey, statPresentations);
		customArmorRegistry = new CustomArmorRegistry(customArmorCreator, customItemRegistry);
		customArmorResolver = new CustomArmorResolver(customIDKey, customItemRegistry, customArmorRegistry);
	}

}
