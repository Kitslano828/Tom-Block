package org.tomdang.islandedge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

/** Safeguards for the imported southwest-island world, not the hub or raid worlds. */
public final class IslandEdgePlugin extends JavaPlugin implements Listener {
    private static final String WORLD_NAME = "world";
    private static final int SPAWN_X = -920;
    private static final int SPAWN_Y = 66;
    private static final int SPAWN_Z = 376;
    private final Set<Long> playableChunks = new HashSet<>();
    private OceanTransitionProfile oceanTransitionProfile;

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        if (!WORLD_NAME.equals(worldName)) return null;
        if (oceanTransitionProfile == null) {
            try (var stream = getResource("ocean-seam.csv")) {
                if (stream == null) throw new IOException("ocean-seam.csv is missing");
                oceanTransitionProfile = OceanTransitionProfile.load(stream);
            } catch (IOException exception) {
                throw new IllegalStateException("Cannot load southwest-island ocean seam", exception);
            }
        }
        return new IslandOceanGenerator(oceanTransitionProfile);
    }

    @Override
    public void onEnable() {
        try (var stream = getResource("playable-chunks.csv")) {
            if (stream == null) throw new IOException("playable-chunks.csv is missing");
            try (var lines = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = lines.readLine()) != null) {
                    String[] coordinates = line.split(",", -1);
                    if (coordinates.length != 2) throw new IOException("Invalid chunk: " + line);
                    playableChunks.add(key(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1])));
                }
            }
        } catch (IOException | NumberFormatException exception) {
            getLogger().severe("Island boundary could not load: " + exception.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (playableChunks.isEmpty()) {
            getLogger().severe("Island boundary is empty; disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Loaded " + playableChunks.size() + " southwest-island playable chunks");
    }

    @EventHandler
    public void onFluidFlow(BlockFromToEvent event) {
        if (WORLD_NAME.equals(event.getBlock().getWorld().getName())
                && event.getBlock().getType() == Material.WATER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        if (to == null || !isIsland(to.getWorld()) || contains(to)) return;
        Location from = event.getFrom();
        event.setTo(contains(from) ? from : spawn(to.getWorld()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Location location = event.getPlayer().getLocation();
        if (isIsland(location.getWorld()) && !contains(location)) {
            event.getPlayer().teleport(spawn(location.getWorld()));
        }
    }

    private static boolean isIsland(World world) {
        return world != null && WORLD_NAME.equals(world.getName());
    }

    private boolean contains(Location location) {
        return isIsland(location.getWorld())
                && playableChunks.contains(key(location.getBlockX() >> 4, location.getBlockZ() >> 4));
    }

    private static Location spawn(World world) {
        return new Location(world, SPAWN_X + 0.5, SPAWN_Y, SPAWN_Z + 0.5);
    }

    private static long key(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xffffffffL);
    }
}
