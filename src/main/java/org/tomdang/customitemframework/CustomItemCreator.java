package org.tomdang.customitemframework;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.tomdang.customitemframework.lore.CustomItemLoreRenderer;
import org.tomdang.customitemframework.lore.ItemLoreContext;
import org.tomdang.customitemframework.stats.CustomItemStatLoreRenderer;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

import java.util.Collection;
import java.util.List;

public class CustomItemCreator {
	private final NamespacedKey customItemIdKey;
	private final CustomItemStatLoreRenderer statLoreRenderer;
	private final CustomItemLoreRenderer itemLoreRenderer;

	public CustomItemCreator(NamespacedKey customItemIdKey) {
		this(customItemIdKey, null);
	}

	public CustomItemCreator(NamespacedKey customItemIdKey, PlayerStatPresentationRegistry presentations) {
		this.customItemIdKey = customItemIdKey;
		statLoreRenderer = presentations == null
				? new CustomItemStatLoreRenderer()
				: new CustomItemStatLoreRenderer(presentations);
		itemLoreRenderer = presentations == null
				? new CustomItemLoreRenderer()
				: new CustomItemLoreRenderer(presentations);
	}

	public ItemStack createItemStack(CustomItem customItem) {
		return createItemStack(customItem, ItemLoreContext.defaults());
	}

	public ItemStack createItemStack(CustomItem customItem, ItemLoreContext context) {
		if (customItem == null) throw new IllegalArgumentException("customItem cannot be null");
		if (context == null) throw new IllegalArgumentException("context cannot be null");
		ItemStack item;

		item = ItemStack.of(customItem.getMaterial());
		ItemMeta meta = item.getItemMeta();

		meta.getPersistentDataContainer().set(customItemIdKey, PersistentDataType.STRING, customItem.getId());
		meta.displayName(Component.text(customItem.getDisplayName()).color(customItem.getRarity().getColor()).decoration(TextDecoration.ITALIC, false));
		meta.lore(itemLoreRenderer.render(customItem, context));

		item.setItemMeta(meta);
		return item;
	}

	protected List<Component> renderStatLore(CustomItem customItem) {
		return statLoreRenderer.render(customItem.getStatModifiers());
	}

	protected List<Component> renderLore(CustomItem customItem, Collection<Component> leadingLore) {
		return itemLoreRenderer.render(customItem, leadingLore);
	}

	protected List<Component> renderLore(CustomItem customItem, Collection<Component> leadingLore,
	                                     ItemLoreContext context) {
		return itemLoreRenderer.render(customItem, leadingLore, context);
	}

}
