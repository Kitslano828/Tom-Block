package org.tomdang.player.skill.menu;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.tomdang.player.stats.presentation.PlayerStatMenuItemFactory;
import org.tomdang.player.stats.menu.BorderedMenuSlotCalculator;

import java.util.ArrayList;
import java.util.List;

/** A quiet, tooltip-free pane background with a small configurable accent edge. */
public final class SkillMenuBackgroundRenderer {
	public List<Material> layout(SkillMenuConfiguration configuration, Material accent) {
		if (configuration == null || accent == null) throw new IllegalArgumentException("Background inputs are required");
		List<Material> layout = new ArrayList<>(java.util.Collections.nCopies(54, configuration.overviewAccent()));
		for (int slot : new BorderedMenuSlotCalculator().borderSlots())
			layout.set(slot, configuration.backgroundMaterial());
		for (int slot : configuration.accentSlots()) layout.set(slot, accent);
		return List.copyOf(layout);
	}

	public void render(Inventory inventory, SkillMenuConfiguration configuration, Material accent) {
		if (inventory == null || inventory.getSize() != 54) throw new IllegalArgumentException("Skills inventory must have 54 slots");
		PlayerStatMenuItemFactory factory = new PlayerStatMenuItemFactory();
		List<Material> materials = layout(configuration, accent);
		for (int slot = 0; slot < materials.size(); slot++)
			inventory.setItem(slot, factory.createFiller(materials.get(slot)));
	}
}
