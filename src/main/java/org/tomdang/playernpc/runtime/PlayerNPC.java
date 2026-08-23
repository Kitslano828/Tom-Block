package org.tomdang.playernpc.runtime;

import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public class PlayerNPC {

	@Getter
	private final ServerPlayer serverPlayer;

	public PlayerNPC(ServerPlayer serverPlayer) {
		if (serverPlayer == null) throw new IllegalArgumentException("Server Player cannot be null");

		this.serverPlayer = serverPlayer;
	}

	public UUID getProfileUUID() {
		return this.serverPlayer.getUUID();
	}

	public int getEntityID() {
		return this.serverPlayer.getId();
	}

	public String getProfileName () {
		return this.serverPlayer.gameProfile.name();
	}

}
