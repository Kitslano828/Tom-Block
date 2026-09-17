package org.tomdang.player.skill.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.skill.SkillType;
import org.tomdang.player.stats.presentation.PlayerStatPresentationRegistry;

public final class SkillsMenuListener implements Listener {
	private final PlayerProfileService profiles;
	private final SkillMenuConfiguration configuration;
	private final PlayerStatPresentationRegistry statPresentations;

	public SkillsMenuListener(PlayerProfileService profiles, SkillMenuConfiguration configuration, PlayerStatPresentationRegistry statPresentations) {
		if (profiles == null || configuration == null || statPresentations == null) throw new IllegalArgumentException("Listener dependencies are required");
		this.profiles = profiles;
		this.configuration = configuration;
		this.statPresentations = statPresentations;
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Object holder = event.getView().getTopInventory().getHolder();
		if (!(holder instanceof SkillsOverviewMenu) && !(holder instanceof SkillDetailMenu)) return;
		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player player)) return;
		PlayerProfile profile = profiles.getPlayerProfileFromMap(player.getUniqueId());
		if (profile == null) { player.closeInventory(); return; }
		int slot = event.getRawSlot();
		if (holder instanceof SkillsOverviewMenu overview) {
			if (overview.isCloseSlot(slot)) { player.closeInventory(); return; }
			SkillType skill = overview.skillAt(slot);
			if (skill != null) player.openInventory(new SkillDetailMenu(configuration, skill, profile, statPresentations).getInventory());
		} else if (holder instanceof SkillDetailMenu detail) {
			if (detail.isCloseSlot(slot)) player.closeInventory();
			else if (detail.isBackSlot(slot)) player.openInventory(new SkillsOverviewMenu(configuration, profile, statPresentations).getInventory());
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		Object holder = event.getView().getTopInventory().getHolder();
		if (holder instanceof SkillsOverviewMenu || holder instanceof SkillDetailMenu) event.setCancelled(true);
	}
}
