package org.tomdang.activity.bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.tomdang.activity.ActivityAccessService;
import org.tomdang.activity.ActivityInstance;

/** Applies activity visibility and authority rules to Bukkit entities. */
public final class BukkitActivityEntityController implements Listener, AutoCloseable {
    private final Plugin plugin;
    private final ActivityAccessService access;
    private final Map<UUID, Binding> bindings = new HashMap<>();

    public BukkitActivityEntityController(Plugin plugin, ActivityAccessService access) {
        if (plugin == null || access == null) throw new IllegalArgumentException("Activity entity dependencies are required");
        this.plugin = plugin;
        this.access = access;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /** A public encounter model: visible according to policy, but never directly mutable. */
    public void publishVisual(Entity entity, ActivityInstance activity) {
        bind(entity, activity, false, false);
    }

    /** A private input surface such as a clue or snare selector. */
    public void publishControl(Entity entity, ActivityInstance activity) {
        bind(entity, activity, true, false);
    }

    /** A combat/fishing target that accepts authorized contributions. */
    public void publishContributionTarget(Entity entity, ActivityInstance activity) {
        bind(entity, activity, false, true);
    }

    public void unregister(Entity entity) {
        if (entity != null) bindings.remove(entity.getUniqueId());
    }

    private void bind(Entity entity, ActivityInstance activity, boolean acceptsInteraction, boolean acceptsDamage) {
        if (entity == null || activity == null) throw new IllegalArgumentException("Entity and activity are required");
        bindings.put(entity.getUniqueId(), new Binding(entity, activity, acceptsInteraction, acceptsDamage));
        boolean publiclyVisible = switch (activity.policy().visibility()) {
            case NEARBY, WORLD -> !acceptsInteraction;
            case OWNER, PARTICIPANTS -> false;
        };
        entity.setVisibleByDefault(publiclyVisible);
        for (Player player : Bukkit.getOnlinePlayers()) applyVisibility(player, bindings.get(entity.getUniqueId()));
    }

    private void applyVisibility(Player player, Binding binding) {
        boolean visible = binding.acceptsInteraction
                ? access.canInteract(binding.activity, player.getUniqueId())
                : access.canView(binding.activity, player.getUniqueId());
        if (visible) player.showEntity(plugin, binding.entity);
        else player.hideEntity(plugin, binding.entity);
    }

    @EventHandler
    public void join(PlayerJoinEvent event) {
        for (Binding binding : bindings.values()) applyVisibility(event.getPlayer(), binding);
    }

    @EventHandler(ignoreCancelled = true)
    public void interact(PlayerInteractEntityEvent event) {
        Binding binding = bindings.get(event.getRightClicked().getUniqueId());
        if (binding == null) return;
        if (!binding.acceptsInteraction || !access.canInteract(binding.activity, event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void damage(EntityDamageByEntityEvent event) {
        Binding binding = bindings.get(event.getEntity().getUniqueId());
        if (binding == null) return;
        Player player = playerDamager(event.getDamager());
        boolean authorized = player != null && access.canInteract(binding.activity, player.getUniqueId());
        if (!binding.acceptsDamage || (!authorized && access.blocksUnauthorizedContribution(binding.activity))) {
            event.setCancelled(true);
        }
    }

    private Player playerDamager(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) return player;
        return null;
    }

    @Override public void close() {
        bindings.clear();
        HandlerList.unregisterAll(this);
    }

    private record Binding(Entity entity, ActivityInstance activity, boolean acceptsInteraction,
                           boolean acceptsDamage) {}
}
