package org.tomdang.bootstrap;

import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.tomdang.customarmorframework.CustomArmorCreator;
import org.tomdang.customarmorframework.CustomArmorRegistry;
import org.tomdang.customarmorframework.CustomArmorResolver;
import org.tomdang.customitemframework.CustomItemCreator;
import org.tomdang.customitemframework.CustomItemRegistry;
import org.tomdang.customitemframework.CustomItemResolver;

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

	public ItemBootStrap(NamespacedKey customIDKey) {
		customItemRegistry = new CustomItemRegistry();
		customItemCreator = new CustomItemCreator(customIDKey);
		customItemResolver = new CustomItemResolver(customIDKey, customItemRegistry);
		customArmorCreator = new CustomArmorCreator(customIDKey);
		customArmorRegistry = new CustomArmorRegistry(customArmorCreator, customItemRegistry);
		customArmorResolver = new CustomArmorResolver(customIDKey, customItemRegistry, customArmorRegistry);
	}

}
