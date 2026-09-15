package org.tomdang.bootstrap;

import org.tomdang.TomBlock;
import org.tomdang.customabilityframework.CustomAbilityRegistry;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customarmorframework.configuration.CustomArmorConfigurationLoader;
import org.tomdang.customarmorframework.configuration.CustomArmorDefinition;
import org.tomdang.customarmorframework.configuration.CustomArmorDefinitionRegistrar;
import org.tomdang.customitemframework.CustomItemRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ArmorConfigurationBootStrap {

	public ArmorConfigurationBootStrap(TomBlock instance, CustomArmorRegistry armorRegistry,
	                                   CustomItemRegistry itemRegistry, CustomAbilityRegistry abilityRegistry) {
		if (instance == null) throw new IllegalArgumentException("instance cannot be null");
		if (armorRegistry == null) throw new IllegalArgumentException("armorRegistry cannot be null");
		if (itemRegistry == null) throw new IllegalArgumentException("itemRegistry cannot be null");
		if (abilityRegistry == null) throw new IllegalArgumentException("abilityRegistry cannot be null");

		List<CustomArmorDefinition> definitions;
		try (InputStream configurationStream = instance.getResource("armor.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain armor.yml");
			}
			definitions = new CustomArmorConfigurationLoader().loadDefinitions(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled armor.yml resource", exception);
		}

		new CustomArmorDefinitionRegistrar(armorRegistry, itemRegistry, abilityRegistry)
				.registerDefinitions(definitions);
	}
}
