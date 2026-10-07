package org.tomdang.worldtime.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.tomdang.island.runtime.IslandContextService;
import org.tomdang.worldtime.WorldCalendarService;
import org.tomdang.worldtime.WorldTimeSnapshot;
import org.tomdang.worldtime.event.GameDayStarted;
import org.tomdang.worldtime.event.GameMonthStarted;
import org.tomdang.worldtime.event.GameSeasonChanged;
import org.tomdang.worldtime.event.WorldTimeAdvanced;
import org.tomdang.worldtime.event.WorldTimeEventBus;

/** Projects the global calendar onto every loaded managed island world. */
public final class WorldDaylightSynchronizer implements Listener, AutoCloseable {
    private final Plugin plugin;
    private final WorldCalendarService calendar;
    private final IslandContextService islands;
    private final WorldTimeEventBus events;
    private final BukkitTask task;
    private WorldTimeSnapshot previous;

    public WorldDaylightSynchronizer(Plugin plugin, WorldCalendarService calendar,
            IslandContextService islands, WorldTimeEventBus events) {
        if (plugin == null || calendar == null || islands == null || events == null)
            throw new IllegalArgumentException("World time synchronization dependencies are required");
        this.calendar = calendar;
        this.plugin = plugin;
        this.islands = islands;
        this.events = events;
        previous = calendar.snapshot();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getWorlds().forEach(this::synchronizeIfManaged);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::synchronizeAll, 1L,
                calendar.settings().synchronizationTicks());
    }

    @EventHandler public void loaded(WorldLoadEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> synchronizeIfManaged(event.getWorld()));
    }

    public void synchronizeAll() {
        WorldTimeSnapshot current = calendar.snapshot();
        for (World world : Bukkit.getWorlds()) synchronizeIfManaged(world, current);
        publishTransitions(previous, current);
        previous = current;
    }

    public void synchronizeIfManaged(World world) { synchronizeIfManaged(world, calendar.snapshot()); }

    private void synchronizeIfManaged(World world, WorldTimeSnapshot snapshot) {
        if (islands.runtime(world.getName()).isEmpty()) return;
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 101);
        if (Math.abs(world.getTime() - snapshot.minecraftTime()) > 1) world.setTime(snapshot.minecraftTime());
    }

    private void publishTransitions(WorldTimeSnapshot old, WorldTimeSnapshot current) {
        if (old.equals(current)) return;
        events.publish(new WorldTimeAdvanced(old, current));
        if (!old.date().equals(current.date())) events.publish(new GameDayStarted(current));
        if (old.date().month() != current.date().month() || old.date().year() != current.date().year())
            events.publish(new GameMonthStarted(current));
        if (old.season() != current.season()) events.publish(new GameSeasonChanged(old.season(), current));
    }

    @Override public void close() {
        task.cancel();
        calendar.save();
        HandlerList.unregisterAll(this);
    }
}
