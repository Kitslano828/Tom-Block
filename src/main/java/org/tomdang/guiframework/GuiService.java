package org.tomdang.guiframework;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/** Sole runtime entry point for opening and operating framework-owned menus. */
public final class GuiService implements AutoCloseable {
    private final Plugin plugin;
    private final GuiRegistry screens;
    private final GuiSessionRegistry sessions;

    public GuiService(Plugin plugin, GuiRegistry screens, GuiSessionRegistry sessions) {
        if (plugin == null || screens == null || sessions == null) throw new IllegalArgumentException("GUI service dependencies are required");
        this.plugin = plugin;
        this.screens = screens;
        this.sessions = sessions;
    }

    public void open(Player player, String screenId) { open(player, screenId, Map.of()); }

    public void open(Player player, String screenId, Map<String, Object> state) {
        requirePlayer(player);
        GuiRoute route = new GuiRoute(screenId, state);
        screens.require(route.screenId());
        render(player, sessions.open(player.getUniqueId(), route));
    }

    void activate(Player player, GuiInventoryHolder holder, int slotIndex, GuiInteraction interaction) {
        Optional<GuiSession> current = current(player, holder);
        if (current.isEmpty()) return;
        GuiSlot slot = holder.slot(slotIndex).orElse(null);
        if (slot == null || !slot.role().interactive()) return;

        GuiSession session = current.get();
        GuiActionResult result;
        try {
            result = slot.action().execute(new GuiActionContext(
                    player, holder.screenId(), slotIndex, interaction, session.current().state()
            ));
        } catch (RuntimeException exception) {
            plugin.getLogger().log(Level.SEVERE, "GUI action failed for " + holder.screenId() + " slot " + slotIndex, exception);
            player.sendMessage(Component.text("That menu action could not be completed.", NamedTextColor.RED));
            return;
        }
        apply(player, session, result == null ? GuiActionResult.stay() : result);
    }

    void onClosed(Player player, GuiInventoryHolder holder) {
        current(player, holder).ifPresent(session -> sessions.close(player.getUniqueId(), session.token()));
    }

    void closeSilently(Player player) { sessions.close(player.getUniqueId()); }

    private void apply(Player player, GuiSession session, GuiActionResult result) {
        switch (result) {
            case GuiActionResult.Stay ignored -> { }
            case GuiActionResult.Refresh refresh -> {
                session.refresh(new GuiRoute(session.current().screenId(), refresh.state()));
                render(player, session);
            }
            case GuiActionResult.Open open -> {
                screens.require(open.screenId());
                session.navigate(new GuiRoute(open.screenId(), open.state()));
                render(player, session);
            }
            case GuiActionResult.Back ignored -> {
                if (session.back()) render(player, session);
                else closeSession(player, session);
            }
            case GuiActionResult.Close ignored -> closeSession(player, session);
        }
    }

    private void render(Player player, GuiSession session) {
        GuiScreen screen = screens.require(session.current().screenId());
        GuiRenderContext context = new GuiRenderContext(player, session.current().state());
        GuiCanvas canvas = new GuiCanvas(screen.size());
        screen.render(context, canvas);
        long revision = session.nextRevision();
        GuiInventoryHolder holder = new GuiInventoryHolder(
                player.getUniqueId(), session.token(), session.current().screenId(), revision, canvas.slots()
        );
        Component title = screen.title(context);
        if (title == null) throw new IllegalStateException("GUI screen " + screen.id() + " returned a null title");
        Inventory inventory = Bukkit.createInventory(holder, screen.size(), title);
        canvas.slots().forEach((index, guiSlot) -> inventory.setItem(index, guiSlot.item()));
        holder.bind(inventory);
        player.openInventory(inventory);
    }

    private Optional<GuiSession> current(Player player, GuiInventoryHolder holder) {
        if (!player.getUniqueId().equals(holder.playerId())) return Optional.empty();
        return sessions.find(player.getUniqueId()).filter(session ->
                session.token().equals(holder.sessionToken()) &&
                session.revision() == holder.revision() &&
                session.current().screenId().equals(holder.screenId())
        );
    }

    private void closeSession(Player player, GuiSession session) {
        sessions.close(player.getUniqueId(), session.token());
        player.closeInventory();
    }

    private static void requirePlayer(Player player) {
        if (player == null) throw new IllegalArgumentException("GUI player is required");
    }

    @Override
    public void close() {
        for (GuiSession session : sessions.all()) {
            Player player = Bukkit.getPlayer(session.playerId());
            if (player != null && player.getOpenInventory().getTopInventory().getHolder() instanceof GuiInventoryHolder) {
                player.closeInventory();
            }
        }
        sessions.clear();
    }
}
