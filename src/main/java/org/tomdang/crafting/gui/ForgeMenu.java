package org.tomdang.crafting.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.tomdang.crafting.CraftingService;
import org.tomdang.crafting.craftingGrid.CraftingGrid;

import java.util.*;

public class ForgeMenu implements InventoryHolder {

	private final Inventory inventory;
	private static final int[] CRAFTING_POSITION = {
			10, 11, 12,
			19, 20, 21,
			28, 29, 30
	};
	private static final int OUTPUT_PREVIEW_SLOT = 34;
	private static final int CRAFTING_BUTTON_SLOT = 33;

	public ForgeMenu() {
		inventory = Bukkit.createInventory(this, 45, Component.text("Custom Forge"));
		inventory.setItem(CRAFTING_BUTTON_SLOT, getCraftingButton());


		Set<Integer> specialSlots = new HashSet<>();
		specialSlots.add(OUTPUT_PREVIEW_SLOT);
		specialSlots.add(CRAFTING_BUTTON_SLOT);
		for (int slot : CRAFTING_POSITION) {
			specialSlots.add(slot);
		}

		for (int i = 0; i < inventory.getSize(); i++) {
			if (specialSlots.contains(i)) {
				continue;
			}
			inventory.setItem(i, getFillerItem());
		}
	}

	public void refreshOutputPreview(CraftingService craftingService) {
		CraftingGrid craftingGrid = createCraftingGrid();
		ItemStack previewItem = craftingService.preview(craftingGrid);
		setOutputPreview(previewItem);
	}

	public CraftingGrid createCraftingGrid() {
		ItemStack[] grid = new ItemStack[9];
		for (int i = 0; i < 9; i++) {
			ItemStack readItem = inventory.getItem(CRAFTING_POSITION[i]);
			grid[i] = readItem;
		}

		return new CraftingGrid(grid);
	}

	public void applyCraftingGrid(CraftingGrid grid) {
		if (grid == null) return;

		for(int i = 0; i < 9; i++) {
			ItemStack item = grid.getItemAt(i);
			inventory.setItem(CRAFTING_POSITION[i], item);
		}
	}

	public boolean isCraftingPosition(int slot) {
		return Arrays.stream(CRAFTING_POSITION).anyMatch(pos -> pos == slot);
	}

	public boolean isCraftingButton(int slot) {
		return slot == CRAFTING_BUTTON_SLOT;
	}

	public boolean isOutputPreviewSlot(int slot) {
		return slot == OUTPUT_PREVIEW_SLOT;
	}

	private void setOutputPreview(ItemStack item) {
		if (item == null) {
			inventory.setItem(OUTPUT_PREVIEW_SLOT, null);
		} else {
			inventory.setItem(OUTPUT_PREVIEW_SLOT, item.clone());
		}
	}

	private ItemStack getCraftingButton() {
		ItemStack craftingButton = ItemStack.of(Material.DAMAGED_ANVIL);
		ItemMeta craftingButtonMeta = craftingButton.getItemMeta();
		craftingButtonMeta.displayName(Component.text("CRAFT", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
		List<Component> lore = new ArrayList<>();
		lore.add(Component.text("Click here to craft").decoration(TextDecoration.ITALIC, false));
		craftingButtonMeta.lore(lore);
		craftingButton.setItemMeta(craftingButtonMeta);
		return craftingButton;
	}

	private ItemStack getFillerItem() {
		ItemStack fillerItem = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE); // Typically pane is used for GUIs
		ItemMeta fillerItemMeta = fillerItem.getItemMeta();
		if (fillerItemMeta != null) {
			fillerItemMeta.displayName(Component.empty());
			fillerItemMeta.setHideTooltip(true);
			fillerItem.setItemMeta(fillerItemMeta);
		}
		return fillerItem;
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}

	public void open(Player player) {
		player.openInventory(inventory);
	}
}
