package org.tomdang.player.skill.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.stats.presentation.PlayerStatMenuItemFactory;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

public final class SkillsOverviewMenu implements InventoryHolder {
	private final Inventory inventory;
	private final SkillMenuConfiguration configuration;

	public SkillsOverviewMenu(SkillMenuConfiguration configuration, PlayerProfile profile, PlayerStatPresentationRegistry statPresentations) {
		if (configuration == null || profile == null || statPresentations == null) throw new IllegalArgumentException("Menu inputs are required");
		this.configuration = configuration;
		inventory = Bukkit.createInventory(this, 54, Component.text(configuration.title()));
		new SkillMenuBackgroundRenderer().render(inventory, configuration, configuration.overviewAccent());
		PlayerStatMenuItemFactory factory = new PlayerStatMenuItemFactory();
		SkillMenuItemRenderer renderer = new SkillMenuItemRenderer(statPresentations);
		for (SkillType skill : SkillType.values()) {
			SkillMenuConfiguration.Entry entry = configuration.entries().get(skill);
			inventory.setItem(entry.slot(), factory.create(renderer.render(skill, entry, skill.progress(profile), false)));
		}
		inventory.setItem(configuration.closeSlot(), factory.create(renderer.navigation(configuration.closeMaterial(), "Close")));
	}

	public SkillType skillAt(int rawSlot) {
		for (SkillType skill : SkillType.values())
			if (configuration.entries().get(skill).slot() == rawSlot) return skill;
		return null;
	}

	public boolean isCloseSlot(int rawSlot) { return rawSlot == configuration.closeSlot(); }
	@Override public Inventory getInventory() { return inventory; }
}
