package org.tomdang.hud.calendar;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.tomdang.hud.composition.HudElementId;
import org.tomdang.hud.composition.HudRegion;
import org.tomdang.hud.presentation.HudElementSpec;
import org.tomdang.hud.presentation.ProductionHudService;
import org.tomdang.hud.presentation.scheduling.HudUpdatePolicy;
import org.tomdang.worldtime.WorldCalendarService;
import org.tomdang.worldtime.WorldTimeSnapshot;
import org.tomdang.worldtime.event.WorldTimeAdvanced;
import org.tomdang.worldtime.event.WorldTimeEventBus;

/** Event-driven top-left projection of the authoritative global calendar. */
public final class CalendarHudService implements Listener, AutoCloseable {
    public static final HudElementSpec SPEC = HudElementSpec.persistent(
            HudElementId.of("tomblock", "calendar"), HudRegion.CALENDAR, 50);
    private final Plugin plugin;
    private final WorldCalendarService calendar;
    private final ProductionHudService hud;
    private final AutoCloseable subscription;

    public CalendarHudService(Plugin plugin, WorldCalendarService calendar,
            WorldTimeEventBus events, ProductionHudService hud) {
        if (plugin == null || calendar == null || events == null || hud == null)
            throw new IllegalArgumentException("Calendar HUD dependencies are required");
        this.plugin = plugin;
        this.calendar = calendar;
        this.hud = hud;
        subscription = events.subscribe(WorldTimeAdvanced.class, event -> refreshAll(event.current()));
        Bukkit.getPluginManager().registerEvents(this, plugin);
        refreshAll(calendar.snapshot());
    }

    @EventHandler public void join(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, () -> refresh(event.getPlayer(), calendar.snapshot()));
    }

    private void refreshAll(WorldTimeSnapshot time) {
        for (Player player : Bukkit.getOnlinePlayers()) refresh(player, time);
    }

    private void refresh(Player player, WorldTimeSnapshot time) {
        var model = new CalendarHudModel(time.monthName(), time.date().day(), time.season(), time.period(),
                time.hour(), time.minute());
        hud.showIfChanged(player.getUniqueId(), SPEC, model, revision(time), Bukkit.getCurrentTick(), HudUpdatePolicy.ON_CHANGE);
    }

    private static long revision(WorldTimeSnapshot time) {
        long result = time.date().year();
        result = 31 * result + time.date().month();
        result = 31 * result + time.date().day();
        result = 31 * result + time.hour();
        result = 31 * result + time.minute();
        result = 31 * result + time.period().ordinal();
        return 31 * result + time.season().ordinal();
    }

    @Override public void close() {
        HandlerList.unregisterAll(this);
        try { subscription.close(); }
        catch (Exception exception) { throw new IllegalStateException("Could not detach calendar HUD", exception); }
    }
}
