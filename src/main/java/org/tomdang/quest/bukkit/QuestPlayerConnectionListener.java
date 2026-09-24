package org.tomdang.quest.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.tomdang.quest.progress.QuestProgressService;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class QuestPlayerConnectionListener implements Listener {
	public static final String INTRO_QUEST = "INTRO_TO_HUNTING";
	private final QuestProgressService quests;
	private final Logger logger;

	public QuestPlayerConnectionListener(QuestProgressService quests, Logger logger) {
		if (quests == null || logger == null) throw new IllegalArgumentException("Quest connection dependencies are required");
		this.quests = quests;
		this.logger = logger;
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
		if (quests.progress(event.getPlayer().getUniqueId(), INTRO_QUEST).isEmpty())
			quests.start(event.getPlayer().getUniqueId(), INTRO_QUEST);
	}

	@EventHandler public void onQuit(PlayerQuitEvent event) {
		quests.unload(event.getPlayer().getUniqueId());
	}
}
