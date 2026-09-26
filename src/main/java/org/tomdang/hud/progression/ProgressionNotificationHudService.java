package org.tomdang.hud.progression;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.tomdang.hud.composition.HudElementId;
import org.tomdang.hud.composition.HudRegion;
import org.tomdang.hud.presentation.HudElementSpec;
import org.tomdang.hud.presentation.ProductionHudService;
import org.tomdang.player.skill.SkillAwardResult;
import org.tomdang.player.skill.SkillProgressNotificationSink;
import org.tomdang.player.stats.PlayerStatValueFormatter;

import java.util.*;

/** Queued left-side feed for durable progression feedback. */
public final class ProgressionNotificationHudService implements SkillProgressNotificationSink, AutoCloseable {
    private static final HudElementId ID = HudElementId.of("tomblock", "progression-feed");
    private static final HudElementSpec SPEC = HudElementSpec.persistent(ID, HudRegion.PROGRESSION, 100);
    private static final int MAX_LINES = 4;
    private static final long DURATION_TICKS = 80;
    private final Plugin plugin;
    private final ProductionHudService hud;
    private final Map<UUID, Deque<Entry>> feeds = new HashMap<>();
    private long sequence;

    public ProgressionNotificationHudService(Plugin plugin, ProductionHudService hud) {
        this.plugin = Objects.requireNonNull(plugin); this.hud = Objects.requireNonNull(hud);
    }

    @Override
    public void show(Player player, SkillAwardResult result) {
        if (player == null || result == null) throw new IllegalArgumentException("Player and skill award are required");
        for (ProgressionNotificationLine line : linesFor(result)) add(player.getUniqueId(), line.text(), line.tone());
    }

    static List<ProgressionNotificationLine> linesFor(SkillAwardResult result) {
        if (result.awardedXp() <= 0 && !result.leveledUp()) return List.of();
        List<ProgressionNotificationLine> lines = new ArrayList<>();
        lines.add(new ProgressionNotificationLine("+" + result.awardedXp() + " " + result.skill().name() + " EXP",
                ProgressionNotificationTone.XP));
        if (result.leveledUp()) {
            lines.add(new ProgressionNotificationLine(result.skill().name() + " LEVEL " + result.after().level(),
                    ProgressionNotificationTone.LEVEL));
            String stat = result.skill().rewardStat().getDisplayName().toUpperCase(Locale.ROOT);
            double gained = result.skill().rewardAt(result.after().level())
                    - result.skill().rewardAt(result.before().level());
            String amount = PlayerStatValueFormatter.format(gained);
            lines.add(new ProgressionNotificationLine("+" + amount + " " + stat, ProgressionNotificationTone.REWARD));
        }
        return List.copyOf(lines);
    }

    private void add(UUID playerId, String text, ProgressionNotificationTone tone) {
        long entryId = ++sequence;
        Deque<Entry> feed = feeds.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        feed.addLast(new Entry(entryId, new ProgressionNotificationLine(text, tone)));
        while (feed.size() > MAX_LINES) feed.removeFirst();
        publish(playerId);
        Bukkit.getScheduler().runTaskLater(plugin, () -> expire(playerId, entryId), DURATION_TICKS);
    }

    private void expire(UUID playerId, long entryId) {
        Deque<Entry> feed = feeds.get(playerId);
        if (feed == null) return;
        feed.removeIf(entry -> entry.id() == entryId);
        if (feed.isEmpty()) {
            feeds.remove(playerId);
            hud.hide(playerId, SPEC, Bukkit.getCurrentTick());
        } else publish(playerId);
    }

    private void publish(UUID playerId) {
        Deque<Entry> feed = feeds.get(playerId);
        if (feed == null || feed.isEmpty()) return;
        hud.show(playerId, SPEC, new ProgressionNotificationModel(feed.stream().map(Entry::line).toList()), Bukkit.getCurrentTick());
    }

    @Override
    public void close() {
        for (UUID playerId : List.copyOf(feeds.keySet())) hud.hide(playerId, SPEC, Bukkit.getCurrentTick());
        feeds.clear();
    }

    private record Entry(long id, ProgressionNotificationLine line) { }
}
