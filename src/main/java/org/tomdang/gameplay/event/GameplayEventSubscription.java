package org.tomdang.gameplay.event;

public interface GameplayEventSubscription extends AutoCloseable {
	@Override void close();
}
