package org.tomdang.gameplay.event;

@FunctionalInterface
public interface GameplayEventHandler<E extends GameplayEvent> {
	void handle(E event);
}
