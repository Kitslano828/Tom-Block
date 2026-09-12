package org.tomdang.bootstrap;

import lombok.Getter;
import org.tomdang.TomBlock;
import org.tomdang.crafting.CraftingRecipeMatcher;
import org.tomdang.crafting.CraftingService;
import org.tomdang.crafting.configuration.CraftingRecipeConfigurationLoader;
import org.tomdang.crafting.configuration.CraftingRecipeDefinitionRegistrar;
import org.tomdang.crafting.configuration.ShapedCraftingRecipeDefinition;
import org.tomdang.crafting.recipe.CraftingRecipeRegistry;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.customitemframework.CustomItemStackFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CraftingBootStrap {

	@Getter
	private final CraftingService craftingService;

	public CraftingBootStrap(TomBlock instance, CustomItemResolver customItemResolver, CustomItemRegistry customItemRegistry,
							 CustomItemStackFactory customItemStackFactory) {

		CraftingRecipeRegistry craftingRecipeRegistry = new CraftingRecipeRegistry();
		CraftingRecipeMatcher craftingRecipeMatcher = new CraftingRecipeMatcher(customItemResolver);

		CraftingRecipeConfigurationLoader configurationLoader = new CraftingRecipeConfigurationLoader();
		List<ShapedCraftingRecipeDefinition> definitions;
		try (InputStream configurationStream = instance.getResource("recipes.yml")) {
			if (configurationStream == null) {
				throw new IllegalStateException("TomBlock.jar does not contain recipes.yml");
			}
			definitions = configurationLoader.loadDefinitions(
					new InputStreamReader(configurationStream, StandardCharsets.UTF_8)
			);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not close the bundled recipes.yml resource", exception);
		}
		new CraftingRecipeDefinitionRegistrar(craftingRecipeRegistry, customItemRegistry)
				.registerDefinitions(definitions);

		craftingService = new CraftingService(
				craftingRecipeRegistry,
				craftingRecipeMatcher,
				customItemRegistry,
				customItemStackFactory
		);
	}

}
