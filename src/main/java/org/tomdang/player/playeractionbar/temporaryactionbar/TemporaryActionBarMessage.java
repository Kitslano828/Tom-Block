package org.tomdang.player.playeractionbar.temporaryactionbar;

import lombok.Getter;
import net.kyori.adventure.text.Component;

public class TemporaryActionBarMessage {
	@Getter
	private final Component message;
	@Getter
	private final long expiresAt;
	@Getter
	private final int pixelWidth;

	public TemporaryActionBarMessage(Component message, long expiresAt, int pixelWidth) {
		this.message = message;
		this.expiresAt = expiresAt;
		this.pixelWidth = pixelWidth;
	}
}
