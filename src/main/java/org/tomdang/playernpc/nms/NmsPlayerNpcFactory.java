package org.tomdang.playernpc.nms;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.tomdang.playernpc.runtime.PlayerNPC;

import java.util.UUID;

public class NmsPlayerNpcFactory {

	public PlayerNPC create(Location location, String profileName) {
		return create(location, profileName, UUID.randomUUID());
	}

	public PlayerNPC create(Location location, String profileName, UUID profileID) {
		if (location == null) throw new IllegalArgumentException("Location cannot be null");
		if (profileName == null) throw new IllegalArgumentException("profileName cannot be null");
		if (profileName.isBlank()) throw new IllegalArgumentException("profileName cannot be blank");
		if (profileName.length() > 16) throw new IllegalArgumentException("profileName cannot exceed 16 characters");
		if (profileID == null) throw new IllegalArgumentException("ProfileID shouldn't be null");

		World world = location.getWorld();
		if (world == null) throw new IllegalStateException("World cannot be null");

		CraftServer craftServer = (CraftServer) Bukkit.getServer();
		MinecraftServer minecraftServer = craftServer.getServer();

		CraftWorld craftWorld = (CraftWorld) world;
		ServerLevel serverLevel = craftWorld.getHandle();

		ClientInformation clientInformation = ClientInformation.createDefault();

		GameProfile profile = new GameProfile(profileID, profileName);

		ServerPlayer serverPlayer = new ServerPlayer(minecraftServer, serverLevel, profile, clientInformation);
		serverPlayer.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		return new PlayerNPC(serverPlayer);
	}

}
