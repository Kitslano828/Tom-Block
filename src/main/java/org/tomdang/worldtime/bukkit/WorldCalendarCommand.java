package org.tomdang.worldtime.bukkit;

import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.worldtime.GameDate;
import org.tomdang.worldtime.WorldCalendarService;

public final class WorldCalendarCommand implements CommandExecutor, TabCompleter {
    private final WorldCalendarService calendar;
    private final WorldDaylightSynchronizer synchronizer;
    public WorldCalendarCommand(WorldCalendarService calendar, WorldDaylightSynchronizer synchronizer) {
        this.calendar = calendar;
        this.synchronizer = synchronizer;
    }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("show")) { show(sender); return true; }
        if (!sender.hasPermission("tomblock.admin.calendar")) {
            sender.sendMessage(Component.text("You do not have permission to alter global time.", NamedTextColor.RED));
            return true;
        }
        try {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "pause" -> calendar.pause();
                case "resume" -> calendar.resume();
                case "speed" -> {
                    if (args.length != 2) throw new IllegalArgumentException("Usage: /calendar speed <multiplier>");
                    calendar.setSpeed(Double.parseDouble(args[1]));
                }
                case "set" -> {
                    if (args.length != 6) throw new IllegalArgumentException("Usage: /calendar set <year> <month> <day> <hour> <minute>");
                    calendar.set(new GameDate(Long.parseLong(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3])),
                            Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                }
                default -> throw new IllegalArgumentException("Usage: /calendar [show|pause|resume|speed|set]");
            }
            synchronizer.synchronizeAll();
            show(sender);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(Component.text(exception.getMessage(), NamedTextColor.RED));
        }
        return true;
    }

    private void show(CommandSender sender) {
        var time = calendar.snapshot();
        String state = time.paused() ? "paused" : time.speed() == 1 ? "running" : "running at " + time.speed() + "x";
        sender.sendMessage(Component.text(time.displayDate() + " · " + time.displayTime() + " · "
                + time.period() + " · " + time.season() + " (" + state + ")", NamedTextColor.GOLD));
    }

    @Override public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return List.of("show", "pause", "resume", "speed", "set").stream()
                .filter(value -> value.startsWith(prefix)).toList();
    }
}
