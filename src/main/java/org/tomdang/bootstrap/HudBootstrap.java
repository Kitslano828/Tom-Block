package org.tomdang.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.tomdang.TomBlock;
import org.tomdang.hud.composition.*;
import org.tomdang.hud.composition.bukkit.HudPlayerConnectionListener;
import org.tomdang.hud.composition.layout.HudLayoutEngine;
import org.tomdang.hud.composition.layout.MinecraftHudTextMetrics;
import org.tomdang.hud.composition.transport.ActionBarHudProtocolTransport;
import org.tomdang.hud.composition.transport.NoOpHudTransport;
import org.tomdang.hud.dialogue.HudDialoguePresenter;
import org.tomdang.hud.calendar.CalendarHudPresenter;
import org.tomdang.hud.hunting.HuntingHudPresenter;
import org.tomdang.hud.hunting.HuntingHudService;
import org.tomdang.hud.notification.HudNotificationPresenter;
import org.tomdang.hud.notification.HudNotificationService;
import org.tomdang.hud.presentation.HudPresenterRegistry;
import org.tomdang.hud.presentation.ProductionHudService;
import org.tomdang.hud.presentation.asset.HudAssetConfigurationLoader;
import org.tomdang.hud.presentation.theme.HudThemeConfigurationLoader;
import org.tomdang.hud.progression.ProgressionNotificationHudService;
import org.tomdang.hud.progression.ProgressionNotificationPresenter;
import org.tomdang.hud.protocol.HudPackStateRegistry;
import org.tomdang.hud.protocol.HudProtocolConfiguration;
import org.tomdang.hud.protocol.HudProtocolEncoder;
import org.tomdang.hud.protocol.bukkit.HudPackStatusListener;
import org.tomdang.hud.protocol.lab.HudLabCommand;
import org.tomdang.hud.status.*;
import org.tomdang.player.playeractionbar.ActionBarMessageSink;
import org.tomdang.player.playeractionbar.PlayerActionBarService;
import org.tomdang.player.playerresource.PlayerResourceService;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Composes the production HUD and owns its runtime lifecycle. */
public final class HudBootstrap implements AutoCloseable {
	private final HudRuntime runtime;
	private final ProductionHudService production;
	private final ProgressionNotificationHudService progressionNotifications;
	private final PlayerResourceValuesHudService resourceValues;
	private final HuntingHudService hunting;

	public HudBootstrap(TomBlock plugin, PlayerResourceService resources, PlayerActionBarService actionBar) {
		Objects.requireNonNull(plugin, "plugin");
		Objects.requireNonNull(resources, "resources");
		Objects.requireNonNull(actionBar, "actionBar");
		var layout = new HudLayoutConfigurationLoader().load(plugin.getResource("hud-layout.yml"));
		var packStates = new HudPackStateRegistry();
		var protocol = HudProtocolConfiguration.load(plugin.getResource("hud-protocol.properties"));
		var protocolTransport = new ActionBarHudProtocolTransport(plugin,
				new HudProtocolEncoder(protocol, layout), packStates,
				EnumSet.complementOf(EnumSet.of(HudRegion.MAP)));
		var failures = (HudFailureReporter) failure -> plugin.getLogger().warning(
				"HUD element " + failure.elementId() + " failed during " + failure.stage() + ": "
						+ failure.cause().getMessage());
		runtime = new HudRuntime(new PlayerHudSessionRegistry(),
				new HudCompositor(layout, new HudLayoutEngine(new MinecraftHudTextMetrics()),
						HudVisibilityPolicy.regionSuppression(), failures),
				List.of(new NoOpHudTransport(EnumSet.of(HudRegion.MAP)), protocolTransport));

		var presenters = new HudPresenterRegistry();
		presenters.register(new PlayerResourceValuesHudPresenter());
		presenters.register(new CalendarHudPresenter());
		presenters.register(new HudNotificationPresenter());
		presenters.register(new ProgressionNotificationPresenter());
		presenters.register(new HuntingHudPresenter());
		presenters.register(new HudDialoguePresenter());
		presenters.seal();
		production = new ProductionHudService(runtime, presenters,
				new HudThemeConfigurationLoader().load(plugin.getResource("hud-theme.yml")),
				new HudAssetConfigurationLoader().load(plugin.getResource("hud-assets.yml")));

		var notifications = new HudNotificationService(plugin, production);
		progressionNotifications = new ProgressionNotificationHudService(plugin, production);
		hunting = new HuntingHudService(production);
		resourceValues = new PlayerResourceValuesHudService(resources, production, Bukkit::getCurrentTick);
		plugin.getServer().getPluginManager().registerEvents(
				new PlayerResourceValuesHudConnectionListener(resourceValues), plugin);
		actionBar.setMessageSink(new ActionBarMessageSink() {
			@Override public void show(UUID playerId, net.kyori.adventure.text.Component message, long expiresAtTick) {
				notifications.show(playerId, message, Math.max(1L, expiresAtTick - Bukkit.getCurrentTick()));
			}
			@Override public void clear(UUID playerId) { notifications.clear(playerId); }
		});
		var packStatus = new HudPackStatusListener(packStates, plugin.getLogger(), plugin, protocol.packId());
		plugin.getServer().getPluginManager().registerEvents(packStatus, plugin);
		Objects.requireNonNull(plugin.getCommand("hudlab"), "Missing hudlab command")
				.setExecutor(new HudLabCommand(runtime, packStates, packStatus));
		plugin.getServer().getPluginManager().registerEvents(new HudPlayerConnectionListener(runtime), plugin);
		for (Player player : Bukkit.getOnlinePlayers()) {
			runtime.open(player.getUniqueId());
			resourceValues.refresh(player);
		}
	}

	public HudRuntime runtime() { return runtime; }
	public ProductionHudService production() { return production; }
	public ProgressionNotificationHudService progressionNotifications() { return progressionNotifications; }
	public HuntingHudService hunting() { return hunting; }

	@Override public void close() {
		progressionNotifications.close();
		resourceValues.close();
		runtime.close();
	}
}
