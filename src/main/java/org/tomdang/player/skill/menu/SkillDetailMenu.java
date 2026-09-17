package org.tomdang.player.skill.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.skill.SkillProgress;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.stats.presentation.PlayerStatMenuItemFactory;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

public final class SkillDetailMenu implements InventoryHolder {
	private final Inventory inventory;
	private final SkillType skill;
	private final SkillMenuConfiguration configuration;

	public SkillDetailMenu(SkillMenuConfiguration configuration, SkillType skill, PlayerProfile profile, PlayerStatPresentationRegistry statPresentations) {
		if (configuration == null || skill == null || profile == null || statPresentations == null) throw new IllegalArgumentException("Menu inputs are required");
		this.configuration = configuration;
		this.skill = skill;
		SkillMenuConfiguration.Entry entry = configuration.entries().get(skill);
		SkillProgress progress = skill.progress(profile);
		inventory = Bukkit.createInventory(this, 54, Component.text(entry.name() + " Skill"));
		new SkillMenuBackgroundRenderer().render(inventory, configuration, entry.accent());
		PlayerStatMenuItemFactory factory = new PlayerStatMenuItemFactory();
		SkillMenuItemRenderer renderer = new SkillMenuItemRenderer(statPresentations);
		inventory.setItem(configuration.detailSlot(), factory.create(renderer.render(skill, entry, progress, true)));
		int filled = progress.maxLevel() ? 7 : (int) Math.floor(progress.fraction() * 7);
		for (int index = 0; index < 7; index++)
			inventory.setItem(28 + index, factory.createFiller(index < filled
					? entry.progressFilled() : configuration.progressEmpty()));
		inventory.setItem(configuration.backSlot(), factory.create(renderer.navigation(configuration.backMaterial(), "Back to Skills")));
		inventory.setItem(configuration.closeSlot(), factory.create(renderer.navigation(configuration.closeMaterial(), "Close")));
	}

	public SkillType skill() { return skill; }
	public boolean isBackSlot(int rawSlot) { return rawSlot == configuration.backSlot(); }
	public boolean isCloseSlot(int rawSlot) { return rawSlot == configuration.closeSlot(); }
	@Override public Inventory getInventory() { return inventory; }
}
