package org.tomdang.bootstrap;

import java.nio.file.Path;
import org.tomdang.TomBlock;
import org.tomdang.island.runtime.IslandContextService;
import org.tomdang.worldtime.FileWorldClockStateStore;
import org.tomdang.worldtime.WorldCalendarConfigurationLoader;
import org.tomdang.worldtime.WorldCalendarService;
import org.tomdang.worldtime.bukkit.WorldCalendarCommand;
import org.tomdang.worldtime.bukkit.WorldDaylightSynchronizer;
import org.tomdang.worldtime.event.WorldTimeEventBus;
import org.tomdang.hud.calendar.CalendarHudService;
import org.tomdang.hud.presentation.ProductionHudService;

public final class WorldCalendarBootstrap implements AutoCloseable {
    private final WorldCalendarService calendar;
    private final WorldTimeEventBus events = new WorldTimeEventBus();
    private final WorldDaylightSynchronizer synchronizer;
    private final CalendarHudService hud;

    public WorldCalendarBootstrap(TomBlock plugin, IslandContextService islands, ProductionHudService productionHud) {
        var settings = new WorldCalendarConfigurationLoader().load(plugin.getResource("world-calendar.yml"));
        Path state = plugin.getDataFolder().toPath().resolve("world-calendar-state.properties");
        calendar = new WorldCalendarService(settings, new FileWorldClockStateStore(state));
        synchronizer = new WorldDaylightSynchronizer(plugin, calendar, islands, events);
        hud = new CalendarHudService(plugin, calendar, events, productionHud);
        var command = java.util.Objects.requireNonNull(plugin.getCommand("calendar"), "Missing calendar command");
        var handler = new WorldCalendarCommand(calendar, synchronizer);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }

    public WorldCalendarService calendar() { return calendar; }
    public WorldTimeEventBus events() { return events; }
    @Override public void close() { hud.close(); synchronizer.close(); }
}
