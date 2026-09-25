package org.tomdang.entityai.diagnostics;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.entityai.core.AiBrain;
import org.tomdang.entityai.core.AiDiagnosticsSnapshot;
import org.tomdang.entityai.core.AiVector;
import org.tomdang.entityai.core.PerceivedEntity;
import org.tomdang.entityai.runtime.EntityAiRuntime;

/** Operator inspection and private particle visualization for registered AI agents. */
public final class AiDiagnosticsCommand implements CommandExecutor, TabCompleter, AutoCloseable {
    private static final double NEAREST_RANGE = 48;
    private static final Particle.DustOptions AGENT = dust(255, 70, 70);
    private static final Particle.DustOptions HOME = dust(70, 235, 100);
    private static final Particle.DustOptions DESTINATION = dust(50, 210, 255);
    private static final Particle.DustOptions PERCEPTION = dust(255, 215, 50);
    private final Plugin plugin;
    private final EntityAiRuntime runtime;
    private final Map<UUID, BukkitTask> visualizations = new HashMap<>();

    public AiDiagnosticsCommand(Plugin plugin, EntityAiRuntime runtime) {
        if (plugin == null || runtime == null) throw new IllegalArgumentException("AI diagnostics dependencies are required");
        this.plugin = plugin;
        this.runtime = runtime;
    }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("tomblock.admin.aidiag")) {
            sender.sendMessage(Component.text("You do not have permission to inspect entity AI.", NamedTextColor.RED));
            return true;
        }
        String operation = args.length == 0 ? "nearest" : args[0].toLowerCase(Locale.ROOT);
        return switch (operation) {
            case "summary" -> summary(sender);
            case "list" -> list(sender);
            case "nearest" -> playerOnly(sender, player -> nearest(player).ifPresentOrElse(
                    brain -> inspect(player, brain), () -> noNearby(player)));
            case "inspect" -> inspectId(sender, args);
            case "show", "visualize" -> show(sender, args);
            case "hide" -> hide(sender);
            default -> usage(sender);
        };
    }

    private boolean summary(CommandSender sender) {
        sender.sendMessage(Component.text("Entity AI: ", NamedTextColor.GOLD)
                .append(Component.text(runtime.size() + " registered brains, " + runtime.budget()
                        + " updates/tick budget", NamedTextColor.YELLOW)));
        return true;
    }

    private boolean list(CommandSender sender) {
        summary(sender);
        if (runtime.size() == 0) sender.sendMessage(Component.text("No agents are registered.", NamedTextColor.GRAY));
        runtime.all().stream().map(AiBrain::diagnostics)
                .sorted(Comparator.comparing(AiDiagnosticsSnapshot::type).thenComparing(AiDiagnosticsSnapshot::id))
                .forEach(value -> sender.sendMessage(Component.text(value.type() + " ", NamedTextColor.AQUA)
                        .append(Component.text(value.id().toString(), NamedTextColor.GRAY))
                        .append(Component.text(" goal=" + value.activeGoal().map(goal -> goal.id()).orElse("none")
                                + " world=" + value.worldId() + " pos=" + vector(value.position()),
                                NamedTextColor.YELLOW))));
        return true;
    }

    private boolean inspectId(CommandSender sender, String[] args) {
        if (args.length != 2) return usage(sender);
        try {
            AiBrain brain = runtime.find(UUID.fromString(args[1])).orElse(null);
            if (brain == null) sender.sendMessage(Component.text("No registered AI agent has that UUID.", NamedTextColor.RED));
            else inspect(sender, brain);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(Component.text("Invalid agent UUID.", NamedTextColor.RED));
        }
        return true;
    }

    private void inspect(CommandSender sender, AiBrain brain) {
        AiDiagnosticsSnapshot value = brain.diagnostics();
        sender.sendMessage(Component.text(value.type() + " AI ", NamedTextColor.GOLD)
                .append(Component.text(value.id().toString(), NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("Position " + vector(value.position()) + " | home " + vector(value.home())
                + " | home distance " + decimal(Math.sqrt(value.position().distanceSquared(value.home()))),
                NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Capabilities: " + names(value.capabilities()), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Goals: " + value.goals().stream()
                .map(goal -> (goal.active() ? "*" : "") + goal.id() + "(" + goal.priority() + ")")
                .reduce((left, right) -> left + ", " + right).orElse("none"), NamedTextColor.AQUA));
        value.navigation().ifPresentOrElse(navigation -> sender.sendMessage(Component.text(
                        "Navigation: " + (navigation.active() ? "active " : "last ") + navigation.status()
                                + " -> " + vector(navigation.request().destination())
                                + " speed=" + decimal(navigation.request().speed())
                                + " updated=" + navigation.updatedTick(), NamedTextColor.GREEN)),
                () -> sender.sendMessage(Component.text("Navigation: no request yet", NamedTextColor.DARK_GRAY)));
        sender.sendMessage(Component.text("Perception: " + value.perception().entities().size()
                + " entities (sample tick " + value.perception().tick() + ")", NamedTextColor.LIGHT_PURPLE));
        for (PerceivedEntity entity : value.perception().entities()) {
            sender.sendMessage(Component.text(" - " + entity.id() + " distance="
                    + decimal(Math.sqrt(entity.position().distanceSquared(value.position()))) + " velocity="
                    + decimal(Math.sqrt(entity.velocity().lengthSquared())) + " visible=" + entity.visible()
                    + " owner=" + entity.owner() + " hostile=" + entity.hostile(), NamedTextColor.GRAY));
        }
        sender.sendMessage(Component.text("Memory: " + (value.memory().isEmpty() ? "empty" : value.memory()),
                NamedTextColor.BLUE));
    }

    private boolean show(CommandSender sender, String[] args) {
        return playerOnly(sender, player -> {
            if (args.length == 2 && args[1].equalsIgnoreCase("off")) { stopVisualization(player, true); return; }
            int seconds = 10;
            if (args.length == 2) {
                try { seconds = Math.max(1, Math.min(60, Integer.parseInt(args[1]))); }
                catch (NumberFormatException exception) { player.sendMessage(Component.text("Seconds must be 1-60.", NamedTextColor.RED)); return; }
            } else if (args.length > 2) { usage(player); return; }
            AiBrain brain = nearest(player).orElse(null);
            if (brain == null) { noNearby(player); return; }
            stopVisualization(player, false);
            long endTick = org.bukkit.Bukkit.getCurrentTick() + seconds * 20L;
            BukkitTask task = new BukkitRunnable() {
                @Override public void run() {
                    if (!player.isOnline() || org.bukkit.Bukkit.getCurrentTick() >= endTick
                            || runtime.find(brain.agent().id()).isEmpty()) {
                        cancel(); visualizations.remove(player.getUniqueId()); return;
                    }
                    render(player, brain.diagnostics());
                }
            }.runTaskTimer(plugin, 0L, 5L);
            visualizations.put(player.getUniqueId(), task);
            player.sendMessage(Component.text("Visualizing " + brain.agent().type() + " for " + seconds
                    + "s: red=agent, green=home, aqua=destination, yellow=perception.", NamedTextColor.GREEN));
        });
    }

    private boolean hide(CommandSender sender) {
        return playerOnly(sender, player -> stopVisualization(player, true));
    }

    private void render(Player player, AiDiagnosticsSnapshot value) {
        if (!player.getWorld().getKey().asString().equals(value.worldId())) return;
        point(player, value.position(), AGENT, 3);
        point(player, value.home(), HOME, 3);
        line(player, value.position(), value.home(), HOME);
        value.navigation().filter(org.tomdang.entityai.navigation.NavigationDiagnostics::active).ifPresent(navigation -> {
            point(player, navigation.request().destination(), DESTINATION, 4);
            line(player, value.position(), navigation.request().destination(), DESTINATION);
        });
        value.perception().entities().stream().filter(PerceivedEntity::visible)
                .forEach(entity -> line(player, value.position(), entity.position(), PERCEPTION));
    }

    private Optional<AiBrain> nearest(Player player) {
        AiVector origin = new AiVector(player.getX(), player.getY(), player.getZ());
        String world = player.getWorld().getKey().asString();
        return runtime.all().stream().filter(brain -> brain.agent().valid())
                .filter(brain -> brain.agent().worldId().equals(world))
                .filter(brain -> brain.agent().position().distanceSquared(origin) <= NEAREST_RANGE * NEAREST_RANGE)
                .min(Comparator.comparingDouble(brain -> brain.agent().position().distanceSquared(origin)));
    }

    private void point(Player player, AiVector position, Particle.DustOptions style, int count) {
        player.spawnParticle(Particle.DUST, location(player, position), count, .08, .08, .08, 0, style);
    }

    private void line(Player player, AiVector from, AiVector to, Particle.DustOptions style) {
        AiVector delta = to.subtract(from);
        double distance = Math.sqrt(delta.lengthSquared());
        if (distance < .05) return;
        int steps = Math.min(48, Math.max(1, (int) Math.ceil(distance / .5)));
        for (int index = 1; index <= steps; index++) {
            AiVector position = from.add(delta.multiply((double) index / steps));
            player.spawnParticle(Particle.DUST, location(player, position), 1, 0, 0, 0, 0, style);
        }
    }

    private Location location(Player player, AiVector value) {
        return new Location(player.getWorld(), value.x(), value.y(), value.z());
    }
    private void stopVisualization(Player player, boolean announce) {
        BukkitTask old = visualizations.remove(player.getUniqueId());
        if (old != null) old.cancel();
        if (announce) player.sendMessage(Component.text(old == null ? "No AI visualization was active."
                : "AI visualization hidden.", old == null ? NamedTextColor.GRAY : NamedTextColor.YELLOW));
    }
    private void noNearby(Player player) {
        player.sendMessage(Component.text("No registered AI agent is within " + (int) NEAREST_RANGE + " blocks.",
                NamedTextColor.RED));
    }
    private boolean playerOnly(CommandSender sender, java.util.function.Consumer<Player> action) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This operation requires a player.", NamedTextColor.RED));
        } else action.accept(player);
        return true;
    }
    private boolean usage(CommandSender sender) {
        sender.sendMessage(Component.text("Usage: /aidiag <summary|list|nearest|inspect <uuid>|show [1-60|off]|hide>",
                NamedTextColor.RED));
        return true;
    }
    private static Particle.DustOptions dust(int red, int green, int blue) {
        return new Particle.DustOptions(Color.fromRGB(red, green, blue), 1.0f);
    }
    private String vector(AiVector value) { return decimal(value.x()) + "," + decimal(value.y()) + "," + decimal(value.z()); }
    private String decimal(double value) { return String.format(Locale.ROOT, "%.2f", value); }
    private String names(java.util.Collection<?> values) { return values.isEmpty() ? "none" : values.toString(); }

    @Override public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return List.of("summary", "list", "nearest", "inspect", "show", "hide").stream()
                    .filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("inspect")) {
            String prefix = args[1].toLowerCase(Locale.ROOT);
            return runtime.all().stream().map(brain -> brain.agent().id().toString())
                    .filter(value -> value.startsWith(prefix)).toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("show")) return List.of("10", "30", "60", "off");
        return List.of();
    }

    @Override public void close() {
        visualizations.values().forEach(BukkitTask::cancel);
        visualizations.clear();
    }
}
