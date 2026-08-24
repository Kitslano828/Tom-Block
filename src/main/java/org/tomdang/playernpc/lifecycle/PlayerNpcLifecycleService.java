package org.tomdang.playernpc.lifecycle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.tomdang.playernpc.nms.NmsPlayerNpcFactory;
import org.tomdang.playernpc.nms.NmsPlayerNpcViewer;
import org.tomdang.playernpc.runtime.PlayerNPC;
import org.tomdang.playernpc.runtime.PlayerNpcRegistry;
import org.tomdang.playernpc.runtime.PlayerNpcVisibilityRegistry;

import java.util.Set;
import java.util.UUID;

public class PlayerNpcLifecycleService {

	private final PlayerNpcRegistry playerNpcRegistry;
	private final PlayerNpcVisibilityRegistry playerNpcVisibilityRegistry;
	private final NmsPlayerNpcViewer nmsPlayerNpcViewer;
	private final NmsPlayerNpcFactory nmsPlayerNpcFactory;

	public PlayerNpcLifecycleService(PlayerNpcRegistry playerNpcRegistry, PlayerNpcVisibilityRegistry playerNpcVisibilityRegistry, NmsPlayerNpcViewer nmsPlayerNpcViewer, NmsPlayerNpcFactory nmsPlayerNpcFactory ) {
		if (playerNpcRegistry == null) throw new IllegalArgumentException("Player NPC registry cannot be null");
		if (playerNpcVisibilityRegistry == null) throw new IllegalArgumentException("Player NPC visibility registry cannot be null");
		if (nmsPlayerNpcViewer == null) throw new IllegalArgumentException("NMS player NPC viewer cannot be null");
		if (nmsPlayerNpcFactory == null) throw new IllegalArgumentException("NMS player NPC factory cannot be null");

		this.playerNpcRegistry = playerNpcRegistry;
		this.playerNpcVisibilityRegistry = playerNpcVisibilityRegistry;
		this.nmsPlayerNpcViewer = nmsPlayerNpcViewer;
		this.nmsPlayerNpcFactory = nmsPlayerNpcFactory;
	}

	public boolean showToViewer(Player viewer, UUID npcUUID) {
		if (viewer == null) throw new IllegalArgumentException("Viewer cannot be null");
		if (npcUUID == null) throw new IllegalArgumentException("NPC UUID cannot be null");

		PlayerNPC playerNPC = playerNpcRegistry.get(npcUUID);
		if (playerNPC == null) throw new IllegalStateException("NPC does not exist");

		UUID viewerUUID = viewer.getUniqueId();
		if (playerNpcVisibilityRegistry.isVisibleTo(npcUUID, viewerUUID)) return false;

		nmsPlayerNpcViewer.show(viewer, playerNPC);
		playerNpcVisibilityRegistry.addViewer(npcUUID, viewerUUID);
		return true;
	}

	public boolean hideFromViewer(Player viewer, UUID npcUUID) {
		if (viewer == null) throw new IllegalArgumentException("Viewer cannot be null");
		if (npcUUID == null) throw new IllegalArgumentException("NPC UUID cannot be null");

		PlayerNPC playerNPC = playerNpcRegistry.get(npcUUID);
		if (playerNPC == null) throw new IllegalStateException("NPC does not exist");

		UUID viewerUUID = viewer.getUniqueId();
		if (!playerNpcVisibilityRegistry.isVisibleTo(npcUUID, viewerUUID)) return false;

		nmsPlayerNpcViewer.hide(viewer, playerNPC);
		playerNpcVisibilityRegistry.removeViewer(npcUUID, viewerUUID);
		return true;
	}

	public PlayerNPC createNpc(Location location, String profileName) {
		PlayerNPC playerNPC = nmsPlayerNpcFactory.create(location, profileName);
		playerNpcRegistry.register(playerNPC);
		return playerNPC;
	}

	public PlayerNPC createNpc(Location location, String profileName, UUID profileID) {
		PlayerNPC playerNPC = nmsPlayerNpcFactory.create(location, profileName, profileID);
		playerNpcRegistry.register(playerNPC);
		return playerNPC;
	}

	public PlayerNPC removeNpc(UUID npcUUID) {
		if (npcUUID == null) throw new IllegalArgumentException("NPC UUID cannot be null");
		PlayerNPC playerNPC = playerNpcRegistry.get(npcUUID);
		if (playerNPC == null) return null;
		Set<UUID> npcViewers = playerNpcVisibilityRegistry.getViewers(npcUUID);
		for (UUID playerUUID : npcViewers) {
			Player viewer = Bukkit.getPlayer(playerUUID);
			if (viewer == null) continue;
			if (!viewer.isOnline()) continue;
			nmsPlayerNpcViewer.hide(viewer, playerNPC);
		}
		playerNpcVisibilityRegistry.clearNpc(npcUUID);
		return playerNpcRegistry.remove(npcUUID); 
	}

	public void moveNpc(UUID profileUUID, Location location) {
		if (profileUUID == null) throw new IllegalArgumentException("ProfileUUID cannot be null");
		if (location == null) throw new IllegalArgumentException("location cannot be null");
		if (location.getWorld() == null) throw new IllegalArgumentException("world cannot be null");

		PlayerNPC playerNPC = playerNpcRegistry.get(profileUUID);
		if (playerNPC == null) throw new IllegalStateException("NPC does not exist");

		ServerPlayer serverPlayer = playerNPC.getServerPlayer();
		ServerLevel serverLevel = serverPlayer.level();
		CraftWorld craftWorld = serverLevel.getWorld();

		if (!location.getWorld().equals(craftWorld)) throw new IllegalStateException("Cross world travelling has yet to be implementated");

		serverPlayer.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

		for (UUID playerUUID : playerNpcVisibilityRegistry.getViewers(profileUUID)) {
			Player player = Bukkit.getPlayer(playerUUID);
			if (player == null || !player.isOnline()) continue;
			nmsPlayerNpcViewer.teleport(player, playerNPC);
		}

	}

	public void clearViewerVisibility(UUID viewerUUID) {
		if (viewerUUID == null) throw new IllegalArgumentException("Viewer UUID cannot be null");
		playerNpcVisibilityRegistry.clearViewer(viewerUUID);
	}

}
