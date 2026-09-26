package org.tomdang.critter.presentation.critterdex;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.tomdang.critter.definition.CritterDefinition;
import org.tomdang.critter.definition.CritterRegistry;
import org.tomdang.critter.journal.CritterJournalEntry;
import org.tomdang.critter.journal.CritterJournalService;
import org.tomdang.guiframework.*;

import java.util.ArrayList;
import java.util.List;

public final class CritterdexDetailScreen implements GuiScreen {
    public static final String ID = "CRITTERDEX_DETAIL";
    private final CritterRegistry definitions;
    private final CritterJournalService journal;
    private final GuiLayout layout;

    public CritterdexDetailScreen(CritterRegistry definitions, CritterJournalService journal, GuiLayout layout) {
        this.definitions = definitions; this.journal = journal; this.layout = layout;
    }

    @Override public String id() { return ID; }
    @Override public int size() { return layout.size(); }
    @Override public Component title(GuiRenderContext context) {
        CritterDefinition definition = definitions.require(context.text("critter", ""));
        return Component.text(layout.title().replace("{critter}", definition.name()), NamedTextColor.DARK_GREEN);
    }

    @Override
    public void render(GuiRenderContext context, GuiCanvas canvas) {
        CritterDefinition definition = definitions.require(context.text("critter", ""));
        CritterJournalEntry progress = journal.find(context.player().getUniqueId(), definition.id()).orElse(null);
        boolean discovered = progress != null;

        List<Component> summary = new ArrayList<>();
        summary.add(Component.text("Type: ", NamedTextColor.GRAY).append(Component.text(CritterdexItems.title(definition.family()), NamedTextColor.AQUA)));
        summary.add(Component.text("Rarity: ", NamedTextColor.GRAY).append(Component.text(CritterdexItems.title(definition.rarity().name()), NamedTextColor.WHITE)));
        summary.add(Component.text("Movement: ", NamedTextColor.GRAY).append(Component.text(CritterdexItems.readable(definition.movement().modes()), NamedTextColor.WHITE)));
        summary.add(Component.empty());
        summary.add(Component.text("Hunted: ", NamedTextColor.GRAY).append(Component.text(discovered ? progress.successfulHunts() : 0, NamedTextColor.GOLD)));
        summary.add(Component.text("Observed: ", NamedTextColor.GRAY).append(Component.text(discovered ? progress.observations() : 0, NamedTextColor.YELLOW)));
        Material icon = Material.matchMaterial(definition.journalIcon());
        canvas.put(layout.slot("portrait"), CritterdexItems.item(icon == null ? Material.PAPER : icon,
                Component.text(definition.name(), discovered ? NamedTextColor.GREEN : NamedTextColor.GRAY), summary), GuiSlotRole.READ_ONLY);

        if (discovered) {
            canvas.put(layout.slot("description"), CritterdexItems.item(Material.WRITABLE_BOOK,
                    Component.text("Description", NamedTextColor.GOLD),
                    CritterdexItems.wrapped(definition.journalDescription(), NamedTextColor.GRAY, 36)), GuiSlotRole.READ_ONLY);
            canvas.put(layout.slot("habitat"), CritterdexItems.item(Material.COMPASS,
                    Component.text("Where It Spawns", NamedTextColor.GREEN),
                    List.of(Component.text(CritterdexItems.readable(definition.habitats()), NamedTextColor.GRAY))), GuiSlotRole.READ_ONLY);
            List<Component> behavior = new ArrayList<>();
            behavior.add(Component.text("Temperament: " + CritterdexItems.readable(definition.temperaments()), NamedTextColor.GRAY));
            behavior.add(Component.text("Hunting style: " + CritterdexItems.readable(definition.hunting().archetypes()), NamedTextColor.GRAY));
            canvas.put(layout.slot("behavior"), CritterdexItems.item(Material.SPYGLASS,
                    Component.text("Behavior", NamedTextColor.AQUA), behavior), GuiSlotRole.READ_ONLY);
        } else {
            canvas.put(layout.slot("description"), CritterdexItems.item(Material.GRAY_DYE,
                    Component.text("Undiscovered", NamedTextColor.GRAY),
                    List.of(Component.text("Find and observe this critter to reveal its field notes.", NamedTextColor.DARK_GRAY))), GuiSlotRole.LOCKED);
        }

        canvas.put(layout.slot("back"), CritterdexItems.button(Material.ARROW, "Back", NamedTextColor.YELLOW),
                GuiSlotRole.NAVIGATION, action -> GuiActionResult.back());
        canvas.put(layout.slot("close"), CritterdexItems.button(Material.BARRIER, "Close", NamedTextColor.RED),
                GuiSlotRole.ACTION, action -> GuiActionResult.close());
        canvas.fillEmpty(CritterdexItems.item(Material.RED_STAINED_GLASS_PANE, Component.text(" "), List.of()));
    }
}
