package org.tomdang.playernpc.nms;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.tomdang.playernpc.runtime.PlayerNPC;

import java.util.EnumSet;
import java.util.List;

public class NmsPlayerNpcViewer {

	public void show(Player player, PlayerNPC playerNPC) {
		if (playerNPC == null) throw new IllegalArgumentException("playerNPC cannot be null");
		ServerPlayer serverPlayer = playerNPC.getServerPlayer();

		ServerGamePacketListenerImpl gamePacketListener = getConnection(player);

		ClientboundPlayerInfoUpdatePacket.Entry playerInfoEntry = new ClientboundPlayerInfoUpdatePacket.Entry(serverPlayer.getUUID(), serverPlayer.getGameProfile(), false, 0, GameType.SURVIVAL, null, true, 0, null);
		ClientboundPlayerInfoUpdatePacket playerInfoPacket =
				new ClientboundPlayerInfoUpdatePacket(
						EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER),
						playerInfoEntry
				);

		gamePacketListener.send(playerInfoPacket);

		ClientboundAddEntityPacket spawnPacket = new ClientboundAddEntityPacket(
				serverPlayer.getId(), serverPlayer.getUUID(), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
				serverPlayer.getXRot(), serverPlayer.getYRot(), serverPlayer.getType(), 0, serverPlayer.getDeltaMovement(), serverPlayer.getYHeadRot());
		gamePacketListener.send(spawnPacket);

	}

	public void hide(Player player, PlayerNPC playerNPC) {
		if (playerNPC == null) throw new IllegalArgumentException("playerNPC cannot be null");
		ServerGamePacketListenerImpl gamePacketListener = getConnection(player);

		ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(playerNPC.getEntityID());
		gamePacketListener.send(removeEntitiesPacket);
		ClientboundPlayerInfoRemovePacket clientboundPlayerInfoRemovePacket = new ClientboundPlayerInfoRemovePacket(List.of(playerNPC.getProfileUUID()));
		gamePacketListener.send(clientboundPlayerInfoRemovePacket);
	}

	private ServerGamePacketListenerImpl getConnection(Player player) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");

		CraftPlayer craftPlayer = (CraftPlayer) player;
		ServerPlayer realPlayer = craftPlayer.getHandle();

		ServerGamePacketListenerImpl gamePacketListener = realPlayer.connection;

		if (gamePacketListener == null) throw new IllegalStateException("A real player must have a connection");

		return gamePacketListener;
	}

}
