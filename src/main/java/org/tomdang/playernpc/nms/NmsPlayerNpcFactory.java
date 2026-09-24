package org.tomdang.playernpc.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.tomdang.playernpc.runtime.PlayerNPC;
import org.tomdang.actorframework.skin.ActorSkin;

import java.util.UUID;

public class NmsPlayerNpcFactory {
	private static final byte ALL_SKIN_LAYERS = 0x7F;

	public PlayerNPC create(Location location, String profileName) {
		return create(location, profileName, UUID.randomUUID(), null);
	}

	public PlayerNPC create(Location location, String profileName, ActorSkin skin) {
		return create(location, profileName, UUID.randomUUID(), skin);
	}

	public PlayerNPC create(Location location, String profileName, UUID profileID) {
		return create(location, profileName, profileID, null);
	}

	public PlayerNPC create(Location location, String profileName, UUID profileID, ActorSkin skin) {
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

		GameProfile profile = profileWithSkin(profileID, profileName, skin);

		ServerPlayer serverPlayer = new ServerPlayer(minecraftServer, serverLevel, profile, clientInformation);
		serverPlayer.getEntityData().set(Player.DATA_PLAYER_MODE_CUSTOMISATION, ALL_SKIN_LAYERS);
		serverPlayer.snapTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
		return new PlayerNPC(serverPlayer);
	}

	static GameProfile profileWithSkin(UUID id, String name, ActorSkin skin) {
		if (skin == null) return new GameProfile(id, name);
		Property property = skin.signature() == null
				? new Property("textures", skin.value())
				: new Property("textures", skin.value(), skin.signature());
		PropertyMap properties = new PropertyMap(ImmutableMultimap.of("textures", property));
		return new GameProfile(id, name, properties);
	}

}
