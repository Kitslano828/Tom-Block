package org.tomdang.gameplay.event;

public final class GameplayEventDispatchException extends IllegalStateException {
	public GameplayEventDispatchException(GameplayEvent event, Throwable cause) {
		super("Gameplay event subscriber failed for " + event.getClass().getSimpleName(), cause);
	}
}
