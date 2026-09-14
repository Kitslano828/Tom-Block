package org.tomdang.actorframework.nameplate.nms;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.tomdang.actorframework.nameplate.nms.runtime.NmsActorNameplateLine;

import java.util.Set;

public class NmsActorNameplateViewer {

	public void show(Player viewer, NmsActorNameplateLine runtimeLine) {
		if (viewer == null) throw new IllegalArgumentException("Viewer cannot be null");
		if (runtimeLine == null) throw new IllegalArgumentException("runtimeLine cannot be null");

		ServerGamePacketListenerImpl connection = resolveConnection(viewer);
		Display.TextDisplay textDisplay = runtimeLine.getTextDisplay();

		ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(
				runtimeLine.getEntityID(),
				runtimeLine.getPresentationUUID(),
				textDisplay.getX(),
				textDisplay.getY(),
				textDisplay.getZ(),
				textDisplay.getXRot(),
				textDisplay.getYRot(),
				textDisplay.getType(),
				0,                  // Entity data value
				Vec3.ZERO,          // Delta movement (velocity)
				textDisplay.getYHeadRot() // Head Y rotation
		);

		// Send spawn packet first
		connection.send(addEntityPacket);

		connection.send(new ClientboundSetEntityDataPacket(
				runtimeLine.getEntityID(),
				textDisplay.getEntityData().packAll()
		));
	}

	public void hide(Player viewer, NmsActorNameplateLine runtimeLine) {
		if (viewer == null) throw new IllegalArgumentException("Viewer cannot be null");
		if (runtimeLine == null) throw new IllegalArgumentException("runtimeLine cannot be null");

		ServerGamePacketListenerImpl connection = resolveConnection(viewer);

		ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(runtimeLine.getEntityID());

		connection.send(removeEntitiesPacket);
	}

	public void teleport(Player viewer, NmsActorNameplateLine runtimeLine, Location finalLocation) {
		if (viewer == null) throw new IllegalArgumentException("Viewer cannot be null");
		if (runtimeLine == null) throw new IllegalArgumentException("runtimeLine cannot be null");
		if (finalLocation == null || finalLocation.getWorld() == null) throw new IllegalArgumentException("finalLocation and its world cannot be null");

		double x = finalLocation.getX();
		double originalY = finalLocation.getY();
		double z = finalLocation.getZ();

		if (!Double.isFinite(x) || !Double.isFinite(originalY) || !Double.isFinite(z)) {
			throw new IllegalArgumentException("Actor location coordinates (X, Y, Z) must be finite");
		}

		Display.TextDisplay textDisplay = runtimeLine.getTextDisplay();

		textDisplay.setPos(finalLocation.getX(), finalLocation.getY(), finalLocation.getZ());

		ServerGamePacketListenerImpl connection = resolveConnection(viewer);

		ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(
				runtimeLine.getEntityID(),
				PositionMoveRotation.of(textDisplay),
				Set.of(),
				textDisplay.onGround()
		);

		connection.send(teleportEntityPacket);
	}

	private ServerGamePacketListenerImpl resolveConnection(Player player) {
		ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();

		if (serverPlayer.connection == null) {
			throw new IllegalStateException("Player connection is missing for viewer: " + player.getName());
		}

		return serverPlayer.connection;
	}

}
