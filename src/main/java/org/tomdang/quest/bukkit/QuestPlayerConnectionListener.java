package org.tomdang.quest.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.quest.progress.QuestProgressService;
import org.tomdang.quest.definition.QuestRegistry;
import org.tomdang.quest.definition.QuestStartPolicy;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.UUID;
import java.util.function.Consumer;

public final class QuestPlayerConnectionListener implements Listener {
	private final QuestProgressService quests;
	private final QuestRegistry definitions;
	private final Logger logger;
	private final Consumer<UUID> afterJoin;

	public QuestPlayerConnectionListener(QuestProgressService quests, QuestRegistry definitions, Logger logger) {
		this(quests, definitions, logger, ignored -> {});
	}

	public QuestPlayerConnectionListener(QuestProgressService quests, QuestRegistry definitions, Logger logger,
			Consumer<UUID> afterJoin) {
		if (quests == null || definitions == null || logger == null) throw new IllegalArgumentException("Quest connection dependencies are required");
		this.quests = quests;
		this.definitions = definitions;
		this.logger = logger;
		this.afterJoin = afterJoin == null ? ignored -> {} : afterJoin;
	}

	@EventHandler public void onPreLogin(AsyncPlayerPreLoginEvent event) {
		try {
			quests.load(event.getUniqueId());
		} catch (RuntimeException exception) {
			logger.log(Level.SEVERE, "Failed to load quest progress for " + event.getUniqueId(), exception);
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
					"TomBlock could not load your quest progress. Please try again shortly.");
		}
	}

	@EventHandler public void onJoin(PlayerJoinEvent event) {
		for (var definition : definitions.all()) {
			if (definition.startPolicy() == QuestStartPolicy.AUTO_ON_JOIN
					&& quests.progress(event.getPlayer().getUniqueId(), definition.id()).isEmpty()) {
				quests.start(event.getPlayer().getUniqueId(), definition.id());
			}
		}
		afterJoin.accept(event.getPlayer().getUniqueId());
	}

	@EventHandler public void onQuit(PlayerQuitEvent event) {
		quests.unload(event.getPlayer().getUniqueId());
	}
}
