package org.tomdang.critter.command;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tomdang.encounter.runtime.EncounterRuntimeService;

/** Starts isolated critter encounters without coupling test content to a quest. */
public final class CritterTestCommand implements CommandExecutor, TabCompleter {
    private static final java.util.Map<String, String> FIELD_TESTS = java.util.Map.of(
            "mossback", "MOSSBACK_FIELD_TEST",
            "glimmerfly", "TUTORIAL_GLIMMERFLY",
            "bramblehog", "BRAMBLEHOG_FIELD_TEST",
            "dewhopper", "DEWHOPPER_FIELD_TEST",
            "burrowtail", "BURROWTAIL_FIELD_TEST",
            "screecher", "CANOPY_SCREECHER_FIELD_TEST",
            "sporeling", "SPORELING_FIELD_TEST");
    private final EncounterRuntimeService encounters;

    public CritterTestCommand(EncounterRuntimeService encounters) {
        if (encounters == null) throw new IllegalArgumentException("Encounter runtime is required");
        this.encounters = encounters;
    }

    @Override public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command requires a player.", NamedTextColor.RED));
            return true;
        }
        if (!sender.hasPermission("tomblock.admin.crittertest")) {
            sender.sendMessage(Component.text("You do not have permission to test critters.", NamedTextColor.RED));
            return true;
        }
        String operation = args.length == 0 ? "mossback" : args[0].toLowerCase(Locale.ROOT);
        if (operation.equals("stop")) {
            var active = encounters.findFor(player.getUniqueId());
            if (active.isEmpty()) player.sendMessage(Component.text("No encounter is active.", NamedTextColor.GRAY));
            else if (!FIELD_TESTS.containsValue(active.get().definitionId())) {
                player.sendMessage(Component.text(
                        "Your active encounter belongs to gameplay and cannot be stopped by this test command.",
                        NamedTextColor.RED));
            }
            else {
                encounters.fail(active.get().instanceId(), "ADMIN_STOP");
                player.sendMessage(Component.text("Active encounter stopped.", NamedTextColor.YELLOW));
            }
            return true;
        }
        String encounterId = FIELD_TESTS.get(operation);
        if (encounterId == null) {
            usage(player);
            return true;
        }
        try {
            encounters.start(encounterId, player.getUniqueId(), Set.of(player.getUniqueId()));
            player.sendMessage(Component.text("Started " + encounterId + ".", NamedTextColor.GREEN));
        } catch (IllegalStateException exception) {
            player.sendMessage(Component.text("Stop your current encounter first with /crittertest stop.",
                    NamedTextColor.RED));
        }
        return true;
    }

    private void usage(Player player) {
        player.sendMessage(Component.text("Usage: /crittertest <mossback|glimmerfly|bramblehog|dewhopper|burrowtail|screecher|sporeling|stop>", NamedTextColor.RED));
    }

    @Override public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return java.util.stream.Stream.concat(FIELD_TESTS.keySet().stream().sorted(), java.util.stream.Stream.of("stop"))
                .filter(value -> value.startsWith(prefix)).toList();
    }
}
