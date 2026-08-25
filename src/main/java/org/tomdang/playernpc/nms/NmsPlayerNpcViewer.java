package org.tomdang.playernpc.nms;

import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.level.GameType;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.tomdang.playernpc.runtime.PlayerNPC;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
		sendHeadRotationPacket(gamePacketListener, serverPlayer);
	}

	public void hide(Player player, PlayerNPC playerNPC) {
		if (playerNPC == null) throw new IllegalArgumentException("playerNPC cannot be null");
		ServerGamePacketListenerImpl gamePacketListener = getConnection(player);

		ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(playerNPC.getEntityID());
		gamePacketListener.send(removeEntitiesPacket);
		ClientboundPlayerInfoRemovePacket clientboundPlayerInfoRemovePacket = new ClientboundPlayerInfoRemovePacket(List.of(playerNPC.getProfileUUID()));
		gamePacketListener.send(clientboundPlayerInfoRemovePacket);
	}

	public void teleport(Player viewer, PlayerNPC playerNPC) {
		if (viewer == null) throw new IllegalArgumentException("Player cannot be null");
		if (playerNPC == null) throw new IllegalArgumentException("playerNPC cannot be null");

		ServerPlayer serverPlayer = playerNPC.getServerPlayer();
		ServerGamePacketListenerImpl gamePacketListener = getConnection(viewer);

		ClientboundTeleportEntityPacket clientboundTeleportEntityPacket = new ClientboundTeleportEntityPacket(
				playerNPC.getEntityID(),
				PositionMoveRotation.of(serverPlayer),
				Set.of(),
				serverPlayer.onGround
				);

		gamePacketListener.send(clientboundTeleportEntityPacket);
		sendHeadRotationPacket(gamePacketListener, serverPlayer);
	}

	private void sendHeadRotationPacket(ServerGamePacketListenerImpl gamePacketListener, ServerPlayer serverPlayer) {
		byte encodedYaw = (byte) Mth.floor(serverPlayer.getYHeadRot() * 256.0F / 360.0F);
		gamePacketListener.send(new ClientboundRotateHeadPacket(serverPlayer, encodedYaw));
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
