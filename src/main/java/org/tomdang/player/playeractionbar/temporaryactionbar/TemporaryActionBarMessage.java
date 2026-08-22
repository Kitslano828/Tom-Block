package org.tomdang.player.playeractionbar.temporaryactionbar;

import lombok.Getter;
import net.kyori.adventure.text.Component;

public class TemporaryActionBarMessage {
	@Getter
	private final Component message;
	@Getter
	private final long expiresAt;

	public TemporaryActionBarMessage(Component message, long expiresAt) {
		this.message = message;
		this.expiresAt = expiresAt;
	}
}
