package org.tomdang.worldmap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Presents the exported map through vanilla filled-map items. */
public final class WorldMapItemService {
	private final WorldMapImage image;
	private final String worldName;
	private final int localDiameterBlocks;

	public WorldMapItemService(JavaPlugin plugin) {
		if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
		File sourceFile = new File(plugin.getDataFolder(), "maps/world.png");
		if (!sourceFile.isFile()) {
			throw new IllegalStateException("Missing external map image at " + sourceFile
					+ "; deploy src/main/resources/maps/world.png with the plugin");
		}
		BufferedImage source;
		try {
			source = ImageIO.read(sourceFile);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read maps/world.png", exception);
		}
		if (source == null) throw new IllegalStateException("maps/world.png is not a readable image");
		File settingsFile = new File(plugin.getDataFolder(), "world-map.yml");
		if (Files.notExists(settingsFile.toPath())) plugin.saveResource("world-map.yml", false);
		YamlConfiguration settings = YamlConfiguration.loadConfiguration(settingsFile);
		worldName = settings.getString("world", "world");
		localDiameterBlocks = settings.getInt("local-diameter-blocks", 256);
		if (localDiameterBlocks <= 0) throw new IllegalStateException("local-diameter-blocks must be positive");
		String path = "bounds.";
		if (settings.isInt(path + "minimum-x") && settings.isInt(path + "minimum-z")
				&& settings.isInt(path + "maximum-x") && settings.isInt(path + "maximum-z")) {
			WorldMapBounds bounds = new WorldMapBounds(settings.getInt(path + "minimum-x"), settings.getInt(path + "minimum-z"),
					settings.getInt(path + "maximum-x"), settings.getInt(path + "maximum-z"));
			image = new WorldMapImage(source, bounds);
		} else {
			image = new WorldMapImage(source, null);
		}
		plugin.getLogger().info("Loaded world map " + source.getWidth() + "x" + source.getHeight()
				+ (isCalibrated() ? " (coordinate bounds calibrated)" : " (coordinate bounds not calibrated)"));
	}

	public boolean isCalibrated() {
		return image.isCalibrated();
	}

	public ItemStack create(Player player, boolean local) {
		if (player == null) throw new IllegalArgumentException("player cannot be null");
		if (local && !isCalibrated()) throw new IllegalStateException("Set image bounds in world-map.yml first");
		World world = Bukkit.getWorld(worldName);
		if (world == null) throw new IllegalStateException("Map world is not loaded: " + worldName);
		MapView view = Bukkit.createMap(world);
		for (MapRenderer renderer : view.getRenderers()) view.removeRenderer(renderer);
		view.setTrackingPosition(false);
		view.addRenderer(new ImageRenderer(image, worldName, local, localDiameterBlocks));
		ItemStack item = new ItemStack(Material.FILLED_MAP);
		MapMeta meta = (MapMeta) item.getItemMeta();
		meta.setMapView(view);
		meta.setDisplayName(local ? "TomBlock Local Map" : "TomBlock World Map");
		item.setItemMeta(meta);
		return item;
	}

	private static final class ImageRenderer extends MapRenderer {
		private final WorldMapImage image;
		private final String worldName;
		private final boolean local;
		private final int diameter;
		private final Map<UUID, ViewerState> viewerStates = new HashMap<>();

		private ImageRenderer(WorldMapImage image, String worldName,
				boolean local, int diameter) {
			super(true);
			this.image = image;
			this.worldName = worldName;
			this.local = local;
			this.diameter = diameter;
		}

		@Override
		public void render(MapView view, MapCanvas canvas, Player player) {
			boolean sameWorld = player.getWorld().getName().equals(worldName);
			int x = player.getLocation().getBlockX();
			int z = player.getLocation().getBlockZ();
			ViewerState state = viewerStates.computeIfAbsent(player.getUniqueId(), ignored -> {
				MapCursor cursor = new MapCursor((byte) 0, (byte) 0, (byte) 0,
						MapCursor.Type.RED_MARKER, false);
				canvas.getCursors().addCursor(cursor);
				return new ViewerState(cursor);
			});
			if (local && sameWorld) {
				if (!state.drawn || x != state.lastX || z != state.lastZ || !state.lastWorld.equals(worldName)) {
					canvas.drawImage(0, 0, image.local(x, z, diameter));
					state.lastX = x;
					state.lastZ = z;
					state.lastWorld = worldName;
					state.drawn = true;
				}
				state.cursor.setX((byte) 0);
				state.cursor.setY((byte) 0);
				state.cursor.setVisible(true);
			} else {
				if (!state.drawn || (local && !"full".equals(state.lastWorld))) {
					canvas.drawImage(0, 0, image.full());
					state.drawn = true;
					state.lastWorld = "full";
				}
				WorldMapImage.Pixel pixel = sameWorld ? image.fullMarker(x, z) : null;
				state.cursor.setVisible(pixel != null);
				if (pixel != null) {
					state.cursor.setX(cursorCoordinate(pixel.x()));
					state.cursor.setY(cursorCoordinate(pixel.y()));
				}
			}
			state.cursor.setDirection((byte) Math.floorMod(Math.round(player.getLocation().getYaw() / 22.5f), 16));
		}

		private static byte cursorCoordinate(int pixel) {
			return (byte) Math.clamp(pixel * 2 - 128, -128, 127);
		}

		private static final class ViewerState {
			private final MapCursor cursor;
			private boolean drawn;
			private int lastX;
			private int lastZ;
			private String lastWorld = "";

			private ViewerState(MapCursor cursor) {
				this.cursor = cursor;
			}
		}
	}
}
