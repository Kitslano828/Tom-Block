package org.tomdang.critter.presentation.critterdex;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.tomdang.critter.definition.CritterDefinition;
import org.tomdang.critter.definition.CritterRegistry;
import org.tomdang.critter.journal.CritterJournalEntry;
import org.tomdang.critter.journal.CritterJournalService;
import org.tomdang.guiframework.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CritterdexIndexScreen implements GuiScreen {
    public static final String ID = "CRITTERDEX_INDEX";
    private final CritterRegistry definitions;
    private final CritterJournalService journal;
    private final GuiLayout layout;

    public CritterdexIndexScreen(CritterRegistry definitions, CritterJournalService journal, GuiLayout layout) {
        this.definitions = definitions; this.journal = journal; this.layout = layout;
    }

    @Override public String id() { return ID; }
    @Override public int size() { return layout.size(); }
    @Override public Component title(GuiRenderContext context) { return Component.text(layout.title(), NamedTextColor.DARK_GREEN); }

    @Override
    public void render(GuiRenderContext context, GuiCanvas canvas) {
        List<CritterDefinition> species = definitions.all().stream()
                .sorted(Comparator.comparing(CritterDefinition::name)).toList();
        List<Integer> entrySlots = layout.slotsStartingWith("entry-");
        if (entrySlots.isEmpty()) throw new IllegalStateException("Critterdex layout has no entry slots");
        GuiPagination<CritterDefinition> page = GuiPagination.of(species, context.intValue("page", 0), entrySlots.size());
        Map<String, CritterJournalEntry> collection = journal.collection(context.player().getUniqueId());

        for (int index = 0; index < page.entries().size(); index++) {
            CritterDefinition definition = page.entries().get(index);
            CritterJournalEntry progress = collection.get(definition.id());
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(CritterdexItems.title(definition.family()) + " type", NamedTextColor.DARK_AQUA));
            lore.add(Component.text(definition.rarity().name(), NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Hunted: ", NamedTextColor.GRAY)
                    .append(Component.text(progress == null ? 0 : progress.successfulHunts(), NamedTextColor.GOLD)));
            lore.add(Component.empty());
            lore.add(Component.text(progress == null ? "Not yet discovered" : "Click to inspect", progress == null ? NamedTextColor.DARK_GRAY : NamedTextColor.YELLOW));
            Material icon = Material.matchMaterial(definition.journalIcon());
            ItemStack rendered = CritterdexItems.item(icon == null ? Material.PAPER : icon,
                    Component.text(definition.name(), progress == null ? NamedTextColor.GRAY : NamedTextColor.GREEN), lore);
            canvas.put(entrySlots.get(index), rendered, GuiSlotRole.NAVIGATION, action ->
                    GuiActionResult.open(CritterdexDetailScreen.ID, Map.of("critter", definition.id(), "page", page.page())));
        }
        if (page.hasPrevious()) canvas.put(layout.slot("previous"), CritterdexItems.button(Material.ARROW, "Previous Page", NamedTextColor.YELLOW),
                GuiSlotRole.NAVIGATION, action -> GuiActionResult.refresh(Map.of("page", page.page() - 1)));
        if (page.hasNext()) canvas.put(layout.slot("next"), CritterdexItems.button(Material.ARROW, "Next Page", NamedTextColor.YELLOW),
                GuiSlotRole.NAVIGATION, action -> GuiActionResult.refresh(Map.of("page", page.page() + 1)));
        canvas.put(layout.slot("close"), CritterdexItems.button(Material.BARRIER, "Close", NamedTextColor.RED),
                GuiSlotRole.ACTION, action -> GuiActionResult.close());
        canvas.fillEmpty(CritterdexItems.item(Material.RED_STAINED_GLASS_PANE, Component.text(" "), List.of()));
    }
}
