package org.tomdang.quest.presentation;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.hud.composition.HudRuntime;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.orchestration.QuestLifecycleBus;
import org.tomdang.quest.progress.QuestProgress;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.progress.QuestStatus;

import java.util.Comparator;
import java.util.UUID;

/** Projects authoritative quest state into the shared HUD runtime. */
public final class QuestTrackerHudService implements AutoCloseable {
	private final QuestRegistry definitions;
	private final QuestProgressService progress;
	private final HudRuntime hud;
	private final AutoCloseable subscription;
	private final BukkitTask refreshTask;

	public QuestTrackerHudService(Plugin plugin, QuestRegistry definitions, QuestProgressService progress,
			QuestLifecycleBus lifecycle, HudRuntime hud) {
		if (plugin == null || definitions == null || progress == null || lifecycle == null || hud == null)
			throw new IllegalArgumentException("Quest tracker dependencies are required");
		this.definitions = definitions;
		this.progress = progress;
		this.hud = hud;
		this.subscription = lifecycle.subscribe(event -> refresh(event.playerId()));
		this.refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::refreshOnline, 20L, 20L);
	}

	public void refresh(UUID playerId) {
		if (!progress.isLoaded(playerId)) return;
		QuestProgress active = progress.progress(playerId).stream()
				.filter(value -> value.status() == QuestStatus.ACTIVE)
				.min(Comparator.comparing(QuestProgress::startedAt)).orElse(null);
		long tick = Bukkit.getCurrentTick();
		if (active == null) hud.hide(playerId, TrackedQuestHudElement.ID, tick);
		else hud.show(playerId, new TrackedQuestHudElement(definitions.require(active.questId()), active), tick);
	}

	private void refreshOnline() { Bukkit.getOnlinePlayers().forEach(player -> refresh(player.getUniqueId())); }

	@Override public void close() {
		refreshTask.cancel();
		try { subscription.close(); }
		catch (Exception exception) { throw new IllegalStateException("Could not close quest tracker subscription", exception); }
		Bukkit.getOnlinePlayers().forEach(player -> hud.hide(player.getUniqueId(), TrackedQuestHudElement.ID, Bukkit.getCurrentTick()));
	}
}
