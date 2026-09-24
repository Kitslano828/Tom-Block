package org.tomdang.player.playeractionbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.tomdang.hud.spacing.HudSpacingService;
import org.tomdang.hud.text.MinecraftDefaultTextWidthService;

import java.util.*;

public class PlayerActionBarService {

	private final TomBlock instance;
	private final PlayerProfileService playerProfileService;
	private final CustomItemResolver customItemResolver;
	private final ActionBarRegistry actionBarRegistry;
	private final Map<UUID, TemporaryActionBarMessage> temporaryActionBarMessageMap = new HashMap<>();
	private final Map<UUID, Map<String, ActionBarLayer>> overlayLayers = new HashMap<>();
	private final HudSpacingService spacingService = new HudSpacingService();
	private final MinecraftDefaultTextWidthService textWidthService = new MinecraftDefaultTextWidthService();

	public PlayerActionBarService(TomBlock instance, PlayerProfileService playerProfileService,
								  CustomItemResolver customItemResolver, ActionBarRegistry actionBarRegistry
	) {
		this.instance = instance;
		this.playerProfileService = playerProfileService;
		this.customItemResolver = customItemResolver;
		this.actionBarRegistry = actionBarRegistry;
	}

	public void scheduleActionBarUpdate() {
		Bukkit.getScheduler().runTaskTimer(instance, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				updateActionBar(player);
			}
		}, 1L, 2L);
	}

	public void updateActionBar(Player player) {
		if (player == null) throw new IllegalArgumentException("Player is null");
		UUID playerUUID = player.getUniqueId();

		PlayerProfile playerProfile = playerProfileService.getPlayerProfileFromMap(player.getUniqueId());
		ItemStack heldItem = player.getInventory().getItemInMainHand();
		CustomItem customItem = customItemResolver.getCustomItem(heldItem);

		PlayerActionBarContext playerActionBarContext = new PlayerActionBarContext(player, playerProfile, heldItem, customItem);

		Component finalActionBar = Component.empty();

		for (ActionBarProvider actionBarProvider : actionBarRegistry.getActionBarProviders()) {
			if (actionBarProvider.shouldDisplay(playerActionBarContext)) {
				finalActionBar = finalActionBar.append(zeroCentered(
						actionBarProvider.render(playerActionBarContext),
						actionBarProvider.getPixelWidth(playerActionBarContext)));
			}
		}

		for (ActionBarLayer layer : overlayLayers.getOrDefault(playerUUID, Map.of()).values()) {
			finalActionBar = finalActionBar.append(zeroCentered(layer.component(), layer.pixelWidth()));
		}

		if (temporaryActionBarMessageMap.containsKey(player.getUniqueId())) {
			TemporaryActionBarMessage temporaryMessage = temporaryActionBarMessageMap.get(player.getUniqueId());
			if (Bukkit.getCurrentTick() < temporaryMessage.getExpiresAt()) {
				finalActionBar = finalActionBar.append(zeroCentered(
						temporaryMessage.getMessage(), temporaryMessage.getPixelWidth()));
			} else {
				temporaryActionBarMessageMap.remove(player.getUniqueId());
			}
		}

		player.sendActionBar(finalActionBar);
	}

	public void showTemporaryMessage(Player player, Component component, long duration) {
		long expireAt = Bukkit.getCurrentTick() + duration;
		int width = textWidthService.measure(PlainTextComponentSerializer.plainText().serialize(component));
		TemporaryActionBarMessage temporaryActionBarMessage = new TemporaryActionBarMessage(component, expireAt, width);
		temporaryActionBarMessageMap.put(player.getUniqueId(), temporaryActionBarMessage);
	}

	public void setOverlay(Player player, String layerId, Component component, int pixelWidth) {
		if (player == null || component == null) throw new IllegalArgumentException("Player and component are required");
		if (layerId == null || layerId.isBlank()) throw new IllegalArgumentException("Layer id cannot be blank");
		if (pixelWidth < 0) throw new IllegalArgumentException("Layer width cannot be negative");
		overlayLayers.computeIfAbsent(player.getUniqueId(), ignored -> new LinkedHashMap<>())
				.put(layerId, new ActionBarLayer(component, pixelWidth));
		updateActionBar(player);
	}

	public void clearOverlay(Player player, String layerId) {
		if (player == null) throw new IllegalArgumentException("Player cannot be null");
		Map<String, ActionBarLayer> layers = overlayLayers.get(player.getUniqueId());
		if (layers != null) {
			layers.remove(layerId);
			if (layers.isEmpty()) overlayLayers.remove(player.getUniqueId());
		}
		updateActionBar(player);
	}

	private Component zeroCentered(Component component, int pixelWidth) {
		if (pixelWidth == 0) return component;
		int left = pixelWidth / 2;
		// Keep spacing and content as siblings. If the spacing component is the
		// root, its custom font is inherited by ordinary text and renders boxes.
		return Component.empty()
				.append(spacingService.createSpacing(-left))
				.append(component)
				.append(spacingService.createSpacing(-(pixelWidth - left)));
	}

	private record ActionBarLayer(Component component, int pixelWidth) {}
}
