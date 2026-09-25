package org.tomdang.hud.notification;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.presentation.*;
import java.util.*;

/** The sole application boundary for transient, non-chat HUD messages. */
public final class HudNotificationService {
	private static final HudElementId ID = HudElementId.of("tomblock", "notification");
	private final Plugin plugin;
	private final ProductionHudService hud;
	private final Map<UUID, Long> revisions = new HashMap<>();
	public HudNotificationService(Plugin plugin, ProductionHudService hud) {
		this.plugin = Objects.requireNonNull(plugin);
		this.hud = Objects.requireNonNull(hud);
	}
	public void show(UUID playerId, Component message, long durationTicks) {
		if (playerId == null || message == null || durationTicks <= 0)
			throw new IllegalArgumentException("Notification arguments are invalid");
		String text = PlainTextComponentSerializer.plainText().serialize(message).trim();
		if (text.isEmpty()) return;
		long now = Bukkit.getCurrentTick();
		long revision = revisions.merge(playerId, 1L, Long::sum);
		var spec = new HudElementSpec(ID, HudRegion.NOTIFICATION, 100, false, now + durationTicks, Set.of());
		hud.show(playerId, spec, new HudNotificationModel(text, tone(message)), now);
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (Objects.equals(revisions.get(playerId), revision)) {
				revisions.remove(playerId);
				hud.hide(playerId, spec, Bukkit.getCurrentTick());
			}
		}, durationTicks);
	}
	public void clear(UUID playerId) {
		revisions.remove(playerId);
		hud.hide(playerId, HudElementSpec.persistent(ID, HudRegion.NOTIFICATION, 100), Bukkit.getCurrentTick());
	}
	private HudNotificationTone tone(Component component) {
		TextColor color = component.color();
		if (color == null) return HudNotificationTone.INFO;
		int rgb = color.value();
		if (rgb == 0xFF5555 || rgb == 0xAA0000) return HudNotificationTone.WARNING;
		if (rgb == 0x55FF55 || rgb == 0x00AA00) return HudNotificationTone.SUCCESS;
		if (rgb == 0xFFAA00 || rgb == 0xFFFF55 || rgb == 0x55FFFF || rgb == 0x00AAAA) return HudNotificationTone.ACCENT;
		if (rgb == 0xAAAAAA || rgb == 0x555555) return HudNotificationTone.MUTED;
		return HudNotificationTone.INFO;
	}
}
