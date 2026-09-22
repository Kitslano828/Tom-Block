package org.tomdang.worldmap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import net.kyori.adventure.text.Component;
import io.papermc.paper.scoreboard.numbers.NumberFormat;
import org.tomdang.actorframework.presentation.bukkit.BukkitActorCollisionService;
import org.tomdang.region.bukkit.BukkitBlockPositionAdapter;
import org.tomdang.region.position.BlockPosition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Opt-in sidebar experiment with mirrored actor teams. */
public final class MapTestService {
	private static final int GRID_SIZE = 13;
	private static final String IMAGE_ENTRY = "tomblock_map_image";
	private final VillageMapGrid grid;
	private final VillageMapHudLayout imageLayout = new VillageMapHudLayout();
	private final VillageFollowingMapHudLayout followingLayout = new VillageFollowingMapHudLayout();
	private final VillageCenteredMapHudLayout centeredLayout = new VillageCenteredMapHudLayout();
	private final MapHudProbeLayout probeLayout = new MapHudProbeLayout();
	private final BukkitActorCollisionService collisionService;
	private final BukkitBlockPositionAdapter positions = new BukkitBlockPositionAdapter();
	private final Map<UUID, View> views = new HashMap<>();

	public MapTestService(Plugin plugin, VillageMapGrid grid, BukkitActorCollisionService collisionService) {
		if (plugin == null || grid == null || collisionService == null)
			throw new IllegalArgumentException("plugin, grid and collisionService are required");
		this.grid = grid;
		this.collisionService = collisionService;
		Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 10L, 10L);
	}

	public boolean toggle(Player player) {
		return toggle(player, Mode.CONCEPT);
	}

	public boolean toggleImage(Player player) {
		return toggle(player, Mode.IMAGE);
	}

	public boolean toggleStaticProbe(Player player) {
		return toggle(player, Mode.STATIC);
	}

	public boolean toggleMarkerProbe(Player player) {
		return toggle(player, Mode.MARKER);
	}

	public boolean toggleCenteredProbe(Player player) {
		return toggle(player, Mode.CENTERED);
	}

	private boolean toggle(Player player, Mode mode) {
		View existing = views.get(player.getUniqueId());
		if (existing != null) {
			hide(player);
			if (existing.mode() == mode) return false;
		}
		Scoreboard original = player.getScoreboard();
		Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective = board.registerNewObjective("tb_map", "dummy",
				mode == Mode.CONCEPT ? "Village Concept" : " ");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.numberFormat(NumberFormat.blank());
		if (mode != Mode.CONCEPT) {
			Score imageScore = objective.getScore(IMAGE_ENTRY);
			imageScore.setScore(1);
			imageScore.customName(Component.empty());
		} else {
			int lines = GRID_SIZE + 2;
			for (int i = 0; i < lines; i++) {
				String entry = ChatColor.values()[i].toString();
				Team team = board.registerNewTeam("line_" + i);
				team.addEntry(entry);
				objective.getScore(entry).setScore(lines - i);
			}
		}
		collisionService.attachScoreboard(board);
		views.put(player.getUniqueId(), new View(original, board, mode));
		player.setScoreboard(board);
		update(player);
		return true;
	}

	public void hide(Player player) {
		View view = views.remove(player.getUniqueId());
		if (view != null) {
			if (player.getScoreboard() == view.board()) player.setScoreboard(view.original());
			collisionService.detachScoreboard(view.board());
		}
	}

	public void stop() {
		for (UUID id : views.keySet().toArray(UUID[]::new)) {
			Player player = Bukkit.getPlayer(id);
			if (player != null) hide(player);
			else {
				View view = views.remove(id);
				collisionService.detachScoreboard(view.board());
			}
		}
	}

	private void updateAll() {
		for (UUID id : views.keySet().toArray(UUID[]::new)) {
			Player player = Bukkit.getPlayer(id);
			if (player == null) {
				View view = views.remove(id);
				collisionService.detachScoreboard(view.board());
			}
			else update(player);
		}
	}

	private void update(Player player) {
		View view = views.get(player.getUniqueId());
		if (view == null || player.getScoreboard() != view.board()) return;
		BlockPosition position = positions.fromLocation(player.getLocation());
		if (view.mode() == Mode.STATIC) {
			setImageLine(view.board(), probeLayout.staticMap());
			return;
		}
		if (view.mode() == Mode.MARKER) {
			setImageLine(view.board(), probeLayout.mapWithMarker());
			return;
		}
		if (view.mode() == Mode.IMAGE) {
			Component image = followingLayout.select(position) == null
					? imageLayout.compose(position, player.getLocation().getYaw())
					: followingLayout.compose(position, player.getLocation().getYaw());
			setImageLine(view.board(), image);
			return;
		}
		if (view.mode() == Mode.CENTERED) {
			setImageLine(view.board(), centeredLayout.compose(position, player.getLocation().getYaw()));
			return;
		}
		for (int row = 0; row < GRID_SIZE; row++) {
			String raw = grid.row(position, row, GRID_SIZE);
			StringBuilder colored = new StringBuilder();
			for (char cell : raw.toCharArray()) {
				colored.append(switch (cell) {
					case 'P' -> ChatColor.GOLD;
					case 'T' -> ChatColor.DARK_GREEN;
					case 'H' -> ChatColor.RED;
					case 'R' -> ChatColor.YELLOW;
					case 'W' -> ChatColor.AQUA;
					case '#' -> ChatColor.GREEN;
					default -> ChatColor.DARK_GRAY;
				}).append(cell == 'P' ? '◆' : '■');
			}
			setLine(view.board(), row, colored.toString());
		}
		setLine(view.board(), GRID_SIZE, " ");
		setLine(view.board(), GRID_SIZE + 1, ChatColor.GRAY + "X " + position.x() + "  Z " + position.z());
	}

	private void setLine(Scoreboard board, int row, String text) {
		Team team = board.getTeam("line_" + row);
		if (!team.getPrefix().equals(text)) team.setPrefix(text);
	}

	private void setImageLine(Scoreboard board, Component content) {
		Score score = board.getObjective("tb_map").getScore(IMAGE_ENTRY);
		if (!content.equals(score.customName())) score.customName(content);
	}

	private enum Mode { CONCEPT, STATIC, MARKER, IMAGE, CENTERED }
	private record View(Scoreboard original, Scoreboard board, Mode mode) {}
}
