package org.tomdang.player.playerresource;

@FunctionalInterface
public interface PlayerResourceListener {
	void changed(PlayerResourceSnapshot snapshot);
}
