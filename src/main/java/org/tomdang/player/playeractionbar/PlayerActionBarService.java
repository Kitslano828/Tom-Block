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
import org.tomdang.hud.text.MinecraftDefaultTextWidthService;

import java.util.*;

/** Compatibility facade; all output is delegated to the authoritative HUD notification engine. */
public class PlayerActionBarService {

	private final TomBlock instance;
	private final PlayerProfileService playerProfileService;
	private final CustomItemResolver customItemResolver;
	private final ActionBarRegistry actionBarRegistry;
	private final Map<UUID, TemporaryActionBarMessage> temporaryActionBarMessageMap = new HashMap<>();
	private final MinecraftDefaultTextWidthService textWidthService = new MinecraftDefaultTextWidthService();
	private ActionBarMessageSink messageSink;

	public PlayerActionBarService(TomBlock instance, PlayerProfileService playerProfileService,
								  CustomItemResolver customItemResolver, ActionBarRegistry actionBarRegistry
	) {
		this.instance = instance;
		this.playerProfileService = playerProfileService;
		this.customItemResolver = customItemResolver;
		this.actionBarRegistry = actionBarRegistry;
	}

	public void scheduleActionBarUpdate() {
		// Retained as a compatibility lifecycle hook while callers migrate to the HUD facade.
		// Notifications schedule their own expiry through the authoritative HUD engine.
	}

	public void updateActionBar(Player player) {
		if (player == null) throw new IllegalArgumentException("Player is null");
		// No renderer lives here anymore. The action bar is exclusively the HUD engine's transport.
	}

	public void showTemporaryMessage(Player player, Component component, long duration) {
		if (player == null || component == null || duration <= 0)
			throw new IllegalArgumentException("Temporary HUD message arguments are invalid");
		if (messageSink == null) throw new IllegalStateException("HUD notification sink has not been installed");
		messageSink.show(player.getUniqueId(), component, Bukkit.getCurrentTick() + duration);
	}

	public void setMessageSink(ActionBarMessageSink messageSink) {
		this.messageSink = java.util.Objects.requireNonNull(messageSink);
	}
}
