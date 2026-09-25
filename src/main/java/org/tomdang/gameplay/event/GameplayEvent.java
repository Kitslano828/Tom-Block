package org.tomdang.gameplay.event;

import java.util.UUID;

/** A completed gameplay fact concerning one player. Events are immutable and published synchronously. */
public interface GameplayEvent {
	UUID playerId();
}
