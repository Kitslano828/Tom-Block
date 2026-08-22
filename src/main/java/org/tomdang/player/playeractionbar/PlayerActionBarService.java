package org.tomdang.player.playeractionbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.tomdang.TomBlock;
import org.tomdang.customitemframework.CustomItem;
import org.tomdang.customitemframework.CustomItemResolver;
import org.tomdang.player.PlayerProfile;
import org.tomdang.player.PlayerProfileService;
import org.tomdang.player.playeractionbar.statsactionbarprovider.ActionBarProvider;
import org.tomdang.player.playeractionbar.temporaryactionbar.TemporaryActionBarMessage;

import java.util.*;

public class PlayerActionBarService {

	private final TomBlock instance;
	private final PlayerProfileService playerProfileService;
	private final CustomItemResolver customItemResolver;
	private final ActionBarRegistry actionBarRegistry;
	private final ActionBarSuppressionService actionBarSuppressionService;

	private final Map<UUID, TemporaryActionBarMessage> temporaryActionBarMessageMap = new HashMap<>();

	public PlayerActionBarService(TomBlock instance, PlayerProfileService playerProfileService,
								  CustomItemResolver customItemResolver, ActionBarRegistry actionBarRegistry,
								  ActionBarSuppressionService actionBarSuppressionService
	) {
		this.instance = instance;
		this.playerProfileService = playerProfileService;
		this.customItemResolver = customItemResolver;
		this.actionBarRegistry = actionBarRegistry;
		this.actionBarSuppressionService = actionBarSuppressionService;
	}

	public void scheduleActionBarUpdate() {
		Bukkit.getScheduler().runTaskTimer(instance, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				updateActionBar(player);
			}
		}, 1L, 25L);
	}

	public void updateActionBar(Player player) {
		if (player == null) throw new IllegalArgumentException("Player is null");
		UUID playerUUID = player.getUniqueId();

		if (actionBarSuppressionService.isSuppressed(playerUUID)) return;

		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		CustomItem customItem = customItemResolver.getCustomItem(heldItem);

		PlayerActionBarContext playerActionBarContext = new PlayerActionBarContext(player, playerProfile, heldItem, customItem);

		List<Component> visibleParts = new ArrayList<>();

		for (ActionBarProvider actionBarProvider : actionBarRegistry.getActionBarProviders()) {
			if (actionBarProvider.shouldDisplay(playerActionBarContext)) {
				visibleParts.add(actionBarProvider.render(playerActionBarContext));
			}
		}

		if (temporaryActionBarMessageMap.containsKey(player.getUniqueId())) {
			TemporaryActionBarMessage temporaryMessage = temporaryActionBarMessageMap.get(player.getUniqueId());
			if (Bukkit.getCurrentTick() < temporaryMessage.getExpiresAt()) {
				// The message is still active! Send it and skip rendering default stats
				visibleParts.add(temporaryMessage.getMessage());
			} else {
				temporaryActionBarMessageMap.remove(player.getUniqueId());
			}
		}

		Component finalActionBar = Component.join(
				JoinConfiguration.separator(Component.text("  |  ")), // 4 spaces
				visibleParts
		);

		player.sendActionBar(finalActionBar);
	}

	public void showTemporaryMessage(Player player, Component component, long duration) {
		long expireAt = Bukkit.getCurrentTick() + duration;
		TemporaryActionBarMessage temporaryActionBarMessage = new TemporaryActionBarMessage(component, expireAt);
		temporaryActionBarMessageMap.put(player.getUniqueId(), temporaryActionBarMessage);
	}
}
