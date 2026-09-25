package org.tomdang.player.playeractionbar;

import net.kyori.adventure.text.Component;
import java.util.UUID;

/** Temporary legacy messages enter the single HUD carrier through this boundary. */
public interface ActionBarMessageSink {
	void show(UUID playerId, Component message, long expiresAtTick);
	void clear(UUID playerId);
}
